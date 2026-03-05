package com.bojiu.webapp.user.service;

import java.util.List;
import javax.annotation.Resource;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.UserInfoDao;
import com.bojiu.webapp.user.entity.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Telegram用户基础信息表 服务实现类
 */
@Service
public class UserInfoService extends BaseServiceImpl<UserInfo, UserInfoDao, Long> {

	@Resource
	UserService userService;

	@Transactional
	public void saveOrEdit(UserInfo userInfo) {
		baseDao.updateUserInfo(userInfo);
	}

	transient List<Long> botIds, innerIds;
	transient long lastUpdateTime = 0;
	/** 缓存60秒 */
	final long timeout = 60 * 1000;

	public List<Long> getBotIds() {
		if (botIds == null || System.currentTimeMillis() - lastUpdateTime > timeout) {
			botIds = baseDao.findAllBotIds();
		}
		return botIds;
	}

	public List<Long> getInnerIds() {
		if (innerIds == null || System.currentTimeMillis() - lastUpdateTime > timeout) {
			innerIds = userService.findAllUserId();
		}
		return innerIds;
	}

	/** 是否为机器人 */
	public boolean hasBot(Long userId) {
		return getBotIds().contains(userId);
	}

	/** 是否为内部用户 */
	public boolean hasInner(Long userId) {
		return getInnerIds().contains(userId);
	}

}
