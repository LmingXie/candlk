package com.bojiu.webapp.user.action;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.web.ProxyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/app")
public class AppAction {

	@GetMapping("/layout")
	@Permission(Permission.NONE)
	public Messager<JSONObject> layout(final ProxyRequest q) {
		return Messager.exposeData(JSONObject.of("layout", "layout"));
	}
	// TODO 查询全部推荐方案

	// TODO: 2025/12/25 更新默认配置接口（更新后需要刷新全部Redis中的推荐方案利润）

	// TODO: 2025/12/25 修改/保存方案到Redis

	// TODO: 2025/12/25 定时刷新保存的方案（可结合变化的赔率刷新）

	// TODO: 2025/12/25 提供赔率计算接口（前端修改后通过此接口重新计算利润以及下一场所需投注）

	// TODO: 2025/12/25 跟踪赛事结果并结算后续场次的奖金

	// TODO: 2025/12/25 前端实现页面展示

}
