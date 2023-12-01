package com.candlk.common.model;

import java.io.Serializable;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;

/**
 * 用于对接远程表单验证响应消息的实体类<br>
 * 一般情况下，你应该在 Action 中调用 writeJSON(new Messager()) 将该实体转换为JSON字符串
 *
 * @date 2015年07月27日
 */
@Getter
@Setter
@Accessors(chain = true)
public class Messager<E> implements Serializable {

	public static int ERROR_CODE = -1;

	/**
	 * 请使用 {@link java.lang.Void } 替代，后续将移除
	 */
	@Deprecated(forRemoval = true)
	public static final class None {

	}

	/**
	 * status 属性 可选值，表示成功状态
	 */
	public static final String OK = "OK";
	/**
	 * status 属性 可选值，表示操作失败&出错的状态
	 */
	public static final String ERROR = "ERROR";
	/**
	 * status 属性 可选值，表示输入错误的状态
	 */
	public static final String INPUT_ERROR = "inputError";
	/**
	 * status 属性 可选值，表示对应的资源不存在
	 */
	public static final String NULL = "NULL";
	/**
	 * callback 属性可选值，表示 msg 通过确认框展示。
	 */
	public static final String CALLBACK_CONFIRM = "confirm";
	/**
	 * callback 属性可选值，表示 msg 通过弹出框展示。
	 */
	public static final String CALLBACK_ALERT = "alert";
	/**
	 * callback 属性 可选值，表示刷新当前页面
	 */
	public static final String CALLBACK_REFRESH = "refresh";
	/**
	 * callback 属性 可选值，表示刷新当前页面（如果存在父页面，则刷新最顶级的页面）
	 */
	public static final String CALLBACK_REFRESH_TOP = "refreshTop";
	/**
	 * callback 属性 可选值，表示返回前一个页面
	 */
	public static final String CALLBACK_BACK = "back";
	/**
	 * callback 属性 可选值，表示刷新图片验证码
	 */
	public static String CALLBACK_REFRESH_IMAGE = "refreshImage";
	/** 响应状态，如果为"OK"则表示验证通过 */
	protected String status;
	/** 业务代码 */
	protected int code = -1;
	/** 提示消息，如果不为空则前台表单验证将提示该消息内容 */
	protected String msg;
	/** 预留扩展字段 */
	protected Object ext;
	/** 回调函数的方法名称(例如:"refresh")，如果不为空则执行前台对应名称的回调函数 */
	protected String callback;
	/** 跳转的URL，如果不为空，则跳转到对应的URL */
	protected String url;
	//
	protected E payload;
	protected transient boolean exposeData;

	public Messager() {
	}

	/**
	 * 默认 status 为 OK，并设置指定的 data
	 */
	public static <E> Messager<E> hideData(@Nullable E data) {
		return new Messager<E>().setOK().setPayload(data);
	}

	/**
	 * 【警告】该方法仅限序列化工具调用
	 *
	 * @see #data()
	 */
	@Deprecated
	public E getData() {
		return exposeData ? payload : null;
	}

	public Messager<E> setData(E data) {
		if (data != null) {
			this.payload = data;
			this.exposeData = true;
		}
		return this;
	}

	/**
	 * 默认 status 为 OK，并设置指定的 data 和 msg
	 */
	public static <E> Messager<E> hideData(@Nullable E innerData, String msg) {
		return hideData(innerData).setMsg(msg);
	}

	/**
	 * 如果 {@code data} 为 null，则输出一个状态为"NULL"的实例，否则返回包含该数据且为"OK"状态的实例
	 */
	public static <E> Messager<E> OKOrNull(@Nullable E data) {
		if (data == null) {
			return status(NULL);
		} else {
			return exposeData(data);
		}
	}

	/**
	 * 如果 {@code error} 为 null，则返回一个状态为"OK"的实例，否则返回包含该错误消息的实例
	 */
	public static <E> Messager<E> withMsg(@Nullable String error) {
		if (error == null) {
			return OK();
		} else {
			return new Messager<E>().setMsg(error);
		}
	}

	/**
	 * 默认 status 为 OK，并设置指定的 data，且允许对外输出显示 data
	 */
	public static <E> Messager<E> exposeData(@Nullable E data) {
		return hideData(data).expose();
	}

	/**
	 * 默认 status 为 OK，并设置指定的 data 和 msg，且允许对外输出显示 data
	 */
	public static <E> Messager<E> exposeData(@Nullable E data, String msg) {
		return exposeData(data).setMsg(msg);
	}

	public static <E> Messager<E> OK() {
		return new Messager<E>().setOK();
	}

	/**
	 * 默认 status 为 OK，并设置指定的 msg
	 */
	public static <E> Messager<E> OK(String msg) {
		return new Messager<E>().setOK().setMsg(msg);
	}

	/**
	 * 构建一个包含指定 状态 的实例
	 */
	public static <E> Messager<E> status(String status) {
		return new Messager<E>().setStatus(status);
	}

	/**
	 * 构建一个包含指定 状态 和 提示信息 的实例
	 */
	public static <E> Messager<E> status(String status, String msg) {
		return new Messager<E>(msg).setStatus(status);
	}

	/**
	 * 构建一个包含指定 提示信息 的实例
	 */
	public static <E> Messager<E> error(String msg) {
		return status(ERROR, msg);
	}

	public Messager(String msg) {
		this.msg = msg;
	}

	public Messager(String msg, String url) {
		this.msg = msg;
		this.url = url;
	}

	public Messager(String status, String msg, String url) {
		this.status = status;
		this.msg = msg;
		this.url = url;
	}

	public Messager(String status, String msg, String url, String callback) {
		this.status = status;
		this.msg = msg;
		this.url = url;
		this.callback = callback;
	}

