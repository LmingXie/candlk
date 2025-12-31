package com.bojiu.webapp.user.vo;

import java.util.Date;

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

	public static HedgingVO ofAndFlush(String value) {
		return ofAndFlush(value, null);
	}

	public static HedgingVO ofAndFlush(String value, BaseRateConifg baseRateConifg) {
		final HedgingVO vo = Jsons.parseObject(value, HedgingVO.class);
		vo.flush(baseRateConifg);
		return vo;
	}

	/** 转换并推演结果 */
	public static HedgingVO ofAndInfer(String value, Date now) {
		final HedgingVO vo = Jsons.parseObject(value, HedgingVO.class);
		vo.calcHedgingCoinsLock(now);
		return vo;
	}

}
