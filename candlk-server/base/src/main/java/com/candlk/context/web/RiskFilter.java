package com.candlk.context.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.candlk.common.context.RequestContext;
import com.candlk.common.redis.RedisUtil;
import com.candlk.context.model.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.Cmp;
import me.codeplayer.util.NumberUtil;
import org.springframework.stereotype.Component;

/**
 * 风控-限制商户过滤器
 */
@Slf4j
@Component
public class RiskFilter implements Filter {

	static final Cache<Long, Integer> cache = Caffeine.newBuilder()
			.initialCapacity(16)
			.maximumSize(1024)
			.expireAfterAccess(1, TimeUnit.DAYS)
			.build();

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) resp;
		final String uri = request.getRequestURI();
		final boolean logout = uri.endsWith("/logout");
		if (!logout) {
			// 风控规则-禁止游戏
			Member user = RequestContext.getSessionUser(request);
			if (user != null && !check(getSiteStatus(user.getMerchantId()))) {
				// 1.前台用户不能进入三方游戏
				if (uri.endsWith("/game/play")) {
					return;
				}
				// 2.后台用户踢出
				if (RequestContextImpl.get().fromBackstage()) {
					request.getSession().invalidate();
					SessionCookieUtil.clearCookies(request, response);
					response.sendRedirect("/");
					return;
				}
			}
		}
		chain.doFilter(req, resp);
	}

	private Integer getSiteStatus(Long merchantId) {
		return cache.get(merchantId, t -> NumberUtil.getInteger(RedisUtil.score(RedisKey.MERCHANT_SITE_STATUS, merchantId.toString()), null));
	}

	private boolean check(@Nullable Integer siteStatus) {
		return !Cmp.eq(siteStatus, SiteStatus.PROHIBIT_GAMES.value);
	}

}