	public Messager<E> appendMsg(String msg) {
		this.msg = StringUtil.concat(this.msg, msg);
		return this;
	}

	public Messager<E> setMsgAndCallback(String msg, String callback) {
		this.msg = msg;
		this.callback = callback;
		return this;
	}

	/**
	 * 【警告】该方法仅限序列化工具调用
	 *
	 * @see #data()
	 */
	@Deprecated
	@JSONField(serialize = false)
	public E getPayload() {
		return exposeData ? payload : null;
	}

	public E data() {
		return payload;
	}

	/**
	 * 设置状态为"OK"
	 *
	 * @see #OK
	 * @since 1.0
	 */
	public Messager<E> setOK() {
		this.status = OK;
		this.code = 0;
		return this;
	}

	public Messager<E> setStatus(String status) {
		if (OK.equals(status)) {
			this.code = 0;
		}
		this.status = status;
		return this;
	}

	/**
	 * 设置状态为"ERROR"
	 *
	 * @see #ERROR
	 * @since 1.0
	 */
	public Messager<E> setERROR() {
		this.status = ERROR;
		this.code = -1;
		return this;
	}

	/**
	 * 设置模态窗口消息
	 *
	 * @see #CALLBACK_ALERT
	 * @since 1.0
	 */
	public Messager<E> alert(String msg) {
		this.msg = msg;
		this.callback = CALLBACK_ALERT;
		return this;
	}

	/**
	 * 设置确认框消息
	 *
	 * @see #CALLBACK_CONFIRM
	 * @since 1.0
	 */
	public Messager<E> confirm(String msg) {
		this.msg = msg;
		this.callback = CALLBACK_CONFIRM;
		return this;
	}

	/**
	 * 返回一个弹窗提示框
	 */
	public static Messager<String> alertFor(String msg) {
		return new Messager<String>().alert(msg);
	}

	/**
	 * 返回一个确认提示框
	 */
	public static Messager<String> confirmFor(String msg) {
		return new Messager<String>().confirm(msg);
	}

	@JSONField(serialize = false)
	public boolean isOK() {
		return OK.equals(status);
	}

	/**
	 * 设置回调处理函数为刷新图片验证码
	 *
	 * @see #CALLBACK_REFRESH_IMAGE
	 * @since 1.0
	 */
	public Messager<E> setCallbackToRefreshImage() {
		this.callback = CALLBACK_REFRESH_IMAGE;
		return this;
	}

	/**
	 * 设置处理完毕后刷新当前页面
	 *
	 * @see #CALLBACK_REFRESH
	 * @since 1.0
	 */
	public Messager<E> setRefresh() {
		this.callback = CALLBACK_REFRESH;
		return this;
	}

	/**
	 * 设置处理完毕后刷新当前页面（如果存在父页面，则刷新最顶级的页面）
	 *
	 * @see #CALLBACK_REFRESH_TOP
	 * @since 1.0
	 */
	public Messager<E> setURLRefreshTop() {
		this.url = CALLBACK_REFRESH_TOP;
		return this;
	}

	/**
	 * 设置处理完毕后跳转回前一个页面
	 *
	 * @see #CALLBACK_BACK
	 * @since 1.0
	 */
	public Messager<E> setURLBack() {
		this.url = CALLBACK_BACK;
		return this;
	}

	/**
	 * 设置处理完毕后跳转回前一个页面，如果找不到前一个页面，就跳转到指定的默认URL页面
	 *
	 * @see #CALLBACK_BACK
	 * @since 1.0
	 */
	public Messager<E> setURLBack(final String defaultURL) {
		this.url = StringUtil.concat(defaultURL, CALLBACK_BACK);
		return this;
	}

	/**
	 * 只是是否允许通过 {@code getData()} 获取到 {@code data} 属性值
	 */
	public Messager<E> expose(boolean exposeData) {
		this.exposeData = exposeData;
		return this;
	}

	/**
	 * 进行类型转换操作
	 */
	public <R> Messager<R> transform(Function<E, R> converter) {
		return castDataType(converter.apply(payload));
	}

	/**
	 * 允许通过 {@code getData()} 获取到 {@code data} 属性值
	 */
	public Messager<E> expose() {
		return expose(true);
	}

	/**
	 * 检测指定的 Messager对象 是否表示处理成功
	 */
	public static boolean isOK(@Nullable Messager<?> msger) {
		return msger == null || msger.isOK();
	}

	/**
	 * 如果操作成功，且没有提示信息，则设置默认的提示信息，否则什么都不做
	 */
	public Messager<E> setMsgIfOK(String defaultMsg) {
		if (isOK() && StringUtil.isEmpty(msg)) {
			setMsg(defaultMsg);
		}
		return this;
	}

	/**
	 * 检测指定的 Messager对象 是否表示处理成功
	 */
	public static boolean isError(@Nullable Messager<?> msger) {
		return !isOK(msger);
	}

	/**
	 * 通过设置指定类型的数据，同时将当前Messager的泛型类型转换为对应的类型
	 */
	public final <T> Messager<T> castDataType(final T data) {
		return cast(this, data);
	}

	/**
	 * 将当前 Messager 的泛型类型转换为对应的类型
	 */
	public final <T> Messager<T> castAndExpose() {
		return X.castType(expose());
	}

	/**
	 * 通过设置指定类型的数据，同时将当前 {@code Messager }对象的泛型类型转换为对应的类型
	 *
	 * @param msger 该参数可以为null，但 {@code data }也必须同时为null，否则将引发异常
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E> Messager<E> cast(final Messager msger, final E data) {
		if (msger == null) {
			if (data != null) {
				throw new NullPointerException("msger cannot be null");
			}
			return null;
		}
		return msger.setPayload(data);
	}

}
