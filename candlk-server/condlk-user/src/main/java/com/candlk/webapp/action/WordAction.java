package com.candlk.webapp.action;

import java.util.*;
import javax.annotation.Resource;

import com.candlk.common.context.I18N;
import com.candlk.common.model.Messager;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.web.Page;
import com.candlk.common.web.Ready;
import com.candlk.context.model.RedisKey;
import com.candlk.context.web.ProxyRequest;
import com.candlk.webapp.base.action.BaseAction;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.form.*;
import com.candlk.webapp.user.service.TweetWordService;
import com.candlk.webapp.user.vo.TweetWordVO;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 关键词 控制器
 *
 * @since 2025-04-27
 */
@Slf4j
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
	public Messager<Void> imports(ProxyRequest q, @Validated TweetWordForm form) {
		return RedisUtil.fastAttemptInLock(RedisKey.USER_OP_LOCK_PREFIX, () -> {
			List<TweetWord> tweetWords = new ArrayList<>(form.words.size());
			List<String> words = new ArrayList<>(form.words.size());
			final Date now = q.now();
			for (String word : form.words) {
				if (!StringUtil.isEmpty(word)) {
					TweetWord tweetWord = new TweetWord();
					final String trim = word.trim();
					tweetWord.setWords(trim);
					tweetWord.setType(form.type);
					tweetWord.setPriority(form.priority);
					tweetWord.initTime(now);
					tweetWords.add(tweetWord);
					words.add(word.trim());
				}
			}
			if (!tweetWords.isEmpty()) {
				// 排除重复词组
				final Set<String> oldWords = tweetWordService.findWords(words);
				if (!oldWords.isEmpty()) {
					tweetWords.removeIf(t -> oldWords.contains(t.getWords()));
				}
				if (!tweetWords.isEmpty()) {
					try {
						tweetWordService.batchAdd(tweetWords, form.type);
					} catch (Exception e) {
						log.error("导入失败", e);
						return Messager.error("导入失败");
					}
				}
				return Messager.OK("导入成功：排除重复词组【" + oldWords.size() + "】个");
			}
			return Messager.OK("导入成功");
		});
	}

	@Ready("批量删除关键词")
	@GetMapping("/del")
	public Messager<Void> del(ProxyRequest q, TweetWordForm form) throws Exception {
		I18N.assertFalse(CollectionUtils.isEmpty(form.ids), "参数错误");
		List<TweetWord> words = tweetWordService.findByIds(form.ids);
		if (!words.isEmpty()) {
			tweetWordService.del(words);
		}
		return Messager.OK();
	}

	@Ready("热词排名")
	@GetMapping("/rank")
	public Messager<List<TweetWord>> rank(ProxyRequest q, Integer type) throws Exception {
		return Messager.exposeData(tweetWordService.rank(type));
	}

	@Ready("修改关键词")
	@GetMapping("/edit")
	public Messager<Void> edit(ProxyRequest q, @Validated EditWordForm form) throws Exception {
		I18N.assertNotNull(form.ids);
		I18N.assertNotNull(form.type, "关键词类型不可或缺");
		I18N.assertTrue(TweetWord.TYPE_STOP != form.type, "不可修改为停用词！");
		final List<TweetWord> tweetWords = tweetWordService.findByIds(form.ids);
		I18N.assertNotNull(CollectionUtils.isNotEmpty(tweetWords), "关键词不存在");
		for (TweetWord tweetWord : tweetWords) {
			I18N.assertTrue(tweetWord.getType() != TweetWord.TYPE_STOP, "不可修改停用词！");
		}
		tweetWordService.edit(tweetWords, TweetWord.TYPE, form.type, q.now());
		return Messager.OK();
	}

	@Ready("禁用/启用关键词")
	@GetMapping("/editStatus")
	public Messager<Void> editStatus(ProxyRequest q, @Validated EditWordForm form) throws Exception {
		I18N.assertNotNull(form.ids);
		I18N.assertNotNull(form.status, "关键词状态不可或缺");
		final List<TweetWord> tweetWords = tweetWordService.findByIds(form.ids);
		I18N.assertNotNull(CollectionUtils.isNotEmpty(tweetWords), "关键词不存在");
		tweetWordService.edit(tweetWords, TweetWord.STATUS, form.status, q.now());
		return Messager.OK();
	}

}
