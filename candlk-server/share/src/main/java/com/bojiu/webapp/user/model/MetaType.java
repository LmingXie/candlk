package com.bojiu.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.context.Context;
import com.bojiu.common.context.Env;
import com.bojiu.common.model.*;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.MetaValue;
import lombok.Getter;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.*;
import org.jspecify.annotations.Nullable;

import static com.bojiu.webapp.base.service.RemoteSyncService.*;

/**
 * 元数据 类型 枚举
 */
@SuppressWarnings({ "rawtypes" })
@Getter
public enum MetaType implements ValueProxyImpl<MetaType, Integer>, Visible {

	game(0, "游戏配置", State.INTERNAL, false, false, GAME),
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
	/** 是否按用户隔离 */
	public final boolean isolationUser;

	/**
	 * @param state PUBLIC=所有商户公开可见；PROTECTED=商户级（只能看商户自己的）；INTERNAL=平台级；PRIVATE=均不可见
	 */
	MetaType(Integer value, String label, State state, boolean initCopyFromGlobal, boolean isolationUser, Class<? extends MetaValue> configClazz, String... cacheInServices) {
		this.value = value;
		this.initCopyFromGlobal = initCopyFromGlobal;
		this.configClazz = configClazz;
		this.proxy = new ValueProxy<>(this, state, value, label);
		this.cacheInServices = cacheInServices;
		this.isolationUser = isolationUser;
	}

	MetaType(Integer value, String label, State state, boolean initCopyFromGlobal, boolean isolationUser, String... cacheInServices) {
		this(value, label, state, initCopyFromGlobal, isolationUser, null, cacheInServices);
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