package com.bojiu.webapp.user.service;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.UserDao;
import com.bojiu.webapp.user.entity.User;
import org.springframework.stereotype.Service;

/** 账号表 服务实现类 */
@Service
public class UserService extends BaseServiceImpl<User, UserDao, Long> {

}
