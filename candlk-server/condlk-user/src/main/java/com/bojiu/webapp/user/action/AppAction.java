package com.bojiu.webapp.user.action;

import java.util.*;

import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.web.Page;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.model.RedisKey;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.user.dto.BaseRateConifg;
import com.bojiu.webapp.user.dto.HedgingDTO;
import com.bojiu.webapp.user.entity.Emp;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.form.MetaForm;
import com.bojiu.webapp.user.form.query.HedgingQuery;
import com.bojiu.webapp.user.model.UserRedisKey;
import com.bojiu.webapp.user.service.GlobalCacheSyncService;
import com.bojiu.webapp.user.service.MetaService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.bojiu.webapp.base.entity.Merchant.PLATFORM_ID;
import static com.bojiu.webapp.user.model.MetaType.base_rate_config;
import static com.bojiu.webapp.user.model.UserRedisKey.BET_MATCH_DATA_KEY;
import static com.bojiu.webapp.user.model.UserRedisKey.HEDGING_LIST_KEY;

@Slf4j
@RestController
@RequestMapping("/app")
public class AppAction {

	@Resource
	MetaService metaService;

	@GetMapping("/layout")
	@Permission(Permission.NONE)
	public Messager<JSONObject> layout(final ProxyRequest q) {
		return Messager.exposeData(JSONObject.of("layout", "layout"));
	}

	static final Double DEFAULT_MIN_SCORE = -Double.MAX_VALUE, DEFAULT_MAX_SCORE = Double.MAX_VALUE;

	@Ready(value = "推荐/存档方案列表", merchantIdRequired = false)
	@GetMapping("/list")
	@Permission(Permission.NONE)
	public Messager<Page<HedgingDTO>> list(ProxyRequest q, HedgingQuery query) {
		I18N.assertNotNull(query.a);
		I18N.assertNotNull(query.b);
		final Page<HedgingDTO> page = q.getPage();
		final String key = Objects.equals(query.type, 1) ? HEDGING_LIST_KEY : BET_MATCH_DATA_KEY + query.a.name() + "-" + query.b.name();
		final List<Object> scores = RedisUtil.execInPipeline(redisOps -> {
			final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
			opsForZSet.rangeByScore(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE, page.offset(), page.getSize());
			opsForZSet.count(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE);
		});
		final Set<String> values = (Set<String>) scores.get(0);
		final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
		page.setList(CollectionUtil.toList(values, o -> Jsons.parseObject(o, HedgingDTO.class).flush(baseRateConifg)));
		page.setTotal((Long) scores.get(1));
		return Messager.exposeData(page);
	}

	@Ready("保存推荐方案")
	@PostMapping("/save")
	@Permission(Permission.NONE)
	public Messager<Void> save(ProxyRequest q, String value) {
		return RedisUtil.fastAttemptInLock(RedisKey.USER_OP_LOCK_PREFIX, () -> {
			final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
			final HedgingDTO dto = Jsons.parseObject(value, HedgingDTO.class).flush(baseRateConifg);
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
		return Messager.exposeData(Jsons.parseObject(value, HedgingDTO.class).flush(baseRateConifg));
	}
	// TODO: 2025/12/25 前端实现页面展示

	// TODO: 2025/12/25 定时刷新保存的方案（可结合变化的赔率刷新）

	// TODO: 2025/12/25 跟踪赛事结果并结算后续场次的奖金
	
	// TODO: 2025/12/26 赔率需按ID拆分缓存，避免删除无法同步到的赔率数据

}
