package com.bojiu.context.web;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bojiu.common.context.Env;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.Common;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.auth.PermissionInterceptor;
import com.bojiu.context.auth.PermissionLocator;
import com.bojiu.context.model.*;
import com.bojiu.webapp.base.dto.MerchantContext;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.base.service.CacheSyncService;
import com.bojiu.webapp.base.service.RemoteSyncService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.codeplayer.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 商户白名单检查 拦截处理器
 */
@Component
public class MerchantWhitelistCheckInterceptor implements HandlerInterceptor, CacheSyncService {

	// < 商户ID, IPs  >
	static final Cache<Long, List<String>> cache = Caffeine.newBuilder()
			.initialCapacity(4)
			.maximumSize(10240)
			.expireAfterWrite(1, TimeUnit.DAYS)
			.build();

	static final Function<Long, List<String>> getWhitelistByMerchantId = merchantId -> {
		final String mid = merchantId.toString();
		final String ips = RedisUtil.opsForHash().get(RedisKey.MERCHANT_WHITELIST, mid);
		if (StringUtil.isEmpty(ips)) {
			return Collections.emptyList();
		}
		return Common.splitAsStringList(ips);
	};

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (Env.outer()) {
			// 白名单检测
			final Long merchantId = ContextImpl.currentMerchantId();
			// 总台、代理商、经销商不检测
			final String domain;
			if (!Merchant.isPlatform(merchantId) && (handler instanceof HandlerMethod method)) {
				final RequestContextImpl req = RequestContextImpl.get();
				Member member;
				if ((req != null && (member = req.sessionUser()) != null && member.asVisitor())
						|| ("1".equals(request.getParameter("vt")) && request.getRequestURI().startsWith("/emp/login"))) {
					return true;
				}
				if (!(domain = DomainUtils.doGetDomain(request)).startsWith("agent") && !domain.startsWith("dealer")) {
					PermissionLocator locator = (PermissionLocator) request.getAttribute(PermissionInterceptor.PERMISSION_LOCATOR_KEY);
					if (locator != null) {
						final Long groupId = MerchantContext.get(merchantId).getGroupId();
						if (!RedisUtil.opsForSet().isMember(RedisKey.WHITELIST_NOT_CHECK, groupId.toString())
								&& !getMerchantWhitelist(groupId).contains(ContextImpl.get().getClientIP(request))) {
							ProxyRequest.writeJSON(response, Messager.status(MessagerStatus.MERCHANT_WHITELIST, "IP地址不在白名单中，请联系客服"), request);
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	@Nullable
	static List<String> getMerchantWhitelist(Long merchantId) {
		return cache.get(merchantId, getWhitelistByMerchantId);
	}

	@Override
	public String getCacheId() {
		return RemoteSyncService.MerchantWhitelistCheckFilter;
	}

	@Override
	public void flushCache(Object... args) {
		RemoteSyncService.clearCacheByKey(cache, args);
	}

}