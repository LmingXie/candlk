package com.bojiu.context.model;

import java.util.*;

import com.bojiu.common.context.Env;
import com.bojiu.common.util.Formats;
import me.codeplayer.util.*;
import org.jspecify.annotations.Nullable;

public abstract class IdWorker {

	/**
	 * 返回以毫秒为单位的当前时间
	 *
	 * @return 当前时间(毫秒)
	 */
	protected long nowInMs() {
		return System.currentTimeMillis();
	}

	/**
	 * 将实际的业务ID集合转为对应的 分片键 集合
	 */
	protected static Set<Long> toPartitionids(@Nullable Collection<Long> bizIds, final long mask) {
		final int size = X.size(bizIds);
		if (size == 0) {
			return Collections.emptySet();
		}
		if (size == 1) {
			return Collections.singleton(CollectionUtil.getAny(bizIds) & mask);
		}
		final Set<Long> set = new HashSet<>(4, 1F);
		for (Long bizId : bizIds) {
			set.add(bizId & mask);
		}
		return set;
	}

	/**
	 * 获取添加表分区的 SQL 语句
	 */
	public static String addDailyRangePartitionFor(String tableName, EasyDate beginDate, int days) {
		/*
		ALTER  TABLE `gs_game_play_log_new` ADD PARTITION (
		    PARTITION P20240326 VALUES LESS THAN(UNIX_TIMESTAMP('2024-03-27') )
		    ,PARTITION P20240327 VALUES LESS THAN(UNIX_TIMESTAMP('2024-03-28') )
		);
		*/
		final long rawTime = beginDate.getTime();
		final StringBuilder ddl = new StringBuilder(54 + 70 * days).append("ALTER  TABLE `").append(tableName).append("` ADD PARTITION (");
		for (int i = 0; i < days; i++) {
			ddl.append(i == 0 ? "\n\t" : "\n\t,");
			ddl.append("PARTITION P").append(Formats.getYyyyMMdd(beginDate)).append(" VALUES LESS THAN ( UNIX_TIMESTAMP('").append(beginDate.addDay(1).toString()).append("') )");
		}
		ddl.append("\n);\n");
		beginDate.setTime(rawTime);
		return ddl.toString();
	}

	/**
	 * 获取删除表分区的 SQL 语句
	 */
	public static String dropDailyRangePartitionFor(String tableName, EasyDate beginDate, int days) {
		/*
		ALTER TABLE table_name DROP PARTITION partition_name[, partition_name ...];
		*/
		Assert.isTrue(days > 0);
		if (Env.inProduction()) {
			Assert.isTrue(beginDate.getTime() + EasyDate.MILLIS_OF_DAY * (90 + days) < System.currentTimeMillis());
		}
		final long rawTime = beginDate.getTime();
		final StringBuilder sql = new StringBuilder(50 + days * 11);
		sql.append("ALTER TABLE `").append(tableName).append("` DROP PARTITION ");
		for (int i = 0; i < days; i++) {
			sql.append(i == 0 ? "" : ", ").append("P").append(Formats.getYyyyMMdd(beginDate));
			beginDate.addDay(1);
		}
		sql.append(";");
		beginDate.setTime(rawTime);
		return sql.toString();
	}

	/**
	 * 获取删除表分区的 SQL 语句
	 */
	public static String dropDailyRangePartitionFor(String tableName, EasyDate beginDate, Collection<String> names) {
		if (!X.isValid(names)) {
			return null;
		}
		/*
		ALTER TABLE table_name DROP PARTITION partition_name[, partition_name ...];
		*/
		final long rawTime = beginDate.getTime();
		final int size = names.size();
		if (Env.inProduction()) {
			Assert.isTrue(beginDate.getTime() + EasyDate.MILLIS_OF_DAY * (90 + size) < System.currentTimeMillis());
		}
		final StringBuilder sql = new StringBuilder(50 + size * 11);
		sql.append("ALTER TABLE `").append(tableName).append("` DROP PARTITION ");
		for (String name : names) {
			sql.append(sql.isEmpty() ? "" : ", ").append(name);
			beginDate.addDay(1);
		}
		sql.append(";");
		beginDate.setTime(rawTime);
		return sql.toString();
	}

}