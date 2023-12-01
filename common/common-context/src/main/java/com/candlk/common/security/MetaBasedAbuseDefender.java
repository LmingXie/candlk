package com.candlk.common.security;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import me.codeplayer.util.NumberUtil;

public class MetaBasedAbuseDefender<T, R> extends AbuseDefender<T, R> {

	protected final Threshold def;
	protected final String metaType;
	protected final String maxTimesKey;
	protected final String intervalKey;
	protected final TimeUnit intervalUnit;

	public MetaBasedAbuseDefender(Threshold def, String metaType, String maxTimesKey, String intervalKey, TimeUnit intervalUnit,
	                              Function<T, String> keyGenerator, Function<R, State> stateDeterminer, BiFunction<ActionContext<T, R>, State, R> stateMapper) {
		super(null, keyGenerator, stateDeterminer, stateMapper);
		this.def = def;
		this.metaType = metaType;
		this.maxTimesKey = maxTimesKey;
		this.intervalKey = intervalKey;
		this.intervalUnit = intervalUnit;
		refreshThreshold();
	}

	public void refreshThreshold() {
		final String[] vals = null /*MetaPool.getLabels(metaType, maxTimesKey, intervalKey)*/; // TODO
		if (vals != null) {
			int maxTimes = NumberUtil.getInt(vals[0], def.maxTimes);
			long interval = NumberUtil.getLong(vals[1], def.lockingInterval);
			threshold = new Threshold(maxTimes, intervalUnit.toMillis(interval));
		} else {
			threshold = def;
		}
	}

}
