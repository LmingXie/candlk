
package com.bojiu.webapp.user.utils;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class StringToLongUtil {

	/** 基于 CRC32 -> Long */
	public static long stringToLong(String text) {
		if (text == null) {
			return 0L;
		}
		CRC32 crc32 = new CRC32();
		crc32.update(text.getBytes(StandardCharsets.UTF_8));
		return crc32.getValue(); // 32bit → 内含在 long 中
	}

}
