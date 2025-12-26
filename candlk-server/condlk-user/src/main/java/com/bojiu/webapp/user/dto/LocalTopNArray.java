package com.bojiu.webapp.user.dto;

import java.util.Arrays;
import java.util.Comparator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LocalTopNArray {

	/** TopN排名数组 */
	private final HedgingDTO[] topN;
	/** 超容量缓存区 */
	private final HedgingDTO[] tempBuffer;
	/** 超容量缓存区大小 */
	private static final int TEMP_CAPACITY = 10000;
	/** 容忍的初始最低分 */
	private static final double minScoreLimit = -200;
	/** TopN容量 */
	private final int capacity;
	/** 当前TopN容量 */
	private int size = 0,
	/** 当前临时缓存区容量 */
	tempSize = 0;
	/** 计数器 */
	@Getter
	private long counter = 0;

	/** 是否已进入超容量阶段 */
	private boolean isSuperSize = false;

	/** 当前 TopN 的最低分（仅在 isSuperSize=true 时有效） */
	private double minScore = minScoreLimit;

	public LocalTopNArray(int capacity) {
		this.capacity = capacity;
		this.topN = new HedgingDTO[capacity];
		this.tempBuffer = new HedgingDTO[TEMP_CAPACITY];
	}

	public void tryAddAndCounter(HedgingDTO dto) {
		try {
			tryAdd(dto);
		} finally {
			counter++;
		}
	}

	public void tryAdd(HedgingDTO dto) {
		final double score = dto.calcAvgProfitAndCache(dto.getHedgingCoins());
		if (score < minScoreLimit) {
			return;
		}

		// 未满容量阶段：直接追加，不排序
		if (!isSuperSize) {
			topN[size++] = dto;

			if (size == capacity) {
				// 首次满容量：排序并进入超容量模式
				Arrays.sort(topN, Comparator.comparingDouble(o -> o.avgProfit));
				minScore = topN[0].avgProfit;
				isSuperSize = true;
				// log.info("达到额定容量，进入超容量模式：minScore={}", minScore);
			}
			return;
		}

		// 超容量阶段：只缓存可能进入 TopN 的
		if (score <= minScore) {
			return;
		}

		tempBuffer[tempSize++] = dto;

		// tempBuffer 满：合并 + 重新计算 TopN
		if (tempSize == TEMP_CAPACITY) {
			mergeTemp();
		}
	}

	/** 合并 tempBuffer 到 topN，并重算 TopN */
	private void mergeTemp() {
		// 合并到一个新数组
		final HedgingDTO[] merged = new HedgingDTO[capacity + tempSize];
		System.arraycopy(topN, 0, merged, 0, capacity);
		System.arraycopy(tempBuffer, 0, merged, capacity, tempSize);

		// 排序
		Arrays.sort(merged, Comparator.comparingDouble(o -> o.avgProfit));

		// 截取 TopN
		System.arraycopy(merged, merged.length - capacity, topN, 0, capacity);

		// 重置最低分和缓冲区容量
		minScore = topN[0].avgProfit;
		tempSize = 0;
		// log.info("合并 tempBuffer 到 topN：minScore={}", minScore);
	}

	transient HedgingDTO[] resultCache;

	/** 取最终 TopN 结果（缓存） */
	public HedgingDTO[] getResult() {
		if (resultCache != null) {
			return resultCache;
		}
		if (!isSuperSize) {
			return resultCache = Arrays.copyOf(topN, size);
		}

		if (tempSize > 0) {
			mergeTemp();
		}
		return resultCache = Arrays.copyOf(topN, capacity);
	}

}