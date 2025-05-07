package com.candlk.webapp.action;

import java.util.List;
import javax.annotation.Resource;

import com.candlk.common.context.I18N;
import com.candlk.common.model.Messager;
import com.candlk.common.web.Page;
import com.candlk.common.web.Ready;
import com.candlk.context.web.Jsons;
import com.candlk.context.web.ProxyRequest;
import com.candlk.webapp.base.action.BaseAction;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.form.TweetWordQuery;
import com.candlk.webapp.user.service.TweetWordService;
import com.candlk.webapp.user.vo.TweetWordVO;
import me.codeplayer.util.StringUtil;
import org.springframework.web.bind.annotation.*;

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

	@Ready("关键词列表")
	@GetMapping("/list")
	public Messager<Page<TweetWordVO>> list(ProxyRequest q, TweetWordQuery query) throws Exception {
		final Page<TweetWord> page = tweetWordService.findPage(q.getPage(), query);
		return Messager.exposeData(page.transformAndCopy(TweetWordVO::new));
	}

	@Ready("查询搜索关键词")
	@GetMapping("/search")
	public Messager<Page<TweetWordVO>> search(ProxyRequest q, TweetWordQuery query) throws Exception {
		return Messager.exposeData(tweetWordService.search(q.getPage(), query).transformAndCopy(TweetWordVO::new));
	}

	@Ready("批量导入关键词")
	@GetMapping("/imports")
	public Messager<Void> imports(ProxyRequest q, String words, Integer type) throws Exception {
		I18N.assertNotNull(words);
		List<TweetWord> tweetWords = Jsons.parseArray(words, TweetWord.class);
		for (TweetWord tweetWord : tweetWords) {
			I18N.assertTrue(StringUtil.notEmpty(tweetWord.getWords()), "关键词不能为空");
			tweetWord.setWords(tweetWord.getWords().trim());
			if (tweetWord.getType() == null) {
				tweetWord.setType(TweetWord.TYPE_NORMAL);
			}
			tweetWord.setPriority(0);
		}
		tweetWordService.batchAdd(tweetWords, type);
		return Messager.OK();
	}

	@Ready("批量删除关键词")
	@GetMapping("/del")
	public Messager<Void> del(ProxyRequest q, String ids, Integer type) throws Exception {
		I18N.assertNotNull(ids);
		List<Long> tweetWords = Jsons.parseArray(ids, Long.class);
		tweetWordService.del(tweetWords, type);
		return Messager.OK();
	}

	@Ready("热词排名")
	@GetMapping("/rank")
	public Messager<List<TweetWord>> rank(ProxyRequest q, Integer type) throws Exception {
		return Messager.exposeData(tweetWordService.rank(type));
	}

	@Ready("修改关键词")
	@GetMapping("/edit")
	public Messager<Void> edit(ProxyRequest q, Long id, Integer type) throws Exception {
		I18N.assertNotNull(id);
		TweetWord tweetWord = tweetWordService.get(id);
		I18N.assertNotNull(tweetWord, "关键词不存在");
		tweetWordService.edit(tweetWord, type);
		return Messager.OK();
	}

}
