package com.bojiu.webapp.user.model;

import java.util.*;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.context.Context;
import com.bojiu.common.context.Env;
import com.bojiu.common.model.*;
import com.bojiu.common.util.Common;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.MetaValue;
import lombok.Getter;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.*;

import static com.bojiu.webapp.base.service.RemoteSyncService.*;

/**
 * 元数据 类型 枚举
 */
@SuppressWarnings({ "JavadocReference", "rawtypes" })
@Getter
public enum MetaType implements ValueProxyImpl<MetaType, Integer>, Visible {

	@Deprecated
	game(0, "游戏配置", State.INTERNAL, false, true, GAME),
	/**
	 * 推送机器人配置
	 *
	 * @see com.bojiu.webapp.admin.dto.PaymentConfigDTO 仅限商户ID = 0
	 */
	bot_config(1, "推送机器人配置", State.PROTECTED, false, false, USER),
	/** 全局账号配置 */
	global_user_config(2, "全局账号配置", State.PROTECTED, true, false, USER),
	/** 数据库升级标记 */
	db_upgrade(3, "全局账号配置", State.PROTECTED, true, false, USER),
	/** 剧本聊天机器人配置 */
	larp_bot_config(4, "剧本聊天机器人配置", State.PROTECTED, true, false, USER),
	;

	@EnumValue
	public final Integer value;
	final ValueProxy<MetaType, Integer> proxy;
	/** 如果为 true，添加新的商户时，会从 平台（ merchantId = 0） 复制一份模板配置到 商户元数据 */
	public final boolean initCopyFromGlobal;
	/** 该元数据类型的 数据值 所对应的实体类型 */
	public final Class<? extends MetaValue> configClazz;
	/** 使用缓存的 微服务 名称数组（不在该数组中的微服务，调用获取缓存会直接报错） */
	public final String[] cacheInServices;
	/** 复制指定商户配置，默认 false=不复制，true=复制 */
	public final boolean copySpecifiedConfig;

	/**
	 * @param state PUBLIC=所有商户公开可见；PROTECTED=商户级（只能看商户自己的）；INTERNAL=平台级；PRIVATE=均不可见
	 */
	MetaType(Integer value, String label, State state, boolean initCopyFromGlobal, boolean copySpecifiedConfig, Class<? extends MetaValue> configClazz, String... cacheInServices) {
		this.value = value;
		this.initCopyFromGlobal = initCopyFromGlobal;
		this.configClazz = configClazz;
		this.proxy = new ValueProxy<>(this, state, value, label);
		this.cacheInServices = cacheInServices;
		this.copySpecifiedConfig = copySpecifiedConfig;
	}

	MetaType(Integer value, String label, State state, boolean initCopyFromGlobal, boolean copySpecifiedConfig, String... cacheInServices) {
		this(value, label, state, initCopyFromGlobal, copySpecifiedConfig, null, cacheInServices);
	}

	MetaType(Integer value, String label, State state, boolean initCopyFromGlobal, String... cacheInServices) {
		this(value, label, state, initCopyFromGlobal, false, null, cacheInServices);
	}

	MetaType(Integer value, String label, State state, String... cacheInServices) {
		this(value, label, state, false, false, null, cacheInServices);
	}

	public boolean isEnabled() {
		return proxy.state.value >= State.INTERNAL.value;
	}

	public String serialize(MetaValue config) {
		return config == null ? "" : Jsons.encodeRaw(config);
	}

	@SuppressWarnings("unchecked")
	public <T extends MetaValue> T deserialize(String json) {
		return StringUtil.isEmpty(json) ? null : (T) Jsons.parseObject(json, configClazz);
	}

	public static final MetaType[] CACHE = ValueProxy.getVisibleEnums(MetaType.class, game.proxy.values(), State.INTERNAL);

	public static MetaType of(@Nullable Integer value) {
		return game.getValueOf(value);
	}

	/**
	 * 需要系统初始化的数据
	 */
	public static String valuesForInitCopyFromGlobal() {
		List<Integer> metaTypes = Common.toList(Arrays.asList(game.proxy.values()), t -> t.initCopyFromGlobal ? t.getValue() : null, false);
		return Common.join(metaTypes, ",");
	}

	/**
	 * 复制商户时，剔除指定配置
	 */
	public static List<Integer> valuesForRejectSpecifiedConfig() {
		final List<Integer> result = new ArrayList<>();
		for (MetaType mt : game.proxy.values()) {
			if (!mt.copySpecifiedConfig) {
				result.add(mt.value);
			}
		}
		return result;
	}

	@Nullable
	public static MetaType of(String name) {
		final MetaType type = EnumUtils.getEnum(MetaType.class, name);
		return type != null && type.isEnabled() ? type : null;
	}

	@Override
	public State getState() {
		return proxy.state;
	}

	static String serviceName;

	public boolean allowCache() {
		// 这里如果报错，就表示服务调用者超出了限定范围，可以自行增加范围
		Assert.isTrue(cacheInServices.length > 0);
		if (serviceName == null) {
			String name = StringUtils.substringAfterLast(Context.applicationName(), '-');
			serviceName = switch (name) {
				case TRADE -> TRADE;
				case USER -> USER;
				case GAME -> GAME;
				case ADMIN -> ADMIN;
				default -> "";
			};
		}
		return ALL.equals(cacheInServices[0]) || ArrayUtils.contains(cacheInServices, serviceName);
	}

	public void checkServiceRanges() {
		// 生产环境不用检查，以免浪费性能
		if (!Env.inProduction()) {
			// 这里如果报错，就表示服务调用者超出了限定范围，可以自行增加范围
			Assert.isTrue(allowCache());
		}
	}

}