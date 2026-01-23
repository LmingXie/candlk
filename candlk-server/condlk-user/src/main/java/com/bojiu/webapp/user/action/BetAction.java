package com.bojiu.webapp.user.action;

import java.util.*;
import javax.annotation.Resource;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.web.Page;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.context.model.*;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.user.dto.BaseRateConifg;
import com.bojiu.webapp.user.dto.HedgingDTO;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.form.query.HedgingQuery;
import com.bojiu.webapp.user.job.BetMatchJob;
import com.bojiu.webapp.user.service.BetMatchService;
import com.bojiu.webapp.user.service.MetaService;
import com.bojiu.webapp.user.vo.HedgingVO;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import static com.bojiu.webapp.base.entity.Merchant.PLATFORM_ID;
import static com.bojiu.webapp.user.model.MetaType.base_rate_config;
import static com.bojiu.webapp.user.model.UserRedisKey.*;

@Slf4j
@RestController
@RequestMapping("/bet")
public class BetAction extends BaseAction {

	@Resource
	MetaService metaService;
	@Resource
	BetMatchService betMatchService;

	@Ready(value = "全部可用对冲对子", merchantIdRequired = false)
	@GetMapping("/allPair")
	@Permission(Permission.USER)
	public Messager<List<Option<String>>> allPair(ProxyRequest q) {
		User user = q.getSessionUser();
		final Map<String, String> allPair = user.asAllPair() ? BetMatchJob.ALL_PAIR : BetMatchJob.ALL_PAIR2;
		List<Option<String>> options = new ArrayList<>(allPair.size());
		for (Map.Entry<String, String> entry : allPair.entrySet()) {
			options.add(Option.of(entry.getKey(), entry.getValue()));
		}
		return Messager.exposeData(options);
	}

	@Ready(value = "推荐/存档方案列表", merchantIdRequired = false)
	@GetMapping("/list")
	@Permission(Permission.USER)
	public Messager<Page<HedgingVO>> list(ProxyRequest q, HedgingQuery query) {
		boolean searchPlan = Objects.equals(query.type, 1);
		I18N.assertTrue(searchPlan || query.pair != null);
		final Page<HedgingVO> page = q.getPage();
		if (!searchPlan) {
			User user = q.getSessionUser();
			final Map<String, String> allPair = user.asAllPair() ? BetMatchJob.ALL_PAIR : BetMatchJob.ALL_PAIR2;
			I18N.assertTrue(allPair.containsKey(query.pair), UserI18nKey.ILLEGAL_REQUEST);
		}
		final String key = searchPlan ? HEDGING_LIST_KEY : BET_MATCH_DATA_KEY + query.pair;
		final List<Object> scores = RedisUtil.execInPipeline(redisOps -> {
			final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
			opsForZSet.reverseRangeByScore(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE, page.offset(), 10_000);
			opsForZSet.count(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE);
		});
		final Set<String> values = (Set<String>) scores.get(0);
		final List<HedgingVO> vos = new ArrayList<>(1000);
		final long timeNow = new EasyDate(q.now()).addDay(3).getTime(); // 只保留3天以内的赛事
		for (String value : values) {
			final HedgingVO vo = Jsons.parseObject(value, HedgingVO.class);
			if (vo.parlays[vo.parlays.length - 1].gameOpenTime <= timeNow) {
				vos.add(vo);
			}
		}
		final int size = vos.size();
		if (size > 0) {
			// 按照第一场开赛时间升序排列
			if (Cmp.eq(query.sortType, 1)) {
				vos.sort(Comparator.comparingLong(vo -> vo.parlays[0].gameOpenTime));
			}
			page.fromAll(vos);
			if (!searchPlan) { // 已存档的方案无须再进行计算
				final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
				for (HedgingVO vo : page.getList()) { // 只计算分页部分数据的利润
					vo.flush(baseRateConifg);
				}
			}
		} else {
			page.setList(vos);
		}
		page.setTotal(size);
		return Messager.exposeData(page);
	}

	@Ready("保存推荐方案")
	@PostMapping("/save")
	@Permission(Permission.USER)
	public Messager<Void> save(ProxyRequest q, String value) {
		return RedisUtil.fastAttemptInLock(RedisKey.USER_OP_LOCK_PREFIX, () -> {
			final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
			final HedgingDTO dto = HedgingVO.ofAndFlush(value, baseRateConifg);
			if (dto.hasValidId()) {
				final Long id = dto.getId();
				if (!RedisUtil.opsForZSet().rangeByScore(HEDGING_LIST_KEY, id, id).isEmpty()) {
					return Messager.status(Messager.ERROR, "已存档当前方案");
				}
				RedisUtil.doInTransaction(redisOps -> {
					ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
					opsForZSet.removeRange(HEDGING_LIST_KEY, id, id); // 删除旧数据
					opsForZSet.incrementScore(HEDGING_LIST_KEY, value, id); // 添加新数据
				});
			} else {
				RedisUtil.opsForZSet().incrementScore(HEDGING_LIST_KEY, value, dto.getId());
			}
			return Messager.OK();
		});
	}

	@Ready("删除存档的方案")
	@PostMapping("/del")
	@Permission(Permission.USER)
	public Messager<Void> del(ProxyRequest q, String ids) {
		I18N.assertNotNull(ids);
		final List<Long> idList = StringUtil.splitAsLongList(ids);
		return RedisUtil.fastAttemptInLock(RedisKey.USER_OP_LOCK_PREFIX, () -> {
			RedisUtil.doInTransaction(redisOps -> {
				final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
				for (Long id : idList) {
					opsForZSet.removeRangeByScore(HEDGING_LIST_KEY, id, id);
				}
			});
			return Messager.OK();
		});
	}

	@Ready("计算利润")
	@PostMapping("/calc")
	@Permission(Permission.USER)
	public Messager<HedgingVO> calc(ProxyRequest q, String value) {
		return Messager.exposeData(HedgingVO.ofAndFlush(value));
	}

	@Ready("推演赛果并保存数据")
	@PostMapping("/calcSave")
	@Permission(Permission.USER)
	public Messager<HedgingVO> calcSave(ProxyRequest q, String value) {
		I18N.assertNotNull(value);
		final HedgingVO vo = HedgingVO.ofAndInfer(value, q.now());
		final Long id = vo.getId();
		final String newValue = Jsons.encode(vo);
		RedisUtil.doInTransaction(redisOps -> {
			ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
			opsForZSet.removeRangeByScore(HEDGING_LIST_KEY, id, id); // 删除旧数据
			opsForZSet.add(HEDGING_LIST_KEY, newValue, id); // 更新数据
		});
		return Messager.exposeData(vo);
	}

	@Ready("多平台收益对比")
	@PostMapping("/compare")
	@Permission(Permission.USER)
	public Messager<HedgingVO> compare(ProxyRequest q, String value) {
		I18N.assertNotNull(value);
		return Messager.exposeData(betMatchService.calcMuti(value, q.now()));
	}

	// TODO: 2026/1/23 允许修改串子赔率
	// TODO: 2026/1/23 调整为只拿当天的赛事组串子
}
