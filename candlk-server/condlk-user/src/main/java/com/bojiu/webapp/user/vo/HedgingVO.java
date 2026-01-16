package com.bojiu.webapp.user.vo;

import java.util.Date;
import java.util.List;

import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.dto.BaseRateConifg;
import com.bojiu.webapp.user.dto.HedgingDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HedgingVO extends HedgingDTO {

	/** 标记是否需要更新 */
	public transient Boolean update;
	/** 当前正在分析赛事索引位置 */
	public Integer nextIdx;
	/** 对比平台赔率信息 */
	public List<HedgingVO> extBOdds;

	public static HedgingVO ofAndFlush(String value) {
		return ofAndFlush(value, null);
	}

	public static HedgingVO ofAndFlush(String value, BaseRateConifg baseRateConifg) {
		return ofAndFlush(value, baseRateConifg, true);
	}

	public static HedgingVO ofAndFlush(String value, BaseRateConifg baseRateConifg, boolean isFlush) {
		final HedgingVO vo = Jsons.parseObject(value, HedgingVO.class);
		if (isFlush) {
			vo.flush(baseRateConifg);
		}
		return vo;
	}

	/** 转换并推演结果 */
	public static HedgingVO ofAndInfer(String value, Date now) {
		final HedgingVO vo = Jsons.parseObject(value, HedgingVO.class);
		vo.calcHedgingCoinsLock(now);
		return vo;
	}

	public Double[] getAOdds_() {
		final int len = parlays.length;
		Double[] aOdds = new Double[len];
		for (int i = 0; i < len; i++) {
			aOdds[i] = parlays[i].aOdds.getRates()[parlays[i].parlaysIdx];
		}
		return aOdds;
	}

	public Double[] getBOdds_() {
		final int len = parlays.length;
		Double[] bOdds = new Double[len];
		for (int i = 0; i < len; i++) {
			final Integer parlaysIdx = parlays[i].parlaysIdx;
			bOdds[i] = parlays[i].bOdds.getRates()[parlaysIdx == 0 ? 1 : 0];
		}
		return bOdds;
	}

}
