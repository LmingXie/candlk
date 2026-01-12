package com.bojiu.webapp.user.dto;

import java.util.Date;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/** 赛果 */
@Getter
@Setter
public class ScoreResult {

	/** 联赛名称（用户去重） */
	@Nullable
	public String leagueName;
	/** 主队名称 */
	public String teamHome;
	/** 客队名称 */
	public String teamClient;
	/** 开赛时间 */
	@Nullable
	public Date openTime;
	/** 全场进球 [主队,客队] */
	public Integer[] score;
	/** 上半场进球[主队,客队] */
	@Nullable
	public Integer[] scoreH;

	@Override
	public int hashCode() {
		return Objects.hash(leagueName, teamHome, teamClient, openTime);
	}

	@Override
	public boolean equals(Object o) { // 累计充值金额=阶梯，必须唯一
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ScoreResult o2 = (ScoreResult) o;
		return Objects.equals(leagueName, o2.leagueName) && Objects.equals(teamHome, o2.teamHome)
				&& Objects.equals(teamClient, o2.teamClient) && Objects.equals(openTime, o2.openTime);
	}

}
