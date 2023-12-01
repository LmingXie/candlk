package com.candlk.common.security;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.candlk.common.context.I18N;
import com.candlk.common.model.Messager;
import com.candlk.common.security.AbuseDefender.State;
import me.codeplayer.util.StringUtil;

public class AbuseDefenderManager {

	/** 登录错误次数限制的元数据类型KEY */
	public static final String LOGIN_ERROR_CONFIG_KEY = "loginErrorConfig";
	/** 短信发送次数限制的元数据类型KEY */
	public static final String SEND_SMS_CONFIG_KEY = "sendSmsConfig";

	public static State mapMessagerState(final Messager<?> msger) {
		if (msger == null || msger.isOK()) {
			return AbuseDefender.State.YES;
		}
		String status = msger.getStatus();
		if (StringUtil.isEmpty(status) || Messager.ERROR.equals(status)) {
			return State.NO;
		}
		return State.SKIP;
	}

	public static <E> Function<Messager<E>, State> mapMessagerState(Predicate<String> errorStatusPredicate) {
		return msger -> {
			if (msger == null || msger.isOK()) {
				return State.YES;
			}
			String status = msger.getStatus();
			if (errorStatusPredicate.test(status)) {
				return State.NO;
			}
			return State.SKIP;
		};
	}

	public static <T, R> BiFunction<AbuseDefender.ActionContext<T, Messager<R>>, State, Messager<R>> stateMessagerMapper(final String action) {
		return (ctx, state) -> {
			Messager<R> msger = ctx.output;
			if (state == State.LOCKED) {
				ctx.output = msger = new Messager<>(I18N.msg("login.error.times.limit", I18N.msg(action))); // see UserI18nKey.LOGIN_ERROR_TIMES_LIMIT
			} else if (state == State.NO) { // 0
				int remain = ctx.threshold.maxTimes - ctx.counter.times;
				if (remain > 0) {
					msger.appendMsg(I18N.msg("login.error.times.retry", remain));
				} else {
					msger.appendMsg(I18N.msg("login.error.times.limit.input", I18N.msg(action)));
				}
			}
			return msger;
		};
	}

}
