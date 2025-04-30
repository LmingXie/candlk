package com.candlk.webapp.action;

import javax.annotation.Resource;

import com.candlk.webapp.base.action.BaseAction;
import com.candlk.webapp.user.service.TweetWordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关键词 控制器
 *
 * @since 2025-04-27
 */
@RestController
@RequestMapping("/word")
public class WordAction extends BaseAction {

	@Resource
	TweetWordService tweetWordService;

	// TODO: 2025/4/30 查询搜索关键词

	// TODO: 2025/4/30 批量导入关键词

	// TODO: 2025/4/30 批量删除关键词

}
