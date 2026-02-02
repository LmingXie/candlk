package com.bojiu.context.model;

import java.util.Arrays;
import java.util.Set;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import com.bojiu.context.brand.Feature;
import com.bojiu.context.brand.FeatureContext;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Getter
public enum DnsProvider implements ValueProxyImpl<DnsProvider, Integer> {

	/** Cloudflare */
	Cloudflare(0, "Cloudflare", true),
	/** AWS */
	AWS(1, "AWS", true),
	//
	;

	public final Integer value;
	public final boolean enable;
	final ValueProxy<DnsProvider, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	DnsProvider(Integer value, String label, boolean enable) {
		this.value = value;
		this.enable = enable;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	@NonNull
	public static Set<DnsProvider> loadFeatures(FeatureContext context) {
		return context.toSet(Feature.DnsProvider, Integer.class, DnsProvider::of);
	}

	@NonNull
	public static Set<DnsProvider> loadFeaturesBack(FeatureContext context) {
		return context.toSet(Feature.BackDnsProvider, Integer.class, DnsProvider::of);
	}

	public static final DnsProvider[] CACHE = Arrays.stream(values()).filter(DnsProvider::isEnable).toArray(DnsProvider[]::new);

	public static DnsProvider of(@Nullable Integer value) {
		return Cloudflare.getValueOf(value);
	}

}