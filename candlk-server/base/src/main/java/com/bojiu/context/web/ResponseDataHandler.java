package com.bojiu.context.web;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bojiu.common.context.Env;
import com.bojiu.common.context.I18N;
import com.bojiu.common.dao.FastPaginationInnerInterceptor;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.model.Messager;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.common.web.Page;
import com.bojiu.context.auth.ExportInterceptor;
import com.bojiu.context.model.Member;
import com.bojiu.context.model.MemberType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

/**
 * 响应数据处理器，主要用于全局 缓存分页 COUNT(*) 总数、限制单次导出的数据量
 */
public class ResponseDataHandler {

	/** 是否缓存 total（即 COUNT(*) ）总记录数 的阈值 */
	static final int totalCacheThreshold = Env.inProduction() ? 10000 : 100;
	/** 应被限制一次性导出的数据量阈值（暂不处理异步导出） */
	static final int exportLimitThreshold = Env.inProduction() ? 30_0000 : 10000;

	/** 为当前用户缓存的数据量超过一定阈值的各接口总记录数（ 即 COUNT(*) ）：{@code < （员工ID，请求路径?请求参数）， 总记录数 > } */
	static final Cache<Pair<Long, String>, Long> totalCache = MemberType.fromBackstage() ? Caffeine.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.expireAfterWrite(30, TimeUnit.MINUTES)
			.maximumSize(1024)
			.build() : null;

	/** 当前商户+当前接口是否达到了需要缓存 COUNT(*) 的门槛：{@code < (商户ID，请求路径)， 总记录数最大值 > } */
	static final ConcurrentMap<Pair<Long, String>, long[]> merchantCacheWhitelist = MemberType.fromBackstage() ? new ConcurrentHashMap<>() : null;

	public static void handleResultIfNeeded(HttpServletRequest request, Object result) {
		// 如果是后台 数据量 较多的分页列表，则可以酌情缓存总记录数
		if ((result instanceof Messager<?> msger && msger.data() instanceof Page<?> page) && page.getTotal() >= totalCacheThreshold) {
			Member emp = RequestContextImpl.getSessionUser(request);
			if (emp == null) {
				return;
			}
			final long[] maxTotalRef = merchantCacheWhitelist.computeIfAbsent(Pair.of(emp.getMerchantId(), request.getRequestURI()), k -> new long[1]);
			Boolean allow = getAllowCacheTotal(request);
			if (allow != null && !allow) {
				return;
			}
			final String key = paramsAsKey(request);
			Long total = page.getTotal();
			SpringUtil.log.debug("【totalCount 缓存】存入：key={}，总数={}", key, total);
			if (total > maxTotalRef[0]) {
				maxTotalRef[0] = total;
			}
			totalCache.put(Pair.of(emp.getId(), key), total);
		}
	}

	static {
		FastPaginationInnerInterceptor.setPagePreHandler(ResponseDataHandler::handlePage);
	}

	/**
	 * @param stage 阶段性标识
	 * <pre>
	 * 场景 1（导出）： 0（根据缓存信息判断是否超限） -> 99（ 根据 COUNT(*) 判断是否超限 ）
	 * 场景 2（正常查询 + 懒加载）： 1（ 判断是否懒加载 COUNT(*) ） -> 初始化懒加载器
	 * 场景 3（正常查询 + 非懒加载）： 1（ 判断是否懒加载 COUNT(*) ） -> 2（ 根据 COUNT(*) 判断是否超限 ）
	 * </pre>
	 */
	public static int handlePage(IPage<?> page, int stage) {
		if (stage == 0) { // 无需 COUNT(*) 的分页查询（一般是数据导出）
			HttpServletRequest request = RequestContextImpl.get().getRequest();
			if (request != null && ExportInterceptor.isExport(request)) {
				final long totalCount = getCachedTotalCount(request);
				if (totalCount > exportLimitThreshold) {
					throw denyExportException();
				} else if (totalCount < 0 && -totalCount > exportLimitThreshold) {
					return 99; // 找不到缓存，并且记录总数曾经有超过阈值，还需要查询一下 总记录数
				}
			}
			return 0; // 放行，无需 COUNT(*)
		} else if (stage == 99) {
			if (page.getTotal() > exportLimitThreshold) {
				throw denyExportException();
			}
			return 0; // 不要执行 continue
		}
		// 正常查询（ stage 1 或 2） 默认会走到这里来
		return 1; // 返回值：1=默认懒加载；2=默认非懒加载
	}

	static ErrorMessageException denyExportException() {
		return new ErrorMessageException(Messager.status("limited", I18N.msg("export.denied")));
	}

	/**
	 * @return <0=达到缓存门槛，但没有命中缓存（实际返回的是该商户+该接口曾经返回的最大记录数的负数表示）；0=没有达到缓存门槛；>0=实际的缓存值
	 */
	public static long getCachedTotalCount(HttpServletRequest request) {
		Member emp = RequestContextImpl.getSessionUser(request);
		final long[] maxtotalRef;
		if (emp == null || (maxtotalRef = merchantCacheWhitelist.get(Pair.of(emp.getMerchantId(), request.getRequestURI()))) == null) {
			return 0L;
		}
		final String key = paramsAsKey(request);
		Long totalCount = totalCache.getIfPresent(Pair.of(emp.getId(), key));
		SpringUtil.log.debug("【totalCount 缓存】获取：key={}，总数={}", key, totalCount);
		return totalCount != null ? totalCount : -maxtotalRef[0];
	}

	public static String paramsAsKey(HttpServletRequest request) {
		final String path = request.getRequestURI();
		String queryString = request.getQueryString();
		if (queryString == null) {
			return path;
		}
		final TreeMap<String, String> params = new TreeMap<>();
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			final String name = entry.getKey();
			if (switch (name) {
				case "_page", "_size", "total", "_debug" -> false;
				// "export", "exportField", "exportFieldName", "sort", "sortField"
				default -> !name.startsWith("export") && !name.startsWith("sort");
			}) {
				String[] values = entry.getValue();
				params.put(name, values.length == 1 ? values[0] : String.join("|", values));
			}
		}
		if (params.isEmpty()) {
			return path;
		}
		// 剔除了2个分页参数，只会多不会少
		StringBuilder sb = new StringBuilder(path.length() + queryString.length() + 1)
				.append(path).append('?');
		boolean notFirst = false;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (notFirst) {
				sb.append('&');
			} else {
				notFirst = true;
			}
			sb.append(entry.getKey()).append('=').append(entry.getValue());
		}
		return sb.toString();
	}

	@Nullable
	public static Boolean getAllowCacheTotal(HttpServletRequest request) {
		return (Boolean) request.getAttribute("_allowCacheTotal");
	}

	public static void setAllowCacheTotal(HttpServletRequest request, Boolean allow) {
		request.setAttribute("_allowCacheTotal", allow);
	}

}