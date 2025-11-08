package com.bojiu.webapp.base.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 可远程调用的服务类顶层接口
 */
public interface RemoteBaseService<T, K extends Serializable> {

	/**
	 * 根据主键ID获取对应的实体数据
	 */
	T get(K id);

	/**
	 * 根据主键ID集合获取对应的实体数据集合
	 */
	List<T> findByIds(Collection<K> ids);

}