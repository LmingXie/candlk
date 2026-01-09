package com.bojiu.webapp.user.bet;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import org.springframework.data.redis.core.ValueOperations;

/** 登录版本 */
public abstract class LoginBaseBetApiImpl extends BaseBetApiImpl {

	protected transient JSONObject loginInfo;

	/** 登录 */
	protected abstract JSONObject doLogin(String lang);

	/** 登录超时时间 */
	public int getTokenTimeout() {
		return 3;
	}

	/** 获取登录 Token */
	protected JSONObject getLoginToken() {
		if (loginInfo == null) {
			ValueOperations<String, String> opsForValue = RedisUtil.template().opsForValue();
			final String key = getProvider() + "_token", loginJson = opsForValue.get(key);
			if (loginJson != null) {
				loginInfo = Jsons.parseObject(loginJson);
			} else {
				final JSONObject result = doLogin(getDefaultLanguage());
				if (result != null) {
					loginInfo = result;
					opsForValue.set(key, Jsons.encode(loginInfo), getTokenTimeout(), TimeUnit.DAYS);
				}
			}
		}
		return loginInfo;
	}

	/** 清除登录 Token */
	protected void clearLoginToken() {
		loginInfo = null;
		RedisUtil.template().delete(getProvider() + "_token");
		LOGGER.info("【{}】清除登录信息成功！", getProvider());
	}

}
