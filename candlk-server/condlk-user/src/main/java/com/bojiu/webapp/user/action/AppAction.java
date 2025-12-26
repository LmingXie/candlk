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
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.user.dto.BaseRateConifg;
import com.bojiu.webapp.user.dto.HedgingDTO;
import com.bojiu.webapp.user.form.query.HedgingQuery;
import com.bojiu.webapp.user.service.MetaService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import static com.bojiu.webapp.base.entity.Merchant.PLATFORM_ID;
import static com.bojiu.webapp.user.model.MetaType.base_rate_config;
import static com.bojiu.webapp.user.model.UserRedisKey.BET_MATCH_DATA_KEY;

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

	@Ready(value = "推荐方案列表", merchantIdRequired = false)
	@GetMapping("/list")
	@Permission(Permission.NONE)
	public Messager<Page<HedgingDTO>> list(ProxyRequest q, HedgingQuery query) {
		I18N.assertNotNull(query.a);
		I18N.assertNotNull(query.b);
		final Page<HedgingDTO> page = q.getPage();
		final String key = BET_MATCH_DATA_KEY + query.a.name() + "-" + query.b.name();
		final List<Object> scores = RedisUtil.execInPipeline(redisOps -> {
			ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
			opsForZSet.rangeByScore(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE, page.offset(), page.getSize());
			opsForZSet.count(key, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE);
		});
		final Set<String> values = (Set<String>) scores.get(0);
		final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
		page.setList(CollectionUtil.toList(values, o -> Jsons.parseObject(o, HedgingDTO.class).flush(baseRateConifg)));
		page.setTotal((Long) scores.get(1));
		return Messager.exposeData(page);
	}

	// TODO: 2025/12/25 修改/保存方案到Redis

	// TODO: 2025/12/25 定时刷新保存的方案（可结合变化的赔率刷新）

	// TODO: 2025/12/25 提供赔率计算接口（前端修改后通过此接口重新计算利润以及下一场所需投注）

	// TODO: 2025/12/25 跟踪赛事结果并结算后续场次的奖金

	// TODO: 2025/12/25 前端实现页面展示

}
