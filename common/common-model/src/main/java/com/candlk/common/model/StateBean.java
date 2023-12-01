package com.candlk.common.model;

import javax.annotation.Nullable;

// 如果需要序列化，可以屏蔽掉以下属性的冗余输出
// @JSONType(ignores = { "state", "state_", "enabled", "present", "toggle", "toggle_" })
public interface StateBean {

	State getState();

	void setState(State state);

	default void setStateVal(Integer stateVal) {
		setState(State.of(stateVal));
	}

	default Integer getStateVal() {
		State state = getState();
		return state == null ? null : state.value;
	}

	default boolean isEnabled() {
		return isEnabled(getState());
	}

	default Boolean getToggle() {
		return toggleOutput(getState());
	}

	default void setToggle(@Nullable Boolean enabled) {
		State state = clipInputState(enabled);
		if (state != null) {
			setState(state);
		}
	}

	/**
	 * 指示当前实例是否处于"存在"（未被删除）状态（state > 0）
	 */
	default boolean isPresent() {
		return isPresent(getState());
	}

	static boolean isEnabled(@Nullable State state) {
		return State.PUBLIC == state;
	}

	/**
	 * 指示指定的状态是否处于"存在"（未被删除）状态（state > 0）
	 */
	static boolean isPresent(@Nullable State state) {
		return state != null && state.compareTo(State.PRIVATE) > 0;
	}

	static Status asStatus(State state) {
		return isEnabled(state) ? Status.YES : Status.NO;
	}

	default String getToggle_() {
		Boolean toggle = getToggle();
		return toggle == null ? null : getToggleLabel(toggle);
	}

	default String getToggleLabel(boolean toggle) {
		return Status.of(toggle).getToggleLabel();
	}

	default String getState_() {
		State state = getState();
		if (state != null) {
			return switch (state) {
				case PUBLIC -> "公开";
				case PROTECTED -> "前台用户可见";
				case INTERNAL -> "仅后台可见";
				case PRIVATE -> "已删除";
			};
		}
		return null;
	}

	static String getState_(State state) {
		return isEnabled(state) ? "启用" : "停用";
	}

	@Nullable
	static State clipInputState(@Nullable Boolean toggle) {
		return toggle == null ? null : toggle ? State.PUBLIC : State.INTERNAL;
	}

	@Nullable
	static Boolean toggleOutput(@Nullable State state) {
		return state == null ? null : state == State.PUBLIC;
	}

}
