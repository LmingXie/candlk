package com.bojiu.webapp.user.service;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.UserInfoDao;
import com.bojiu.webapp.user.entity.UserInfo;
import org.springframework.stereotype.Service;

/**
 * Telegram用户基础信息表 服务实现类
 */
@Service
public class UserInfoService extends BaseServiceImpl<UserInfo, UserInfoDao, Long> {

}
