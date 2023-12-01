package com.candlk.common.alarm.dingtalk;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionButton implements Serializable {

	String title;
	String actionURL;

}
