package com.bojiu.webapp.base.entity;

/**
 * 标记 反序列化后 的 元数据值 的实例对象
 */
public interface MetaValue<T extends MetaValue<T>> extends java.io.Serializable {

	/**
	 * 每次添加/修改时，都会先校验，再初始化
	 */
	void validate();

	default void init() {
	}

	default T init(T value) {
		return null;
	}

}