package com.candlk.webapp.base.action;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import com.candlk.common.model.Messager;
import com.candlk.common.model.ValueProxyImpl;
import com.candlk.common.util.Common;
import com.candlk.context.auth.PermissionException;
import com.candlk.context.model.Option;
import com.candlk.context.model.WithMerchant;
import com.candlk.context.web.ProxyRequest;
import me.codeplayer.util.LazyCacheLoader;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;

import static com.candlk.context.model.Option.toMetas;

public abstract class BaseAction {

	protected static final Map<String, LazyCacheLoader<List<Option<String>>>> allowEnumMetaTypes = new HashMap<>();

	protected static <E> LazyCacheLoader<E> ofCache(Supplier<E> loader) {
		return new LazyCacheLoader<>(loader);
	}

	protected static void putMeta(String key, Supplier<List<Option<String>>> loader) {
		allowEnumMetaTypes.put(key, new LazyCacheLoader<>(loader));
	}

	protected static void putMeta(String key, ValueProxyImpl<?, ?>... values) {
		putMeta(key, () -> toMetas(values));
	}

	protected static void putMeta(String key, String... valueLabelPairs) {
		List<Option<String>> options = new ArrayList<>(valueLabelPairs.length / 2);
		for (int i = 0; i < valueLabelPairs.length; i++) {
			options.add(new Option<>(valueLabelPairs[i++], valueLabelPairs[i]));
		}
		putMeta(key, () -> options);
	}

	protected static Messager<Map<String, Collection<Option<String>>>> exposeMetas(final Map<String, LazyCacheLoader<List<Option<String>>>> allMetaMap, String types) {
		final Map<String, Collection<Option<String>>> resultMap;
		if (StringUtils.isEmpty(types)) {
			resultMap = new LinkedHashMap<>(allMetaMap.size(), 1F);
			for (Map.Entry<String, LazyCacheLoader<List<Option<String>>>> entry : allMetaMap.entrySet()) {
				LazyCacheLoader<List<Option<String>>> entries = entry.getValue();
				List<Option<String>> optionList = X.tryUnwrap(entries);
				if (X.isValid(optionList)) {
					resultMap.put(entry.getKey(), optionList);
				}
			}
		} else {
			final List<String> keys = Common.splitAsStringList(types);
			resultMap = new LinkedHashMap<>(keys.size(), 1F);
			for (String type : keys) {
				LazyCacheLoader<List<Option<String>>> entries = allMetaMap.get(type);
				List<Option<String>> optionList = X.tryUnwrap(entries);
				if (X.isValid(optionList)) {
					resultMap.put(type, optionList);
				}
			}
		}
		return Messager.exposeData(resultMap);
	}

	public static <T> List<Option<String>> toOptions(Function<? super T, Option<String>> mapper, T... values) {
		final List<Option<String>> options = new ArrayList<>(values.length);
		for (T t : values) {
			options.add(mapper.apply(t));
		}
		return options;
	}

	public static <T extends WithMerchant> T checkAccess(T entity, Long merchantId, boolean errorAsNull) {
		if (entity.getMerchantId().equals(merchantId)) {
			return entity;
		}
		if (errorAsNull) {
			return null;
		}
		throw new PermissionException();
	}

	public static <T extends WithMerchant> T checkAccess(T entity, Long merchantId) {
		return checkAccess(entity, merchantId, false);
	}

	public static <T extends WithMerchant> T checkAccess(T entity, WithMerchant o) {
		return checkAccess(entity, o.getMerchantId(), false);
	}

	public static <T extends WithMerchant> T checkAccess(T entity, ProxyRequest q) {
		return checkAccess(entity, q.getMerchantId(), false);
	}

	/**
	 * 检查商户ID是否与当前实体一致（否则报错）
	 */
	public static void assertSame(Long merchantId, Long oMerchantId) {
		WithMerchant.assertSame(merchantId, oMerchantId);
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	public static void assertCanAccess(Long merchantId, Long oMerchantId) {
		WithMerchant.assertCanAccess(merchantId, oMerchantId);
	}

}
