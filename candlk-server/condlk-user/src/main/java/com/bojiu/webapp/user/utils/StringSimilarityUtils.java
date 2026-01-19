package com.bojiu.webapp.user.utils;

import java.util.*;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.similarity.*;
import org.jspecify.annotations.Nullable;

/**
 * 字符串相似度工具：
 * 适用于英文短文本、人名、赛事名、地名匹配
 */
public class StringSimilarityUtils {

	/** 容错拼写错误，适合找 "Premiers" vs "Premier" */
	private static final LevenshteinDistance LD = LevenshteinDistance.getDefaultInstance();
	/** 长文本情况下更准确，但对词序敏感 */
	private static final CosineDistance CD = new CosineDistance();
	/** 跨语言/口音音译 相似度算法 */
	private static final DoubleMetaphone DM = new DoubleMetaphone();

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

		// 1）字符顺序+前缀偏好（此种评分并不高）
		// final double jw = JW.apply(a, b);

		// 2）编辑距离容错拼写
		final int edit = LD.apply(a, b);
		final double levenshtein = 1 - (double) edit / Math.max(a.length(), b.length());

		// 3）整体语义接近程度
		final double cosine = 1 - CD.apply(a, b);

		// 综合加权
		return levenshtein >= 0.7 && cosine < 0.5 ? (levenshtein * 0.7 + cosine * 0.30) : (levenshtein * 0.3 + cosine * 0.70);
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
	 * 发音相似度（DoubleMetaphone + Tokenizer）
	 * 不做 normalize，也不做自定义词映射
	 *
	 * @param s1 字符串1
	 * @param s2 字符串2
	 * @return 0 ~ 1 越接近越相似
	 */
	public static double similarityMetaphone(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return 0;
		}

		final List<String> t1 = tokenize(s1.toLowerCase()), t2 = tokenize(s2.toLowerCase());

		if (t1.isEmpty() || t2.isEmpty()) {
			return 0;
		}

		int match = 0;

		for (String a : t1) {
			final String dmA = DM.encode(a);
			for (String b : t2) {
				if (dmA.equals(DM.encode(b))) {
					match++;
					break;
				}
			}
		}

		final int total = Math.max(t1.size(), t2.size());
		return match * 1.0 / total;
	}

	/** 分词 */
	private static List<String> tokenize(String s) {
		if (s == null || s.isEmpty()) {
			return Collections.emptyList();
		}
		final StringTokenizer tokenizer = new StringTokenizer(s);
		final List<String> tokens = new ArrayList<>();
		while (tokenizer.hasNext()) {
			tokens.add(tokenizer.next());
		}
		return tokens;
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
