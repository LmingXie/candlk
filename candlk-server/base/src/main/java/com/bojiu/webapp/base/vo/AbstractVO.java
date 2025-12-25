package com.bojiu.webapp.base.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Status;
import com.bojiu.common.model.ValueEnum;
import com.bojiu.common.util.Formats;
import com.bojiu.context.model.Language;
import com.bojiu.context.web.RequestContextImpl;
import com.bojiu.webapp.base.dto.MerchantContext;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public abstract class AbstractVO<T> implements Serializable {

	protected Long id;

	public static <E> E copy(Object source, Supplier<E> target) {
		return copy(source, target, (String[]) null);
	}

	public static <E> E copy(Object source, Supplier<E> target, String... ignoreProperties) {
		if (source == null) {
			return null;
		}
		E vo = target.get();
		if (vo instanceof SourceVO) {
			((SourceVO<?>) vo).setSource(source);
		}
		BeanUtils.copyProperties(source, vo, ignoreProperties);
		return vo;
	}

	public Date now() {
		return RequestContextImpl.get().now();
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd"格式的字符串
	 */
	public static String formatDate(Date date) {
		return Formats.formatDate_D(date);
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd HH:mm"格式的字符串
	 */
	public static String formatDefDate(Date date) {
		return Formats.formatDefDate(date);
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd HH:mm:ss"格式的字符串
	 */
	public static String formatDatetime(Date date) {
		return Formats.formatDatetime(date);
	}

	protected String getStatus_(Integer status) {
		return status == null ? null : Status.of(status).getToggleLabel();
	}

	protected String getStatusOpen_(Integer status) {
		return status == null ? null : I18N.msg("status." + Status.of(status).name().toLowerCase());
	}

	protected String getStatusStr(Integer status) {
		return status == null ? null : switch (status) {
			case 1 -> "开启";
			case 0 -> "关闭";
			default -> null;
		};
	}

	protected static String label(@Nullable ValueEnum<?, ?> t) {
		return ValueEnum.label(t);
	}

	protected static <T extends java.io.Serializable> T value(@Nullable ValueEnum<?, T> t) {
		return ValueEnum.value(t);
	}

	protected static <K> String label(@Nullable Map<K, String> map, @Nullable K key, @Nullable Language language) {
		if (map == null || key == null) {
			return null;
		}
		String code = map.get(key);
		if (code == null) {
			return null;
		}
		return language == null ? I18N.msg(code) : language.msg(code);
	}

	protected static <K> String label(@Nullable Map<K, String> map, @Nullable K key) {
		return label(map, key, null);
	}

	protected static Date toLocalTime(Long merchantId, Date date) {
		return merchantId != null && date != null ? MerchantContext.toLocalTime(merchantId, date) : null;
	}

}