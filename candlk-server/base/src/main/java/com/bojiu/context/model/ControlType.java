package com.bojiu.context.model;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.*;

/**
 * 玩家产出/玩家投入=实际RTP
 */
@Getter
public enum ControlType implements ValueProxyImpl<ControlType, Integer> {
	L0(0, null, "1-2%", false, 1),
	L1(1, 1, "5-15%", false, 10),
	L2(2, null, "15-25%", false, 20),
	L3(3, 2, "25-35%", false, 30),
	L4(4, null, "35-45%", false, 40),
	L5(5, 3, "50%", false, 50),
	L6(6, null, "60%", false, 60),
	L7(7, 4, "70%", false, 70),
	L8(8, 5, "87%", false, 85), // 实际 PGC=87；BPG=80±5
	L9(9, 6, "92%", false, 90), // 实际 PGC=92；BPG=90±5
	L10(10, 7, "94%", false, 94), // 94%RTP 正常池子，HBO 96 RTP
	L11(11, null, "95%", false, 95), // HBO 95%
	L12(12, null, "95.5%", false, 95), // HBO 95.5%
	NONE(50, 0, "96%", false, 96), // 96%RTP 正常池子，不展示，商户端不可配置；HBO 96 RTP
	L13(13, null, "96.5%", false, 97), // HBO 96.5 RTP，默认RTP
	W2(100, 10, "150%", true, 150),
	W5(101, 11, "200%", true, 200),
	W8(102, 12, "300%", true, 300),
	;
	/** 大于该值即归类统计到 控赢打码 */
	private static final int BASE_RTP = 95;

	@EnumValue
	public final Integer value;
	/** PGC真实值 */
	public final Integer pgcVal;
	/** 控赢整数true，控输负数false */
	public final boolean ctrlWin;
	/** 真实RTP */
	public final int rtp;
	final ValueProxy<ControlType, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	ControlType(Integer value, Integer pgcVal, String label, boolean ctrlWin, int rtp) {
		this.value = value;
		this.pgcVal = pgcVal;
		this.proxy = new ValueProxy<>(this, value, label);
		this.ctrlWin = ctrlWin;
		this.rtp = rtp;
	}

	/** PGC全部可用RTP */
	public static final ControlType[] PGC_CACHE;
	/** PGC控输的RTP（前端） */
	public static final ControlType[] PGC_CACHE_FRONT_LOSE;
	/** 数据库配置采用sort写入，计算时使用根据value排序的数组 */
	public static final ControlType[] PGC_CACHE_CALC;

	/** BPG 支持对推广账号进行控赢（只能是200RTP） */
	public static final ControlType[] BPG_CACHE_OUT;
	/** BPG VIP池计算使用的RTP */
	public static final ControlType[] CALC_BPG_CACHE;

	/**
	 * BBGT 只能控输（95%以下）
	 *
	 * @see com.bojiu.webapp.game.dto.CtrlTierCfgDTO#load
	 * @see com.bojiu.webapp.merchant.action.UserSimpleCtrlAction#editVipTierCfg
	 * @see com.bojiu.webapp.merchant.action.UserSimpleCtrlAction#verify
	 */
	public static final ControlType[] BGT_CACHE;
	/**
	 * HBO
	 *
	 * @see com.bojiu.webapp.game.api.impl.HeibaoGameApiImpl#toGameField
	 * @see com.bojiu.webapp.game.dto.CtrlTierCfgDTO#load
	 * @see com.bojiu.webapp.merchant.action.UserSimpleCtrlAction#editVipTierCfg
	 * @see com.bojiu.webapp.merchant.action.UserSimpleCtrlAction#verify
	 * @see com.bojiu.webapp.merchant.service.UserCtrlRuleMerchantService#editVipTierCfg
	 */
	public static final ControlType[] HBO_CACHE;

	static final ControlType[] all = values();
	/**
	 * 控赢的所有枚举值（以英文逗号隔开）
	 * <pre><h4><b>
	 * 【注意】修改此值，必须同步修改 {@link  com.bojiu.webapp.merchant.dao.GamePlayByUserStatHourlyDao } 中的 "ctrl_status > ?" 子句
	 * </b></h4></pre>
	 */
	public static final String CTRL_WIN_VALUES = "50,13,100,101,102"; // > 12 为控赢（95%RTP），12 以下为控输

