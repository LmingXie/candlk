package com.bojiu.webapp.user.action;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;

import com.bojiu.common.model.Messager;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.base.service.RemoteSyncService;
import com.bojiu.webapp.user.api.CockpitXApi;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.handler.DefaultUpdateHandler;
import com.bojiu.webapp.user.service.*;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.ArrayUtil;
import org.drinkless.tdlib.Client;
import org.springframework.web.bind.annotation.*;

/* 账号表 控制器 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserAction extends BaseAction {

	// TODO: 2025/11/26 数据库迁移方案（考虑不进行迁移，java包存放到原有文件夹内，页面逻辑依旧只做静态路由）
	// TODO: 2025/11/26 初始加载已完成同步的客户端
	@Resource
	CockpitXApi cockpitXApi;
	@Resource
	UserService userService;
	@Resource
	MessageService messageService;

	static final ThreadPoolExecutor loadTaskThreadPool = TaskUtils.newThreadPool(4, 20, 1024, "user-load-");

	@Ready("从Cockpit_TG同步全部账号")
	@PostMapping("/sync")
	@Permission(Permission.NONE)
	public Messager<String> sync() {
		// 查询全部账号
		List<User> allUser = userService.findAllNormal();
		if (!allUser.isEmpty()) {
			for (User user : allUser) {
				if (!user.getPhone().equals("66953918358")) {
					continue; // TODO: 2025/11/28 测试
				}
				// loadTaskThreadPool.execute(() -> {
					Long userId = user.getUserId();
					// 通知启动py客户端，收录消息 TODO 超时时提醒应该先启动TG
					Messager<String> msg = cockpitXApi.load(userId);
					if (msg.isOK()) {
						Client.create(new DefaultUpdateHandler(user));
					} else {
						log.error("加载账号失败：" + userId);
					}
				// });
			}
			GlobalCacheSyncService.user().flushCache(RemoteSyncService.UserService, (Object[]) ArrayUtil.toArray(allUser, Long.class, User::getUserId));
		}
		return Messager.OK();
	}

}
