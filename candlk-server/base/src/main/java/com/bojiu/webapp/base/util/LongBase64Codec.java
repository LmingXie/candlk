package com.bojiu.webapp.base.util;

import me.codeplayer.util.JavaUtil;

/**
 * 超高性能的 long 类型 Base64 编解码器
 */
public class LongBase64Codec {

	// 标准 Base64 字符表（RFC 4648）
	private static final byte[] ENCODING_TABLE = JavaUtil.STRING_VALUE.apply("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");

	// 反向映射表：char -> index（-1 表示无效）
	private static final byte[] DECODING_TABLE = new byte[128]; // ASCII 范围

	static {
		// 用 -1 标记无效字符，先填充为 -1
		java.util.Arrays.fill(DECODING_TABLE, (byte) -1);
		for (int i = 0; i < ENCODING_TABLE.length; i++) {
			DECODING_TABLE[ENCODING_TABLE[i]] = (byte) i;
		}
	}

	/**
	 * 将无符号 long 编码为 Base64 字符串（无 padding）
	 */
	public static String encode(long value) {
		if (value == 0) {
			return "A"; // 0 -> 'A'
		}

		// 64 位最多需要 ceil(64/6) = 11 个字符
		final byte[] buffer = new byte[11];
		int pos = 11;

		while (value != 0) {
			int remainder = (int) (value & 0x3F); // value % 64
			buffer[--pos] = ENCODING_TABLE[remainder];
			value >>>= 6; // 无符号右移 6 位（相当于除以 64）
		}
		if (pos == 0) {
			return JavaUtil.STRING_CREATOR_JDK11.apply(buffer, JavaUtil.LATIN1);
		}
		return new String(buffer, pos, 11 - pos);
	}

	// 安全解码（使用修复后的表）
	public static long decode(String base64) {
		return decode(base64, 0, base64.length());
	}

	// 安全解码（使用修复后的表）
	public static long decode(String base64, final int start, final int end) {
		final int length = base64.length();
		if (length == 0 || start < 0 || end > length) {
			throw new IllegalArgumentException();
		}
		final byte[] bytes = JavaUtil.STRING_VALUE.apply(base64);
		long result = 0;
		for (int i = start; i < end; i++) {
			final byte c = bytes[i];
			if (c < 0 || DECODING_TABLE[c] == -1) {
				throw new IllegalArgumentException("Invalid Base64 character: " + base64.charAt(i));
			}
			int digit = DECODING_TABLE[c] & 0xFF; // 转为无符号 byte
			result = (result << 6) | digit;
		}
		return result;
	}

}