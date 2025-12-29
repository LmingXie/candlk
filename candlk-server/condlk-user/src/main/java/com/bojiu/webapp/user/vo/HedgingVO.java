package com.bojiu.webapp.user.vo;

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

	public double[] getHedging() {
		return hedgingCoins;
	}

	public static HedgingVO of(String value) {
		return of(value, null);
	}

	public static HedgingVO of(String value, BaseRateConifg baseRateConifg) {
		HedgingVO vo = Jsons.parseObject(value, HedgingVO.class);
		vo.flush(baseRateConifg);
		return vo;
	}

}
