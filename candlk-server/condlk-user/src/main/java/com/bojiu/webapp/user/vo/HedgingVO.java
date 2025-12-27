package com.bojiu.webapp.user.vo;

import com.bojiu.webapp.user.dto.HedgingDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HedgingVO extends HedgingDTO {

	public double[] getHedging() {
		return hedgingCoins;
	}

}
