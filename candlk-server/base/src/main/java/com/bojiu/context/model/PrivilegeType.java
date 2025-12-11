package com.bojiu.context.model;

import java.util.*;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.TimeInterval;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.UserModelI18nKey;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.EasyDate;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

@Getter
public enum PrivilegeType implements LabelI18nProxy<PrivilegeType, Integer> {

	/** 每日转盘 */
	TURNTABLE(1, PRIVILEGE_TYPE_TURNTABLE, Calendar.DATE, true),
	/** 每日闯关游戏 */
	BREAKING_GAME(2, PRIVILEGE_TYPE_BREAKING_GAME, Calendar.DATE, true),
	/** 生日奖励 */
	BIRTHDAY_REWARDS(3, PRIVILEGE_TYPE_BIRTHDAY_REWARDS, Calendar.DATE, true),
	/** 升级奖励 */
	UPGRADE_REWARDS(4, PRIVILEGE_TYPE_UPGRADE_REWARDS, Calendar.DATE, true),
	/** 每周奖励 */
	WEEKLY_REWARDS(5, PRIVILEGE_TYPE_WEEKLY_REWARDS, Calendar.DAY_OF_WEEK, true),
	/** 每月奖励 */
	MONTH_REWARDS(6, PRIVILEGE_TYPE_MONTH_REWARDS, Calendar.MONTH, true),
	/** 日提款限额 */
	DAILY_CASH_LIMIT(7, PRIVILEGE_TYPE_CASH_LIMIT, Calendar.DATE),
	/** 日提款次数 */
	DAILY_CASH_NUM(8, PRIVILEGE_TYPE_DAILY_CASH, Calendar.DATE),
	/** 每日奖励 */
	DAILY_REWARDS(9, PRIVILEGE_TYPE_DAILY_REWARDS, Calendar.DATE, true),
	/** 贵宾特权 */
	PRIVILEGE(10, PRIVILEGE_TYPE_VIP, Calendar.DATE),
	/** 保级 */
	RELEGATION(11, PRIVILEGE_TYPE_RELEGATION, Calendar.MONTH),
	/** 日免手续费次数 */
	DAILY_FEE_NUM(12, PRIVILEGE_TYPE_DAILY_FEE_NUM, Calendar.DATE),
	/** 总提款限额 */
	TOTAL_CASH_LIMIT(13, PRIVILEGE_TYPE_TOTAL_CASH_LIMIT, Calendar.YEAR),
	/** 总提款次数 */
	TOTAL_CASH_NUM(14, PRIVILEGE_TYPE_TOTAL_CASH_NUM, Calendar.YEAR),
	/** 提现 限制提示语 */
	CASH_LIMIT_TIPS(15, PRIVILEGE_TYPE_CASH_LIMIT_TIPS, Calendar.DATE),
	;

	public boolean rewardType;
	public final Integer value;
	public final int calendarField;
	final ValueProxy<PrivilegeType, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	PrivilegeType(Integer value, String label, int calendarField) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.calendarField = calendarField;
	}

	PrivilegeType(Integer value, String label, int calendarField, boolean rewardType) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.calendarField = calendarField;
		this.rewardType = rewardType;
	}

	public static final PrivilegeType[] CACHE = values();
	/** VIP奖励：升级奖励、日奖励、周奖励、月奖励 */
	public static final PrivilegeType[] VIP_REWARDS = { UPGRADE_REWARDS, DAILY_REWARDS, WEEKLY_REWARDS, MONTH_REWARDS };
	/** VIP特权：日提款限额、日提款次数、日免手续费次数、总提款限额、总提款次数 */
	public static final List<PrivilegeType> VIP_PRIVILEGE = Arrays.asList(DAILY_CASH_LIMIT, DAILY_CASH_NUM, DAILY_FEE_NUM, TOTAL_CASH_LIMIT, TOTAL_CASH_NUM);
	// 获取特权下的奖励类型
	public static final PrivilegeType[] REWARD_CACHE = ArrayUtil.filter(PrivilegeType.values(), PrivilegeType::isRewardType);
	// 获取特权下的奖励类型编号
	public static final List<Integer> REWARD_TYPES = Common.toList(Arrays.asList(REWARD_CACHE), PrivilegeType::getValue);

	public static PrivilegeType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}

	/**
	 * 获取时间范围
	 */
	public TimeInterval getTimeInterval(final EasyDate zonedDate) {
		return TimeInterval.ofFast(zonedDate, calendarField);
	}

	public JSONObject convertTitle() {
		return switch (this) {
			case DAILY_CASH_NUM -> JSONObject.of("title", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_DAILY_CASH_TITLE), "limit", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_LIMIT));
			case DAILY_CASH_LIMIT -> JSONObject.of("title", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_CASH_TITLE), "limit", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_LIMIT));
			case TOTAL_CASH_NUM -> JSONObject.of("title", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_TOTAL_CASH_NUM_TITLE), "limit", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_LIMIT));
			case TOTAL_CASH_LIMIT -> JSONObject.of("title", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_TOTAL_CASH_TITLE), "limit", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_LIMIT));
			case DAILY_FEE_NUM -> JSONObject.of("title", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_DAILY_FEE_NUM_TITLE), "limit", I18N.msg(UserModelI18nKey.PRIVILEGE_TYPE_NUM));
			default -> null;
		};
	}
}