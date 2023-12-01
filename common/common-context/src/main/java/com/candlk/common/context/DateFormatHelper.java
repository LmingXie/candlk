package com.candlk.common.context;

import java.text.ParseException;
import java.util.Date;
import javax.annotation.Nullable;

import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.time.FastDateFormat;

public interface DateFormatHelper {

	default DateFormatHelper getDateFormatHelper() {
		return DefaultDateFormatHelper.INSTANCE;
	}

	default FastDateFormat getDateFormat(String pattern) {
		return getDateFormatHelper().getDateFormat(pattern);
	}

	default Date smartParseDate(String dateStr, boolean errorAsNull) {
		return getDateFormatHelper().smartParseDate(dateStr, errorAsNull);
	}

	default Date smartParseDate(String source) {
		return getDateFormatHelper().smartParseDate(source);
	}

	default Date parseDate(String source, String pattern) {
		return getDateFormatHelper().parseDate(source, pattern);
	}

	default String formatDate(Date date, String pattern) {
		return getDateFormatHelper().formatDate(date, pattern);
	}

	class DefaultDateFormatHelper implements DateFormatHelper {

		public static DefaultDateFormatHelper INSTANCE = new DefaultDateFormatHelper();

		@Override
		public FastDateFormat getDateFormat(String pattern) {
			return FastDateFormat.getInstance(pattern);
		}

		@Override
		public Date smartParseDate(String source, boolean errorAsNull) {
			if (errorAsNull && StringUtil.isEmpty(source)) {
				return null;
			}
			int len = source.length();
			String pattern;
			switch (len) {
				case 6 -> pattern = "yyyyMM";
				case 7 -> pattern = "yyyy-MM";
				case 8 -> pattern = "yyyyMMdd";
				case 10 -> pattern = source.charAt(4) == '-' ? "yyyy-MM-dd" : "yyyy/MM/dd";
				case 11 -> pattern = "yyyy年MM月dd日";
				case 13 -> {
					Long val = NumberUtil.getLong(source, null); // 兼容时间戳
					if (val != null) {
						return new Date(val);
					}
					pattern = "yyyy-MM-dd HH";
				}
				case 16 -> pattern = "yyyy-MM-dd HH:mm";
				case 17 -> pattern = "yyyy年MM月dd日HH时mm分";
				case 19 -> pattern = "yyyy-MM-dd HH:mm:ss";
				case 23 -> pattern = "yyyy-MM-dd HH:mm:ss.SSS";
				default -> {
					return handleError(source, errorAsNull, null);
				}
			}
			FastDateFormat format = getDateFormat(pattern);
			try {
				return format.parse(source);
			} catch (ParseException e) {
				return handleError(source, errorAsNull, e);
			}
		}

		protected Date handleError(String source, boolean errorAsNull, @Nullable Throwable cause) {
			if (errorAsNull) {
				return null;
			}
			throw wrapEx(source, cause);
		}

		protected IllegalArgumentException wrapEx(String source, @Nullable Throwable cause) {
			return new IllegalArgumentException("Invalid request parameters：" + source, cause);
		}

		@Override
		public Date smartParseDate(String source) {
			return smartParseDate(source, false);
		}

		@Override
		public Date parseDate(String source, String pattern) {
			try {
				return getDateFormat(pattern).parse(source);
			} catch (ParseException e) {
				throw wrapEx(source, e);
			}
		}

		@Override
		public String formatDate(Date date, String pattern) {
			return getDateFormat(pattern).format(date);
		}

	}

}
