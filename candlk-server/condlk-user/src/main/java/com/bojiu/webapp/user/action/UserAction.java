package com.bojiu.webapp.user.action;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;

import com.bojiu.common.model.Messager;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.base.service.RemoteSyncService;
import com.bojiu.webapp.user.api.CockpitXApi;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.handler.DefaultUpdateHandler;
import com.bojiu.webapp.user.service.GlobalCacheSyncService;
import com.bojiu.webapp.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.ArrayUtil;
import org.drinkless.tdlib.Client;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	static final ThreadPoolExecutor loadTaskThreadPool = TaskUtils.newThreadPool(4, 20, 1024, "user-load-");

	@RequestMapping("/sync")
	public Messager<String> sync() {
		// 查询全部账号
		List<User> allUser = userService.findAllNormal();
		if (!allUser.isEmpty()) {
			for (User user : allUser) {
				loadTaskThreadPool.execute(() -> {
					Long userId = user.getUserId();
					Messager<String> msg = cockpitXApi.load(userId);
					if (msg.isOK()) {
						// TODO: 2025/11/26 完善更新逻辑
						Client client = Client.create(new DefaultUpdateHandler(user));
						// TODO 设置代理
						// 通过 SetAuthenticationPhoneNumber 发送验证码，记录发送时间
						// java定时查询正在导入的账号是否收到验证码，收到后通过 CheckAuthenticationCode、CheckAuthenticationPassword 进行验证录入
					} else {
						log.error("加载账号失败：" + userId);
					}
				});
			}
			GlobalCacheSyncService.user().flushCache(RemoteSyncService.UserService, (Object[]) ArrayUtil.toArray(allUser, Long.class, User::getUserId));
		}
		return Messager.OK();
	}

}