	public static final List<BigDecimal> pgcDefaultTiers, bpgDefaultTiers, bgtDefaultTiers, hboDefaultTiers;

	static { // 进行排序
		// ！！不可随意增加删除RTP！需同步调整VIP池配置！
		final ControlType[] pgcTypes = new ControlType[] { L5, L7, L8, L9, L10, NONE, W2, W5, W8 };
		Arrays.sort(pgcTypes, Comparator.comparing(ControlType::pgcSort));
		PGC_CACHE = pgcTypes;
		pgcDefaultTiers = parseInitTiers(PGC_CACHE);
		PGC_CACHE_CALC = ArrayUtil.filter(pgcTypes, c -> c != NONE && c.pgcVal != null);
		PGC_CACHE_FRONT_LOSE = ArrayUtil.filter(pgcTypes, c -> !c.ctrlWin && c.pgcVal != null);

		/*初始化BPG可用RTP元数据*/
		ControlType[] bpgTypes = ArrayUtil.filter(all, c -> !c.ctrlWin);
		Arrays.sort(bpgTypes, Comparator.comparing(ControlType::getRtp));
		CALC_BPG_CACHE = bpgTypes;
		bpgTypes = ArrayUtil.filter(all, c -> !c.ctrlWin || c == W5);
		Arrays.sort(bpgTypes, Comparator.comparing(ControlType::getRtp));
		BPG_CACHE_OUT = bpgTypes;
		bpgDefaultTiers = parseInitTiers(bpgTypes);

		/*初始化BBGT可用RTP元数据*/
		BGT_CACHE = ArrayUtil.filter(all, c -> c.rtp <= 95); // BBGT支持 95%以下
		bgtDefaultTiers = parseInitTiers(BGT_CACHE);

		/*初始化HBO可用RTP元数据*/
		HBO_CACHE = new ControlType[] { L10, L11, L12, NONE, L13 };
		hboDefaultTiers = parseInitTiers(HBO_CACHE);

		// 避免枚举值改变
		Assert.isTrue(CTRL_WIN_VALUES.equals(Common.join(Common.toList(Arrays.asList(all), c -> c.asCalcWin() ? c.value : null, false), ",")));
	}

	private static List<BigDecimal> parseInitTiers(ControlType[] bpgTypes) {
		return Stream.concat(Stream.of(BigDecimal.ONE), Stream.generate(() -> BigDecimal.ZERO).limit(bpgTypes.length - 1)).toList();
	}

	public boolean asCalcWin() {
		return rtp > BASE_RTP;
	}

	/** 用于 PGC 控制类型的显示排序 */
	private int pgcSort() {
		return this == NONE ? 0 : rtp;
	}

	public static List<Integer> ctrlStatusListFor(boolean upOrDown) {
		final List<Integer> list = new ArrayList<>(6);
		for (ControlType type : all) {
			if (upOrDown == type.asCalcWin()) {
				list.add(type.value);
			}
		}
		return list;
	}

	public static ControlType of(@Nullable Integer value) {
		return L0.getValueOf(value);
	}

	/** 将本地值装换为PGC值 */
	public static Integer convertToPgcVal(@Nullable ControlType type) {
		return type == null ? null : type.pgcVal;
	}

	/** 将本地值装换为PGC值 */
	public static Integer convertToPgcVal(@Nullable Integer type) {
		return convertToPgcVal(of(type));
	}

	public static String convertRtp(Integer value) {
		return of(value).asCalcWin() ? "RTP>100" : "RTP<100";
	}

	/** 采用增量更新方式（单用户唯一，批量操作可以使用相同ID） */
	public static Long generateTaskId(String gameProviderName) {
		return RedisUtil.opsForValue().increment(
				(gameProviderName == null || gameProviderName.equals("PGC")) ? RedisKey.GAME_CTRL_TASK_ID_COUNTER : RedisKey.GAME_CTRL_TASK_ID_COUNTER + "_" + gameProviderName, 1L
		); // key自增后的值。
	}

}