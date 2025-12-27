package com.bojiu.webapp.user.action;

import java.util.*;
import javax.annotation.Resource;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.web.Page;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.model.RedisKey;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.user.dto.BaseRateConifg;
import com.bojiu.webapp.user.dto.HedgingDTO;
import com.bojiu.webapp.user.form.query.HedgingQuery;
import com.bojiu.webapp.user.service.MetaService;
import com.bojiu.webapp.user.vo.HedgingVO;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import static com.bojiu.webapp.base.entity.Merchant.PLATFORM_ID;
import static com.bojiu.webapp.user.model.MetaType.base_rate_config;
import static com.bojiu.webapp.user.model.UserRedisKey.BET_MATCH_DATA_KEY;
import static com.bojiu.webapp.user.model.UserRedisKey.HEDGING_LIST_KEY;

@Slf4j
@RestController
@RequestMapping("/bet")
public class BetAction {

	@Resource
	MetaService metaService;

	static final Double DEFAULT_MIN_SCORE = -Double.MAX_VALUE, DEFAULT_MAX_SCORE = Double.MAX_VALUE;

	@Ready(value = "推荐/存档方案列表", merchantIdRequired = false)
	@GetMapping("/list")
	@Permission(Permission.NONE)
	public Messager<Page<HedgingDTO>> list(ProxyRequest q, HedgingQuery query) {
		boolean searchAll = Objects.equals(query.type, 1);
		I18N.assertTrue(searchAll || query.pair != null);
		final Page<HedgingDTO> page = q.getPage();
		final String key = searchAll ? HEDGING_LIST_KEY : BET_MATCH_DATA_KEY + query.pair;
		final List<Object> scores = RedisUtil.execInPipeline(redisOps -> {
			final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
			opsForZSet.rangeByScore(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE, page.offset(), page.getSize());
			opsForZSet.count(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE);
		});
		final Set<String> values = (Set<String>) scores.get(0);
		final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
		page.setList(CollectionUtil.toList(values, o -> toVO(o, baseRateConifg)));
		page.setTotal((Long) scores.get(1));
		return Messager.exposeData(page);
	}

	@Ready("保存推荐方案")
	@PostMapping("/save")
	@Permission(Permission.NONE)
	public Messager<Void> save(ProxyRequest q, String value) {
		return RedisUtil.fastAttemptInLock(RedisKey.USER_OP_LOCK_PREFIX, () -> {
			final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
			final HedgingDTO dto = toVO(value, baseRateConifg);
			if (dto.hasValidId()) {
				RedisUtil.doInTransaction(redisOps -> {
					ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
					final Long id = dto.getId();
					opsForZSet.removeRange(HEDGING_LIST_KEY, id, id); // 删除旧数据
					opsForZSet.incrementScore(HEDGING_LIST_KEY, value, id); // 添加新数据
				});
			} else {
				RedisUtil.opsForZSet().incrementScore(HEDGING_LIST_KEY, value, dto.getId());
			}
			return Messager.OK();
		});
	}

	@Ready("计算利润")
	@PostMapping("/calc")
	@Permission(Permission.NONE)
	public Messager<HedgingDTO> calc(ProxyRequest q, String value) {
		final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
		return Messager.exposeData(toVO(value, baseRateConifg));
	}

	private static HedgingDTO toVO(String value, BaseRateConifg baseRateConifg) {
		return Jsons.parseObject(value, HedgingVO.class).flush(baseRateConifg);
	}

	// TODO: 2025/12/25 定时刷新保存的方案（可结合变化的赔率刷新）

	// TODO: 2025/12/25 跟踪赛事结果并结算后续场次的奖金

	// TODO: 2025/12/26 赔率需按ID拆分缓存，避免删除无法同步到的赔率数据

}
