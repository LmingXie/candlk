package com.candlk.common.alarm.dingtalk;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionCard implements Serializable {

	/** 标题 */
	String title;
	/** 内容 */
	String text;
	/** 按钮方向 */
	Integer btnOrientation;
	/** 操作按钮集合 */
	List<ActionButton> btns;

}
