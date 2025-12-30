package com.bojiu.webapp.user.dto;

import lombok.Getter;
import lombok.Setter;

/** 赛果 */
@Getter
@Setter
public class ScoreResult {

	/** 主队名称 */
	public String teamHome;
	/** 客队名称 */
	public String teamClient;
	/** 全场进球[主队,客队] */
	public Integer[] score;
	/** 上半场进球[主队,客队] */
	public Integer[] scoreH;

}
