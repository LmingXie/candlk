package com.bojiu.webapp.user.utils;

import java.util.*;

import org.apache.commons.text.similarity.*;
import org.jspecify.annotations.Nullable;

/**
 * 字符串相似度工具：
 * 适用于英文短文本、人名、赛事名、地名匹配
 */
public class StringSimilarityUtils {

	/** 适合短文本：偏好字符顺序 + 开头更接近得分更高 */
	private static final JaroWinklerDistance JW = new JaroWinklerDistance();
	/** 容错拼写错误，适合找 "Premiers" vs "Premier" */
	private static final LevenshteinDistance LD = LevenshteinDistance.getDefaultInstance();
	/** 长文本情况下更准确，但对词序敏感 */
	private static final CosineDistance CD = new CosineDistance();

	/**
	 * 综合相似度评分 (0.0 ~ 1.0)
	 * 三个算法加权：
	 * JW（开头匹配 & 排列近似）     权重 10%
	 * LD（容错拼写错误）          权重 20%
	 * Cosine（整体相似度）        权重 70%
	 *
	 * @param a 待比较字符串1
	 * @param b 待比较字符串2
	 * @return 相似度分数，越接近1越相似
	 */
	public static double similarity(String a, String b) {
		if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
			return 0;
		}

		// 1）字符顺序+前缀偏好
		final double jw = JW.apply(a, b);

		// 2）编辑距离容错拼写
		final int edit = LD.apply(a, b);
		final double levenshtein = 1 - (double) edit / Math.max(a.length(), b.length());

		// 3）整体语义接近程度
		final double cosine = 1 - CD.apply(a, b);

		// 综合加权
		return jw * 0.10 + levenshtein * 0.20 + cosine * 0.70;
	}

	/**
	 * 匹配所有符合分数的候选（不使用Stream，以最高性能单线程处理）
	 *
	 * @param target 目标字符串
	 * @param minScore 最低命中分数
	 * @param candidates 候选列表
	 * @return 排序后的命中结果列表（最高分在前）
	 */
	public static List<Result> match(String target, double minScore, List<String> candidates) {
		if (target == null || candidates == null || candidates.isEmpty()) {
			return Collections.emptyList();
		}

		// 记录去重
		final Set<String> seen = new HashSet<>();
		final List<Result> results = new ArrayList<>();

		for (String c : candidates) {
			if (c == null || !seen.add(c)) {
				continue; // null 或重复直接跳过
			}

			final double score = similarity(target, c);
			if (score >= minScore) {
				results.add(new Result(c, score));
			}
		}

		// 按分数降序排序
		results.sort((a, b) -> Double.compare(b.score, a.score));
		return results;
	}

	/**
	 * 仅返回单个最佳匹配：
	 * 按分数比较，时间复杂度 O(n)，无排序，最省 CPU
	 *
	 * @param target 目标字符串
	 * @param minScore 最低命中分数
	 * @param candidates 候选列表
	 * @return 命中结果；若无匹配则返回 null
	 */
	@Nullable
	public static String matchBest(String target, double minScore, Set<String> candidates) {
		if (target == null || candidates == null || candidates.isEmpty()) {
			return null;
		}

		String bestValue = null;
		double bestScore = minScore; // 低于这个值的不用考虑

		for (String c : candidates) {
			final double score = similarity(target, c);
			if (score > bestScore) {
				bestScore = score;
				bestValue = c;
			}
		}

		return bestValue;
	}

	/**
	 * 返回结果
	 *
	 * @param value 原始候选文本
	 * @param score 相似度 0.0 ~ 1.0
	 */
	public record Result(String value, double score) {

	}

}
