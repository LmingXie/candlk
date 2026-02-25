package com.bojiu.webapp.user.service;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.ReadStateDao;
import com.bojiu.webapp.user.entity.ReadState;
import org.springframework.stereotype.Service;

/**
 * 对话已读状态表 服务实现类
 */
@Service
public class ReadStateService extends BaseServiceImpl<ReadState, ReadStateDao, Long> {

}
