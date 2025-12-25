package com.bojiu.context.model;

import java.util.Map;

import org.jspecify.annotations.Nullable;

public interface EmbedObj {

	default void setUser(Map<String, Object> user) {

	}

	default Map<String, Object> getUser() {
		return null;
	}

	default Long userId() {
		return mapId(getUser());
	}

	default String userName() {
		return mapName(getUser());
	}

	default void setEmp(Map<String, Object> emp) {

	}

	default Map<String, Object> getEmp() {
		return null;
	}

	default Long empId() {
		return mapId(getEmp());
	}

	default String empName() {
		return mapName(getEmp());
	}

	default void setMerchant(Map<String, Object> merchant) {

	}

	default void setGroup(Map<String, Object> group) {

	}

	default void setBrand(Map<String, Object> brand) {

	}

	default Map<String, Object> getMerchant() {
		return null;
	}

	default Map<String, Object> getGroup() {
		return null;
	}

	default Map<String, Object> getBrand() {
		return null;
	}

	default Long merchantId() {
		return mapId(getMerchant());
	}

	default Long groupId() {
		return mapId(getGroup());
	}

	default String merchantName() {
		return mapName(getMerchant());
	}

	default String groupName() {
		return mapName(getGroup());
	}

	default Long brandId() {
		return mapId(getBrand());
	}

	default String brandName() {
		return mapName(getBrand());
	}

	private static Long mapId(@Nullable Map<String, Object> map) {
		return map == null ? null : (Long) map.get("id");
	}

	static String mapName(@Nullable Map<String, Object> map) {
		return map == null ? null : (String) map.get("name");
	}

}