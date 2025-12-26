package com.bojiu.webapp.user.form.query;

import com.bojiu.webapp.user.model.BetProvider;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HedgingQuery {

	/** 串子平台 */
	public BetProvider a;
	/** 对冲平台 */
	public BetProvider b;

}
