package com.candlk.context.model;

import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;

/**
 * 成员操作 枚举类
 */
@Getter
public enum Operation implements ValueProxyImpl<Operation, Integer> {

	/** 添加 */
	ADD(0, "添加"),
	/** 编辑 */
	EDIT(1, "编辑"),
	/** 删除 */
	DELETE(2, "删除"),
	/** 审核 */
	AUDIT(3, "审核"),
	/** 授权 */
	GRANT(4, "授权"),
	/** 启用/停用 */
	TOGGLE(5, "启用/停用"),
	/** 分享 */
	SHARE(6, "分享"),
	/** 收藏/喜欢 */
	LIKE(7, "收藏"),
	/** 取消收藏/喜欢 */
	UNLIKE(8, "取消收藏"),
	/** 取消 */
	CANCEL(9, "取消");
	//
	;

	// 定义私有变量
	public final Integer value;
	final ValueProxy<Operation, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	Operation(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final Operation[] CACHE = values();

	public static Operation of(Integer value) {
		return ADD.getValueOf(value);
	}

}
