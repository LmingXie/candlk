package com.bojiu.webapp.base.action;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.bojiu.common.context.Env;
import com.bojiu.common.model.*;
import com.bojiu.common.util.Common;
import com.bojiu.context.auth.PermissionException;
import com.bojiu.context.model.*;
import com.bojiu.context.web.ProxyRequest;
import me.codeplayer.util.LazyCacheLoader;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseAction {

	protected static final Map<Language, Map<String, Supplier<List<Option<String>>>>> allowEnumMetaTypes = new HashMap<>();

	protected static <E> LazyCacheLoader<E> ofCache(Supplier<E> loader) {
		return new LazyCacheLoader<>(loader);
	}

	protected static void putMeta(Language language, String key, Supplier<List<Option<String>>> loader) {
		allowEnumMetaTypes.get(language).put(key, ofCache(loader));
	}

	/** 每次请求动态处理，不缓存 */
	protected static void putMetaRaw(Language language, String key, Supplier<List<Option<String>>> loader) {
		allowEnumMetaTypes.get(language).put(key, loader);
	}

	protected static void putMeta(Language language, String key, ValueProxyImpl<?, ?>... values) {
		putMeta(language, key, language, values);
	}

	protected static void putMeta(Language lanKey, String key, @Nullable Language language, ValueProxyImpl<?, ?>... values) {
		putMeta(lanKey, key, () -> Option.toMetas(values, language));
	}

	protected static void putMeta(Language language, String key, String... valueLabelPairs) {
		putMeta(language, key, () -> {
			List<Option<String>> options = new ArrayList<>(valueLabelPairs.length / 2);
			for (int i = 0; i < valueLabelPairs.length; i++) {
				options.add(new Option<>(valueLabelPairs[i++], language.msg(valueLabelPairs[i])));
			}
			return options;
		});
	}

	protected static void putMeta(Language language, String key, Map<?, String> optionMap) {
		putMeta(language, key, () -> Common.toList(optionMap.entrySet(), t -> Option.ofString(t.getKey(), language.msg(t.getValue()))));
	}

	protected static Messager<Map<String, Collection<Option<String>>>> exposeMetas(Language language, @Nullable String types) {
		Map<String, Supplier<List<Option<String>>>> allMetaMap = Language.getValueOrDefault(allowEnumMetaTypes, language);
		types = StringUtils.deleteWhitespace(types);
		final Map<String, Collection<Option<String>>> resultMap;
		if (StringUtils.isEmpty(types)) {
			resultMap = Collections.emptyMap();
			/*
			resultMap = new LinkedHashMap<>(allMetaMap.size(), 1F);
			for (Map.Entry<String, Supplier<List<Option<String>>>> entry : allMetaMap.entrySet()) {
				Supplier<List<Option<String>>> value = entry.getValue();
				List<Option<String>> optionList = value.get();
				if (X.isValid(optionList)) {
					resultMap.put(entry.getKey(), optionList);
				}
			}
			*/
		} else {
			final List<String> keys = Common.splitAsStringList(types);
			resultMap = new LinkedHashMap<>(keys.size(), 1F);
			for (String type : keys) {
				Supplier<List<Option<String>>> entries = allMetaMap.get(type);
				if (entries == null && Env.inner()) {
					throw new ErrorMessageException("Invalid type:" + type);
				}
				List<Option<String>> optionList = X.tryUnwrap(entries);
				if (X.isValid(optionList)) {
					resultMap.put(type, optionList);
				}
			}
		}
		return Messager.exposeData(resultMap);
	}

	@SafeVarargs
	public static <T> List<Option<String>> toOptions(Function<? super T, Option<String>> mapper, T... values) {
		final List<Option<String>> options = new ArrayList<>(values.length);
		for (T t : values) {
			options.add(mapper.apply(t));
		}
		return options;
	}

	@SafeVarargs
	public static <T> Supplier<List<Option<String>>> supplyOptions(Function<? super T, Option<String>> mapper, T... values) {
		return () -> toOptions(mapper, values);
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

}