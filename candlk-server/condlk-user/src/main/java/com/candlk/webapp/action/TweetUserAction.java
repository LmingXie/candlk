package com.candlk.webapp.action;

import java.util.List;
import javax.annotation.Resource;

import com.candlk.common.context.I18N;
import com.candlk.common.model.Messager;
import com.candlk.common.web.Page;
import com.candlk.common.web.Ready;
import com.candlk.context.web.ProxyRequest;
import com.candlk.webapp.base.action.BaseAction;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.form.TweetUserForm;
import com.candlk.webapp.user.form.TweetUserQuery;
import com.candlk.webapp.user.service.TweetUserService;
import com.candlk.webapp.user.vo.TweetUserVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 推文信息表 控制器
 *
 * @since 2025-04-27
 */
@RestController
@RequestMapping("/user")
public class TweetUserAction extends BaseAction {

	@Resource
	TweetUserService tweetUserService;

	@Ready("账号列表")
	@GetMapping("/list")
	public Messager<Page<TweetUserVO>> list(ProxyRequest q, TweetUserQuery query) {
		final Page<TweetUser> page = tweetUserService.findPage(q.getPage(), query);
		return Messager.exposeData(page.transformAndCopy(TweetUserVO::new));
	}

	@Ready("修改账号")
	@GetMapping("/edit")
	public Messager<Void> edit(ProxyRequest q, @Validated TweetUserForm form) {
		List<TweetUser> tweetUsers = tweetUserService.findByIds(form.ids);
		I18N.assertNotNull(form.type, "账号不存在");
		tweetUserService.edit(tweetUsers, form.type);
		return Messager.OK();
	}

}
