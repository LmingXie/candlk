package com.candlk.common.alarm.dingtalk;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.Encrypter;
import me.codeplayer.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;

/**
 * 通知 & 企业微信 bug警告类
 * <p>
 * https://open.dingtalk.com/document/robots/custom-robot-access
 * <p>
 * https://developer.work.weixin.qq.com/document/path/91770
 */
@Slf4j
public class BugSendServiceImpl implements BugSendService {

	/** 默认异常信息不带堆栈细节 */
	protected static final int LOG_INFO = 100;

	protected static String appName;
	protected static String profiles;
	/** 上报日志级别：0=异常信息带细节；100=异常信息不带堆栈细节；Integer.MAX_VALUE=禁用 */
	protected static int logLevel = LOG_INFO;

	@Value("${spring.application.name:}")
	public void setAppName(String appName) {
		BugSendServiceImpl.appName = appName;
	}

	@Value("${spring.profiles.active}")
	public void setProfiles(String profiles) {
		BugSendServiceImpl.profiles = profiles;
		if ("local".equals(profiles)) {
			logLevel = Integer.MAX_VALUE;
		} else if (!"prod".equals(profiles)) {
			logLevel = 0;
		}
	}

	List<AlarmEndpoint> alarmEndpoints;
	String packageName;
	BugWarnExpiredService bugWarnExpiredService;

	/**
	 * 一个通知告警
	 */
	public BugSendServiceImpl(AlarmEndpoint endpoint, String packageName, BugWarnExpiredService bugWarnExpiredService) {
		this(Collections.singletonList(endpoint), packageName, bugWarnExpiredService);
	}

	/**
	 * 多个通知告警
	 */
	public BugSendServiceImpl(List<AlarmEndpoint> alarmEndpoints, String packageName, BugWarnExpiredService bugWarnExpiredService) {
		this.alarmEndpoints = alarmEndpoints;
		this.packageName = packageName;
		this.bugWarnExpiredService = bugWarnExpiredService;
	}

	@Override
	public Messager<String> sendBugMsg(@Nullable String uri, Throwable e, boolean atAll) {
		return doSendMsg(e, ex -> toTextMsgPayload(buildContent(uri, e), atAll), true);
	}

	String buildContent(@Nullable String uri, Throwable e) {
		if (uri == null) {
			uri = "";
		}
		if (hasExceptionFullDetail()) {
			StringWriter out = new StringWriter(2048);
			out.write("系统异常：【" + appName + "-" + profiles + "】=> " + uri + '\n');
			PrintWriter pw = new PrintWriter(out);
			e.printStackTrace(pw);
			return out.toString();
		}

		// content 内容
		StackTraceElement el = getException(e);
		if (el == null) {
			return "系统异常：【" + appName + "-" + profiles + "】=> " + uri + '\n' +
					e.getMessage();
		}
		return "系统异常：【" + appName + "-" + profiles + "】=> " + uri + '\n' +
				e.getMessage() + '\n' +
				el.getClassName() + "." + el.getMethodName() + " ( " + el.getLineNumber() + " )";
	}

	@Nullable
	private StackTraceElement getException(Throwable exception) {
		final StackTraceElement[] stackTraces = exception.getStackTrace();
		final String packageName = this.packageName;
		if (StringUtil.notEmpty(packageName)) {
			for (StackTraceElement el : stackTraces) {
				if (el.getClassName().startsWith(packageName)) {
					return el;
				}
			}
		}
		return stackTraces.length == 0 ? null : stackTraces[0];
	}

	@Override
	public Messager<String> sendBugMsg(String bugMsg, boolean atAll) {
		return doSendMsg(bugMsg, msg -> toTextMsgPayload("业务异常：" + "\n" + appName + "-" + profiles + ":" + "\n" + msg, atAll), true);
	}

	public <T> Messager<String> doSendMsg(T source, Function<T, String> payloadBuilder, boolean distinct) {
		return doSendMsg(alarmEndpoints, source, payloadBuilder, distinct);
	}

	public Messager<String> doSendMsg(final List<AlarmEndpoint> endpoints, String textMsg, boolean distinct, boolean atAll) {
		return doSendMsg(endpoints, toTextMsgPayload(textMsg, atAll), Function.identity(), distinct);
	}

