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
import com.candlk.webapp.user.service.TweetWordService;
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

	@Ready("查询搜索关键词")
	@GetMapping("/search")
	public Messager<Page<TweetWord>> search(ProxyRequest q, String words) throws Exception {
		return Messager.exposeData(tweetWordService.search(q.getPage(), words));
	}

	@Ready("批量导入关键词")
	@PostMapping("/imports")
	public Messager<Void> imports(ProxyRequest q, String words) throws Exception {
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
		tweetWordService.batchAdd(tweetWords);
		return Messager.OK();
	}

	@Ready("批量删除关键词")
	@PostMapping("/del")
	public Messager<Void> del(ProxyRequest q, String ids) throws Exception {
		I18N.assertNotNull(ids);
		List<Long> tweetWords = Jsons.parseArray(ids, Long.class);
		tweetWordService.del(tweetWords);
		return Messager.OK();
	}

}
