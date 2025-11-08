package com.bojiu.context;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.model.LabelI18nProxy;
import lombok.Getter;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.EnumUtils;

/** 应用运营地域 */
@Getter
public enum AppRegion implements LabelI18nProxy<AppRegion, String> {

	/** 巴西 */
	BR(AdminI18nKey.BR),
	/** 亚太 APP_REGION=ASIA;APP_NAMESPACE=nas */
	ASIA(AdminI18nKey.ASIA),
	;

	@EnumValue
	public final String value;
	final ValueProxy<AppRegion, String> proxy;

	AppRegion(String label) {
		this.value = name();
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final AppRegion[] CACHE = values();

	public static AppRegion of(@Nullable String value) {
		return EnumUtils.getEnum(AppRegion.class, value);
	}

	/** 当前应用所属运营地域 */
	public static final AppRegion CURRENT;
	public static final String APP_REGION = "APP_REGION";

	static {
		String region = System.getProperty(APP_REGION); // 允许从 Java System 中获取，便于进行单元测试
		if (StringUtil.isEmpty(region)) {
			region = System.getenv(APP_REGION);
		}
		CURRENT = StringUtil.isEmpty(region)
				? AppRegion.BR
				: AppRegion.valueOf(region); // 需要设置系统环境变量 APP_REGION
	}

	/** 当前是否是在【巴西】地域运营 */
	public static boolean inBr() {
		return CURRENT == BR; // 不用 ==，避免 CURRENT 未初始化
	}

	/** 当前是否是在【亚太】地域运营 */
	public static boolean inAsia() {
		return CURRENT == ASIA;
	}

	/** 断言 应用名称前缀 与 当前地域 匹配，否则抛出异常（即 启动失败） */
	public void assertPrefixMatch(String applicationName, String namespace) {
		final String appPrefix, namespacePrefix;
		switch (this) {
			case BR -> {
				appPrefix = "nbr-";
				namespacePrefix = "nbr_";
			}
			case ASIA -> {
				appPrefix = "nas-";
				namespacePrefix = "nas_";
			}
			default -> throw new IllegalStateException("Unknown region");
		}
		Assert.state(applicationName.startsWith(appPrefix)/* && namespace.startsWith(namespacePrefix)*/); // TODO: 2025/11/8 暂时不支持多服务
	}

}