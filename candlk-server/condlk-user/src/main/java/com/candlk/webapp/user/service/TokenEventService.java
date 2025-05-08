package com.candlk.webapp.user.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TokenEventDao;
import com.candlk.webapp.user.entity.TokenEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 代币事件表 服务实现类
 *
 * @since 2025-04-27
 */
@Service
public class TokenEventService extends BaseServiceImpl<TokenEvent, TokenEventDao, Long> {

	@Transactional
	public void create(TokenEvent input) {
		super.update(new UpdateWrapper<TokenEvent>()
				.set(TokenEvent.STATUS, TokenEvent.CREATED)
				.set(TokenEvent.CA, input.getCa())
				.set(TokenEvent.UPDATE_TIME, input.getUpdateTime())
				.eq(TokenEvent.ID, input.getId())
		);
	}

}
