package com.candlk.webapp.action;

import javax.annotation.Resource;

import com.candlk.common.model.Messager;
import com.candlk.common.web.Page;
import com.candlk.common.web.Ready;
import com.candlk.context.auth.Permission;
import com.candlk.context.model.MemberType;
import com.candlk.context.web.ProxyRequest;
import com.candlk.webapp.user.form.TweetQuery;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.vo.TweetVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.candlk.webapp.base.action.BaseAction;

/**
 * 推文信息表 控制器
 *
 * @since 2025-04-27
 */
@RestController
@RequestMapping("/tweet")
public class TweetAction extends BaseAction {

	@Resource
	TweetService tweetService;

	@Ready("推文列表")
	@GetMapping("/list")
	public Messager<Page<TweetVO>> list(ProxyRequest q, TweetQuery query) {
		return Messager.exposeData(tweetService.findPage(q.getPage(), query, q.getInterval()));
	}

}