	public <T> Messager<String> doSendMsg(final List<AlarmEndpoint> endpoints, T source, Function<T, String> jsonPayloadBuilder, boolean distinct) {
		if (log.isDebugEnabled()) {
			String detail;
			if (source instanceof CharSequence || source instanceof Exception) {
				detail = source.toString();
			} else {
				detail = JSON.toJSONString(source);
			}
			log.debug("======通知业务异常bug信息发送入参=======：{}", detail);
		}
		if (isLocalEnv()) {
			return Messager.OK();
		}
		String payload = jsonPayloadBuilder.apply(source);
		log.debug("通知准备发送参数：{}", payload);
		// 发送通知告警消息
		// MD5 减少键的长度
		boolean canSend = !distinct || canSendInternal(bugWarnExpiredService, payload);
		if (canSend) {
			for (AlarmEndpoint endpoint : endpoints) {
				return endpoint.sendMsg(payload, source instanceof ActionCard);
			}
		}
		Messager<String> msger = Messager.OK();
		return canSend ? msger : msger.setMsg("已经在一分钟内发过");
	}

	protected boolean canSendInternal(@Nullable BugWarnExpiredService service, String payload) {
		return service == null || service.canSend(Encrypter.md5For16(payload));
	}

	@Override
	public Messager<String> sendMsg(String bugMsg, boolean atAll) {
		return doSendMsg(bugMsg, msg -> toTextMsgPayload("通知信息：\n【" + appName + "-" + profiles + "】\n" + msg, atAll), false);
	}

	@Override
	public Messager<String> sendMarkdownMsg(String title, String markdown, boolean atAll) {
		return doSendMsg(markdown, md -> toMarkdownMsgPayload(title, md, atAll), true);
	}

	public static String toTextMsgPayload(String content, boolean atAll) {
		/*
		{
		    "at": {
		        "atMobiles":[
		            "180xxxxxx"
		        ],
		        "atUserIds":[
		            "user123"
		        ],
		        "isAtAll": false
		    },
		    "text": {
		        "content":"我就是我, @XXX 是不一样的烟火"
		    },
		    "msgtype":"text"
		}
		*/
		return toWechatTypedPayload("text", JSONObject.of("content", content), atAll);
	}

	public static String toMarkdownMsgPayload(String title, String markdownText, boolean atAll) {
		/*
		{
		     "msgtype": "markdown",
		     "markdown": {
		         "title":"杭州天气",
		         "text": "#### 杭州天气 @150XXXXXXXX \n > 9度，西北风1级，空气良89，相对温度73%\n > ![screenshot](https://img.alicdn.com/tfs/TB1NwmBEL9TBuNjy1zbXXXpepXa-2400-1218.png)\n > ###### 10点20分发布 [天气](https://www.dingtalk.com) \n"
		     },
		      "at": {
		          "atMobiles": [
		              "150XXXXXXXX"
		          ],
		          "atUserIds": [
		              "user123"
		          ],
		          "isAtAll": false
		      }
		 }
		*/
		return toWechatTypedPayload("markdown", JSONObject.of("title", title, "text", markdownText), atAll);
	}

	public static String toTypedPayload(String type, Map<String, Object> typedContent, boolean atAll) {
		Map<String, Object> map = new HashMap<>(4);
		map.put("msgtype", type);
		// at @所有人
		if (atAll && logLevel >= LOG_INFO) {
			map.put("at", Map.of("isAtAll", true));
		}
		map.put(type, typedContent);
		return JSONObject.toJSONString(map);
	}

	/**
	 *
	 */
	public static String toWechatTypedPayload(String type, JSONObject typedContent, boolean atAll) {
		Map<String, Object> map = new HashMap<>(4);
		map.put("msgtype", type);
		// at @所有人
		if (atAll && logLevel >= LOG_INFO) {
			typedContent.put("mentioned_list", new String[] { "@all" });
		}
		map.put(type, typedContent);
		return JSON.toJSONString(map);
	}

	public static String toTypedPayload(String type, JSONObject typedContent) {
		return toTypedPayload(type, typedContent, false);
	}

	static boolean isLocalEnv() {
		return logLevel == Integer.MAX_VALUE;
	}

	static boolean hasExceptionFullDetail() {
		return logLevel < LOG_INFO;
	}

	/**
	 * 发送处理卡片
	 */
	public Messager<String> sendActionCard(ActionCard card) {
		return doSendMsg(card, c -> JSON.toJSONString(Map.of(
				"msgtype", "actionCard",
				"actionCard", card
		)), false);
	}

}
