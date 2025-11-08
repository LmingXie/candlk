package com.bojiu.context.model;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.brand.Feature;
import com.bojiu.context.brand.FeatureContext;
import lombok.Getter;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

@Getter
public enum TaskType implements LabelI18nProxy<TaskType, Integer> {
	/** 新人福利 */
	NEWCOMER(0, TASK_NEWCOMER),
	/** 每日任务 */
	DAY(1, TASK_DAY),
	/** 每周任务 */
	WEEK(2, TASK_WEEK),
	/** 活跃度宝箱 */
	ACTIVE_BOX(3, TASK_ACTIVE_BOX),
	;

	public final Integer value;
	final ValueProxy<TaskType, Integer> proxy;

	TaskType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final TaskType[] CACHE = values();
	public static final TaskType[] CACHE_SHOW = { NEWCOMER, DAY, WEEK };

	public static TaskType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

	public static TaskType ofShow(@Nullable Integer value) {
		return Common.getEnum(CACHE_SHOW, TaskType::getValue, value);
	}

	@Nonnull
	public static Set<TaskType> loadFeatures(FeatureContext context) {
		return context.toSet(Feature.Task, Integer.class, TaskType::of);
	}

	@Nullable
	public static TaskType of(String name) {
		for (TaskType taskType : CACHE) {
			if (taskType.name().equals(name)) {
				return taskType;
			}
		}
		return null;
	}

}