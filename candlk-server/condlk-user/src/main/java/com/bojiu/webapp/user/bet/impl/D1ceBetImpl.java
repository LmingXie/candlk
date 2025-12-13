package com.bojiu.webapp.user.bet.impl;

import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class D1ceBetImpl extends HgBetImpl {

	@Override
	public BetProvider getProvider() {
		return BetProvider.D1CE;
	}

}