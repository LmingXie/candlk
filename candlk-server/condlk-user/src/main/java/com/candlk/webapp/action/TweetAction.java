package com.candlk.webapp.action;

import javax.annotation.Resource;

import com.candlk.common.context.I18N;
import com.candlk.common.model.Messager;
import com.candlk.common.web.Page;
import com.candlk.common.web.Ready;
import com.candlk.context.web.ProxyRequest;
import com.candlk.webapp.base.action.BaseAction;
import com.candlk.webapp.user.entity.TokenEvent;
import com.candlk.webapp.user.form.CreateForm;
import com.candlk.webapp.user.form.TweetQuery;
import com.candlk.webapp.user.service.TokenEventService;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.vo.TweetVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
	@Resource
	TokenEventService tokenEventService;

	@Ready("推文列表")
	@GetMapping("/list")
	public Messager<Page<TweetVO>> list(ProxyRequest q, TweetQuery query) {
		return Messager.exposeData(tweetService.findPage(q.getPage(), query, q.getInterval()));
	}

	@Ready("成功创建代币")
	@GetMapping("/create")
	public Messager<Void> create(ProxyRequest q, @Validated CreateForm form) throws Exception {
		TokenEvent tokenEvent = tokenEventService.get(form.id);
		I18N.assertNotNull(tokenEvent, "未查询到代币信息！");
		tokenEvent.setCa(form.ca);
		tokenEvent.setUpdateTime(q.now());
		tokenEventService.create(tokenEvent);
		return Messager.OK();
	}

	@Ready("追踪器推文列表")
	@GetMapping("/trackers")
	public Messager<Page<TweetVO>> trackers(ProxyRequest q, TweetQuery query) {
		return Messager.exposeData(tweetService.findPageTrackers(q.getPage(), query, q.getInterval()));
	}

}
