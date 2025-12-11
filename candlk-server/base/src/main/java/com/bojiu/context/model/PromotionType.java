package com.bojiu.context.model;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.brand.Feature;
import com.bojiu.context.brand.FeatureContext;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

/**
 * 活动类型
 */
@Getter
public enum PromotionType implements LabelI18nProxy<PromotionType, Integer> {
	/** 充值 */
	RECHARGE(1, PROMOTION_TYPE_RECHARGE, true, true, true, 1),
	/** 打码 */
	PLAY(2, PROMOTION_TYPE_PLAY, true, true, true, 2),
	/** 签到 */
	SIGN(3, PROMOTION_TYPE_SIGN, true, true, true, 3),
	/** 救援金 */
	RELIEF(4, PROMOTION_TYPE_RELIEF, true, true, true, 4),
	/** 幸运转盘 */
	TURNTABLE(5, PROMOTION_TYPE_TURNTABLE, false, false, true, 5),
	/** 红包 */
	RED_ENVELOPE(6, PROMOTION_TYPE_RED_ENVELOPE, false, true, true, 6),
	/** 推广 */
	AGENT(7, PROMOTION_TYPE_AGENT, true, true, true, 7),
	/** 新人彩金 */
	REDEEM_CODE(8, PROMOTION_TYPE_REDEEM_CODE, false, true, true, 8),
	/** 自定义活动 */
	CUSTOM(9, PROMOTION_TYPE_CUSTOM, true, true, true, 9),
	/** 闯关邀请活动 */
	CHALLENGE_INVITE(10, PROMOTION_TYPE_CHALLENGE_INVITE, true, true, true, 10),
	/** 闯关打码活动 */
	CHALLENGE_PLAY(11, PROMOTION_TYPE_CHALLENGE_PLAY, true, false, true, 11),
	/** 余额救援金 */
	RELIEF_BALANCE(12, PROMOTION_TYPE_RELIEF_BALANCE, false, true, true, 14),
	/** 排行榜 */
	RANK(13, PROMOTION_TYPE_RANK, true, true, true, 12),
	/** 奖金转盘活动 */
	REWARD_TURNTABLE(14, PROMOTION_TYPE_REWARD_TURNTABLE, false, false, false),
	/** 公积金 */
	DEPOSIT_POOL(15, PROMOTION_TYPE_DEPOSIT_POOL, true, false, false),
	/** 指定新人彩金（指定代理渠道） */
	AGENT_REDEEM_CODE(16, PROMOTION_TYPE_AGENT_REDEEM_CODE, false, false),
	/** 每日转盘 */
	DAILY_TURNTABLE(17, PROMOTION_TYPE_DAILY_TURNTABLE, false, true, true, 13),
	/** 拼团 */
	GROUP(18, PROMOTION_TYPE_GROUP, false, true, true, 16),
	/** 攒金大转盘 */
	GOLD_TURNTABLE(19, PROMOTION_TYPE_GOLD_TURNTABLE, false, true, true, 15),
	/** Jackpot */
	JACKPOT(20, PROMOTION_TYPE_JACKPOT, false, true, true, 17),
	;

	@EnumValue
	public final Integer value;
	/** 展示在代办列表中的活动（用于标记是否可以查询待领取奖励） */
	public final boolean todoPromotion;
	/** 是否显示在下拉列表 */
	public final boolean selectListShow;
	/** 默认是否显示，如果为false，则需要总台授权后商户才能看到该类型 */
	public final boolean defaultShow;
	/** 展示顺序 */
	public final int sort;
	final ValueProxy<PromotionType, Integer> proxy;

	PromotionType(Integer value, String label, boolean todoPromotion) {
		this(value, label, todoPromotion, true);
	}

	PromotionType(Integer value, String label) {
		this(value, label, false, true);
	}

	PromotionType(Integer value, String label, boolean todoPromotion, boolean selectListShow) {
		this(value, label, todoPromotion, selectListShow, true);
	}

	PromotionType(Integer value, String label, boolean todoPromotion, boolean selectListShow, boolean defaultShow) {
		this(value, label, todoPromotion, selectListShow, defaultShow, 0);
	}

	PromotionType(Integer value, String label, boolean todoPromotion, boolean selectListShow, boolean defaultShow, int sort) {
		this.value = value;
		this.todoPromotion = todoPromotion;
		this.selectListShow = selectListShow;
		this.defaultShow = defaultShow;
		this.sort = sort;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final PromotionType[] CACHE = values();
	/** 代办列表中的活动 */
	public static final List<PromotionType> TODO_CACHE = CollectionUtil.filter(Arrays.asList(values()), PromotionType::isTodoPromotion);

	public static PromotionType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

	/** 下拉框选项：排除社区活动 */
	public static final PromotionType[] CACHE_SHOW;
	public static final List<PromotionType> CACHE_LIST = new ArrayList<>();

	/** 商户默认可见的活动类型列表 */
	public static final List<Integer> CACHE_DEF_VISIBLE = new ArrayList<>();

	/** 活动列表：排除社区活动 */
	public static final List<PromotionType> CACHE_NON_SNS = new ArrayList<>();
	/** 风险配置VIP取值不包含的活动列表 */
	public static final List<PromotionType> VIP_SPECIAL_LIST = new ArrayList<>();

	static {
		for (PromotionType p : CACHE) {
			if (p.selectListShow) {
				CACHE_LIST.add(p);
				if (p.isDefaultShow()) {
					CACHE_DEF_VISIBLE.add(p.value);
				}
			}
			CACHE_NON_SNS.add(p);
		}
		CACHE_LIST.sort(Comparator.comparing(p -> p.sort));
		VIP_SPECIAL_LIST.add(CHALLENGE_INVITE);
		VIP_SPECIAL_LIST.add(RELIEF_BALANCE);
		VIP_SPECIAL_LIST.add(RANK);
		CACHE_SHOW = CACHE_LIST.toArray(PromotionType[]::new);
	}

	public boolean validate() {
		return true;
	}

	@Nonnull
	public static Set<PromotionType> loadFeatures(FeatureContext context) {
		return context.toSet(Feature.Activity, Integer.class, PromotionType::of);
	}
}