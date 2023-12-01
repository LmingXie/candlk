package com.candlk.common.context;

import me.codeplayer.util.*;

public class AppHook implements Hook {

	protected static Hook instance;

	public static Hook getInstance() {
		return getInstance(AppHook.class);
	}

	@SuppressWarnings("deprecation")
	public static Hook getInstance(Class<? extends Hook> hookType) {
		Hook hook = instance;
		if (hook == null) {
			try {
				instance = hook = hookType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException("Unable to instantiate class: " + hookType, e);
			}
		}
		return hook;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Hook> T me() {
		return (T) getInstance(AppHook.class);
	}

	public static void setInstance(AppHook hook) {
		Assert.notNull(hook, "ContextHook must be not null");
		instance = hook;
	}

}
