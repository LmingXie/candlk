package com.candlk.common.dao;

import java.util.Date;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.candlk.common.model.TimeInterval;
import me.codeplayer.util.*;
import org.apache.ibatis.jdbc.SQL;

/**
 * 工具方法类
 */
public abstract class MybatisUtil {

	public static void addSqlInterval(@Nonnull SQL sb, @Nonnull String column, @Nullable TimeInterval interval, @Nullable String prefix) {
		if (interval != null && StringUtil.notEmpty(column)) {
			prefix = X.expectNotNull(prefix, "interval");
			Date begin = interval.getBegin();
			if (begin != null) {
				sb.WHERE(column + " >= #{" + prefix + ".finalBegin}");
			}
			Date end = interval.getEnd();
			if (end != null) {
				sb.WHERE(column + " <= #{" + prefix + ".finalEnd}");
			}
		}
	}

	public static void addSqlIntervalConflict(@Nonnull SQL sb, @Nonnull String addTime, @Nonnull String startColumn, @Nonnull String endColumn, TimeInterval interval, @Nonnull String prefix, @Nonnull String rangePrefix, boolean endTimeExistNull) {
		if (StringUtil.notEmpty(startColumn) && interval != null && interval.getBegin() != null) {
			sb.WHERE(addTime + " >= #{" + rangePrefix + ".finalBegin} AND " + addTime + " <= #{" + rangePrefix + ".finalEnd} ");
			sb.WHERE(endTimeExistNull ? "(!(" + endColumn + " <= #{" + prefix + ".begin} OR " + startColumn + " >= #{" + prefix + ".end}) OR (" + startColumn + " >= #{" + rangePrefix + ".finalBegin} AND " + endColumn + " IS NULL))" : "(!(" + endColumn + " <= #{" + prefix + ".begin} OR " + startColumn + " >= #{" + prefix + ".end}))");
		}
	}

	public static void addSqlIntervalConflict(@Nonnull SQL sb, @Nonnull String startColumn, @Nonnull String endColumn, TimeInterval interval, @Nonnull String prefix, boolean finalInterval) {
		if (StringUtil.notEmpty(startColumn) && interval != null && interval.getBegin() != null) {
			sb.WHERE("(!(" + endColumn + " <= #{" + prefix + (finalInterval ? ".finalBegin} OR " : ".begin} OR ") + startColumn + " >= #{" + prefix + (finalInterval ? ".finalEnd}))" : ".end}))"));
		}
	}

	public static <T> void addIf(@Nonnull SQL sql, @Nonnull String column, @Nonnull SqlKeyword key, @Nullable T value, Predicate<? super T> filter, String valueExpr) {
		if (filter.test(value)) {
			doAddSql(sql, column, key, value, valueExpr);
		}
	}

	public static void addIfHasValue(@Nonnull SQL sql, @Nonnull String column, @Nonnull SqlKeyword key, @Nullable Object value, String valueExpr) {
		if (value == null || value instanceof String str && str.isBlank()) {
			return;
		}
		doAddSql(sql, column, key, value, valueExpr);
	}

	public static void addIfHasValue(@Nonnull SQL sql, @Nonnull String column, @Nonnull SqlKeyword key, @Nullable String value, String valueExpr) {
		if (value == null || value.isBlank()) {
			return;
		}
		doAddSql(sql, column, key, value, valueExpr);
	}

	public static void addEqIfHasValue(@Nonnull SQL sql, @Nonnull String column, @Nullable Object value, String valueExpr) {
		addIfHasValue(sql, column, SqlKeyword.EQ, value, valueExpr);
	}

	public static void addEqIfHasValue(@Nonnull SQL sql, @Nonnull String column, @Nullable String value, String valueExpr) {
		addIfHasValue(sql, column, SqlKeyword.EQ, value, valueExpr);
	}

	public static void addIfNotNull(@Nonnull SQL sql, @Nonnull String column, @Nonnull SqlKeyword key, @Nullable Object obj, String value) {
		if (value != null) {
			doAddSql(sql, column, key, obj, value);
		}
	}

	public static void addInSql(@Nonnull SQL sql, @Nonnull String column, Object array, boolean isInclude, boolean isString) {
		if (X.isValid(array)) {
			sql.WHERE(ArrayUtil.getInSQL(new StringBuilder(column), array, isInclude, isString).toString());
		}
	}

	public static void addInIdSql(@Nonnull SQL sql, @Nonnull String column, Object array) {
		addInSql(sql, column, array, true, false);
	}

