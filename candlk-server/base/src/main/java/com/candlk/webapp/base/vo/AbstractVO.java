package com.candlk.webapp.base.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.candlk.common.model.*;
import com.candlk.common.util.Formats;
import com.candlk.context.web.RequestContextImpl;
import lombok.*;
import me.codeplayer.util.EasyDate;
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

	/**
	 * 格式化指定的日期，并转换为"yyyy年MM月dd日"格式的字符串
	 */
	public static String formatDateString(Date date) {
		return date != null ? EasyDate.toDateString(date) : null;
	}

	protected String formatDate_DO(Date dateTime) {
		if (dateTime == null) {
			return null;
		}
		final EasyDate d = new EasyDate(dateTime);
		if (d.getSecond() > 0) { // yyyy-MM-dd HH:mm:ss
			return d.toDateTimeString();
		} else if (d.getHour() > 0 || d.getMinute() > 0) { // yyyy-MM-dd HH:mm
			return formatDefDate(dateTime);
		}
		return d.toString();
	}

	protected Integer stateOutput(State state) {
		return state == null ? null : StateBean.asStatus(state).value;
	}

	protected String getState_(State state) {
		return state == null ? null : Status.of(StateBean.isEnabled(state)).getToggleLabel();
	}

	protected String getStatus_(Integer status) {
		return status == null ? null : Status.of(status).getToggleLabel();
	}

	protected String getStatusStr(Integer status) {
		return status == null ? null : switch (status) {
			case 1 -> "开启";
			case 0 -> "关闭";
			default -> null;
		};
	}

	protected static String label(@Nullable ValueProxyImpl<?, ?> t) {
		return t == null ? null : t.getLabel();
	}

	protected static <T> T value(@Nullable ValueProxyImpl<?, T> t) {
		return t == null ? null : t.getValue();
	}

}
