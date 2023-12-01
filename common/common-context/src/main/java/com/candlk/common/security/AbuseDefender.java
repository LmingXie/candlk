package com.candlk.common.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.*;
import javax.annotation.Nullable;

/**
 * 滥用（频繁调用）防御器
 *
 * @param <T> 方法的入参类型
 * @param <R> 方法的出参类型
 */
public class AbuseDefender<T, R> {

	// TODO 这是一个本地永久缓存，后续要改进一下，避免 OOM
	protected ConcurrentMap<String, ErrorCounter> counterMap = new ConcurrentHashMap<>(128);
	//
	protected Function<T, String> keyGenerator;
	protected Threshold threshold;
	// 结果判定器
	protected Function<R, State> stateDeterminer;
	// 结果判定器
	protected BiFunction<ActionContext<T, R>, State, R> stateMapper;

	public AbuseDefender(Threshold threshold, Function<T, String> keyGenerator, Function<R, State> stateDeterminer, BiFunction<ActionContext<T, R>, State, R> stateMapper) {
		this.threshold = threshold;
		this.keyGenerator = keyGenerator;
		this.stateDeterminer = stateDeterminer;
		this.stateMapper = stateMapper;
	}

	protected ErrorCounter getCounter(ActionContext<T, R> context, boolean createIfAbsent) {
		ErrorCounter counter = context.counter;
		if (counter == null && createIfAbsent) {
			context.counter = counter = createCounter();
			counterMap.put(context.key, counter);
		}
		return counter;
	}

	protected ErrorCounter createCounter() {
		return new ErrorCounter(0, 0);
	}

	protected ActionContext<T, R> createContext(T input) {
		String key = keyGenerator.apply(input);
		ErrorCounter counter = counterMap.get(key);
		return new ActionContext<>(this, input, key, threshold, counter);
	}

	public boolean isExceeded(@Nullable ErrorCounter counter, @Nullable Threshold threshold) {
		// TODO 此处业务逻辑并不完全符合业务的需求期望，但已满足日常使用所需
		return counter != null
				&& threshold != null
				&& counter.times >= threshold.maxTimes
				&& System.currentTimeMillis() - counter.lastOperateTime < threshold.lockingInterval;
	}

	protected void raiseError(ActionContext<T, R> context) {
		ErrorCounter counter = getCounter(context, true);
		counter.times++;
		counter.lastOperateTime = System.currentTimeMillis();
	}

	public boolean preCheck(ActionContext<T, R> context) {
		return !isExceeded(context.counter, context.threshold);
	}

	/**
	 * 根据判定的状态值返回对应的结果
	 *
	 * @param state 结果状态。参见 {@link State}
	 */
	protected R mapStateResult(ActionContext<T, R> ctx, State state) {
		return stateMapper.apply(ctx, state);
	}

	public R postCallback(ActionContext<T, R> context) {
		State state = stateDeterminer.apply(context.output);
		if (state == State.YES) { // 执行成功，清除错误计数器
			if (context.counter != null) {
				counterMap.remove(context.key);
				context.counter = null;
			}
		} else if (state == State.NO) { // 错误计数器增加计数
			raiseError(context);
		}
		return mapStateResult(context, state); // 错误次数增加时，可能需要处理提示信息
	}

	public R execute(T input, Supplier<R> action) {
		ActionContext<T, R> context = createContext(input);
		// 预检查
		if (preCheck(context)) {
			// 只有通过预检查才能执行方法
			context.output = action.get();
			return postCallback(context);
		}
		return mapStateResult(context, State.LOCKED);
	}

	public R execute(T input, Function<T, R> action) {
		return execute(input, () -> action.apply(input));
	}

	public static class Threshold {

		/** 允许错误的次数 */
		public final int maxTimes;
		/** 超过错误次数后将被冻结的时间（单位：ms） */
		public final long lockingInterval;

		public Threshold(int maxTimes, long lockingInterval) {
			this.maxTimes = maxTimes;
			this.lockingInterval = lockingInterval;
		}

		public static Threshold of(final int[] configArray) {
			return new Threshold(configArray[0], configArray[1]);
		}

	}

	public static class ErrorCounter {

		/** 累计错误次数 */
		public int times;
		/** 最后一次操作时间 */
		public long lastOperateTime;

		public ErrorCounter(int times, long lastOperateTime) {
			this.times = times;
			this.lastOperateTime = lastOperateTime;
		}

	}

	public static class ActionContext<T, R> {

		public AbuseDefender<T, R> defender;
		public T input;
		public String key;
		public Threshold threshold;
		public ErrorCounter counter;
		//
		public R output;

		public ActionContext(AbuseDefender<T, R> defender, T input, String key, Threshold threshold, ErrorCounter counter) {
			this.defender = defender;
			this.input = input;
			this.key = key;
			this.threshold = threshold;
			this.counter = counter;
		}

	}

	public enum State {
		YES,
		NO,
		LOCKED,
		SKIP
	}

}
