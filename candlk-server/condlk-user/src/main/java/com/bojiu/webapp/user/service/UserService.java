package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;

import com.bojiu.common.model.Status;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.base.service.*;
import com.bojiu.webapp.base.util.LocalScheduler;
import com.bojiu.webapp.user.dao.UserDao;
import com.bojiu.webapp.user.entity.Message;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.handler.DefaultUpdateHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.CollectionUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

/** 账号表 服务实现类 */
@Slf4j
@Service
public class UserService extends BaseServiceImpl<User, UserDao, Long> implements CacheSyncService {

	@Resource
	MessageService messageService;
	/** 缓存：$userId -> $user */
	static final Cache<Long, User> cache = Caffeine.newBuilder()
			.initialCapacity(100)
			.maximumSize(1024)
			.expireAfterWrite(1, TimeUnit.DAYS)
			.build();

	/** 活跃客户端 */
	static final Map<Long, Client> clientMap = new ConcurrentHashMap<>();

	public void putClient(Long userId, Client client) {
		clientMap.put(userId, client);
	}

	public Collection<User> getCacheByIds(Long... ids) {
		final Function<Long, Long> converter = c -> c;
		Map<Long, @NonNull User> all = cache.getAll(ArrayUtil.toList(converter, ids),
				ids_ -> CollectionUtil.toHashMap(findByIds(ids), User::getUserId));
		return all.values();
	}

	public User getCache(Long userId) {
		return cache.get(userId, this::get);
	}

	public Collection<User> getCacheAll() {
		return cache.asMap().values();
	}

	/** 查询全部正常的账号 */
	public List<User> findAllNormal() {
		List<User> users = selectList(smartEq(User.STATUS, Status.YES.value));
		if (!users.isEmpty()) {
			for (User user : users) {
				cache.put(user.getUserId(), user);
			}
		}
		return users;
	}

	@Override
	public String getCacheId() {
		return RemoteSyncService.UserService;
	}

	@Override
	public void flushCache(Object... args) {
		Long userId = (Long) ArrayUtils.get(args, 0);
		if (userId == null) {
			cache.invalidateAll();
		} else {
			cache.invalidate(userId);
		}
	}

	/** 厂商API请求超时计数器：-N=相对正常次数；0=初始化；N=相对超时次数 */
	static final Map<Long, DefaultUpdateHandler> authWaitHandlerMap = new ConcurrentHashMap<>(64, 1F);
	private transient ScheduledFuture<?> scheduledFuture;
	/** 等到超时时间（单位：毫秒） */
	public static final long authTimeout = 3 * 60 * 1000;
	private Date scanBeginTime;
	/** 匹配连续的5个数字 */
	static final Pattern pattern = Pattern.compile("(\\d{5})");

	public synchronized void addWaitAuthTask(Long userId, DefaultUpdateHandler handler) {
		if (authWaitHandlerMap.get(userId) == null) {
			authWaitHandlerMap.put(userId, handler);
			if (scanBeginTime == null || handler.beginTime.before(scanBeginTime)) {
				scanBeginTime = handler.beginTime;
			}
			if (scheduledFuture == null) {
				scheduledFuture = LocalScheduler.getScheduler().scheduleAtFixedRate(() -> {
					if (!authWaitHandlerMap.isEmpty()) {
						for (Map.Entry<Long, DefaultUpdateHandler> entry : authWaitHandlerMap.entrySet()) {
							SpringUtil.asyncRun(() -> {
								Long userId_ = entry.getKey();
								DefaultUpdateHandler updateHandler = entry.getValue();
								List<Message> authMsg = messageService.getAuthMsg(userId_, updateHandler.beginTime);
								if (checkAuthCode(authMsg, updateHandler) || updateHandler.beginTime.getTime() + authTimeout > System.currentTimeMillis()) {
									authWaitHandlerMap.remove(userId_);
								}
							});
						}
					}
				}, 3000);
			}
		} else {
			log.warn("用户已存在等待登录授权的任务：{}", userId);
		}
	}

	private static boolean checkAuthCode(List<Message> authMsg, DefaultUpdateHandler updateHandler) {
		if (authMsg.isEmpty()) {
			return false;
		}
		boolean[] flag = { false };
		Matcher matcher = pattern.matcher(authMsg.removeFirst().getMessage());
		if (matcher.find()) {
			final String code = matcher.group(1);
			if (code != null) {
				// 检查授权验证码
				updateHandler.client.send(new TdApi.CheckAuthenticationCode(code), obj -> {
					if (obj instanceof TdApi.Error) {
						// 继续检查下一个
						checkAuthCode(authMsg, updateHandler);
					} else if (obj instanceof TdApi.Ok) {
						flag[0] = true;
					}
				});
			}
		}
		return flag[0];
	}

}