	public static String inIdSql(@Nonnull String column, Object array) {
		if (X.isValid(array)) {
			return ArrayUtil.getInSQL(new StringBuilder(column), array, true, false).toString();
		}
		return null;
	}

	public static String inStrSql(@Nonnull String column, Object array) {
		if (X.isValid(array)) {
			return ArrayUtil.getInSQL(new StringBuilder(column), array, true, true).toString();
		}
		return null;
	}

	public static void addInStrSql(@Nonnull SQL sql, @Nonnull String column, Object array) {
		addInSql(sql, column, array, true, true);
	}

	public static String sqlSegment(@Nonnull String column, @Nonnull SqlKeyword key, @Nullable Object value, String valueExpr) {
		return switch (key) {
			case EQ, NE, GT, GE, LT, LE -> column + " " + key.getSqlSegment() + X.expectNotNull(valueExpr, value);
			case LIKE, NOT_LIKE -> column + " " + key.getSqlSegment() + " CONCAT('%', '" + X.expectNotNull(valueExpr, value) + "', '%')";
			case IN, NOT_IN -> column + " " + key.getSqlSegment() + " ('" + X.expectNotNull(valueExpr, value) + "') ";
			case IS_NOT_NULL -> column + " " + key.getSqlSegment();
			default -> throw new UnsupportedOperationException();
		};
	}

	public static void doAddSql(@Nonnull SQL sql, @Nonnull String column, @Nonnull SqlKeyword key, @Nullable Object value, String valueExpr) {
		sql.WHERE(sqlSegment(column, key, value, valueExpr));
	}

	public static void wrapperInterval(@Nonnull AbstractWrapper<?, String, ?> wrapper, @Nullable String column, @Nullable TimeInterval interval) {
		if (interval != null) {
			Date finalBegin = interval.getFinalBegin();
			wrapper.ge(finalBegin != null, column, finalBegin);
			Date finalEnd = interval.getFinalEnd();
			wrapper.le(finalEnd != null, column, finalEnd);
		}
	}

	public static <T> void wrapperInOrEq(@Nonnull QueryWrapper<T> wrapper, @Nullable String column, @Nullable Object[] types) {
		if (types != null) {
			if (X.size(types) == 1) {
				wrapper.eq(X.isValid(types[0]), column, types[0]);
				return;
			}
			wrapper.in(X.isValid(types), column, types);
		}
	}

	public static <T> void wrapperInterval(@Nonnull LambdaQueryWrapper<T> wrapper, SFunction<T, ?> column, @Nullable TimeInterval interval) {
		if (interval != null) {
			Date finalBegin = interval.getFinalBegin();
			wrapper.ge(finalBegin != null, column, finalBegin);
			Date finalEnd = interval.getFinalEnd();
			wrapper.le(finalEnd != null, column, finalEnd);
		}
	}

	/**
	 * 为不支持毫秒级的低精度 MySQL 日期数据类型提供时间范围的查询条件封装
	 */
	public static <T> void wrapperIntervalLowScale(@Nonnull AbstractWrapper<?, String, ?> wrapper, @Nullable String column, @Nullable TimeInterval interval) {
		if (interval != null) {
			Date finalBegin = interval.getFinalBegin();
			wrapper.ge(finalBegin != null, column, finalBegin);
			Date end = interval.getEnd();
			if (end != null) {
				final int endCalendarField = interval.getEndCalendarField();
				if (endCalendarField != -1) {
					end = interval.getEasyDate(end).endOf(endCalendarField).setMillisecond(0).toDate();
				} else {
					final long time = end.getTime(), reminder = time % 1000L;
					if (reminder > 0) {
						end = new Date(time - reminder);
					}
				}
				wrapper.le(true, column, end);
			}
		}
	}

	/**
	 * 临时忽略多租户策略，并执行指定的数据访问任务
	 * 执行完毕后，将恢复原状
	 */
	public static <T> T runWithIgnoreTenant(Supplier<T> task) {
		// 设置忽略租户插件
		InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().tenantLine(true).build());
		try {
			return task.get();
		} finally {
			// 关闭忽略策略
			InterceptorIgnoreHelper.clearIgnoreStrategy();
		}
	}

	/**
	 * 临时忽略多租户策略，并执行指定的数据访问任务
	 * 执行完毕后，将恢复原状
	 */
	public static void runWithIgnoreTenant(Runnable task) {
		// 设置忽略租户插件
		InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().tenantLine(true).build());
		try {
			task.run();
		} finally {
			// 关闭忽略策略
			InterceptorIgnoreHelper.clearIgnoreStrategy();
		}
	}

}
