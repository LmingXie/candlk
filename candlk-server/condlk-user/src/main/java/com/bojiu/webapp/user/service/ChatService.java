package com.bojiu.webapp.user.service;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.ChatDao;
import com.bojiu.webapp.user.entity.Chat;
import org.springframework.stereotype.Service;

/**
 * 账号对话表 服务实现类
 *
 * @author
 * @since 2025-11-27
 */
@Service
public class ChatService extends BaseServiceImpl<Chat, ChatDao, Long> {

}
