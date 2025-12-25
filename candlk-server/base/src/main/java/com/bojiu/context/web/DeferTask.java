package com.bojiu.context.web;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.model.Messager;
import org.jspecify.annotations.Nullable;

/**
 * 延迟处理任务
 *
 * @param whenFlag 位运算标识：0=任意情况下都执行；1=返回OK时执行；2=返回不OK时执行；4=报错时执行
 */
public record DeferTask(int whenFlag, Runnable task) {

	public static final int whenAny = 0, whenOK = 1, whenFail = 1 << 1, whenError = 1 << 2, whenFailOrError = whenFail | whenError;

	static final String ATTR_DEFER_TASKS = "$deferTasks";

	/**
	 * 添加一个延迟处理任务，会在 Action方法 执行后并满足 <code> whenFlag </code> 的情况下执行
	 *
	 * @param whenFlag 前置条件 位运算标识：0=任意情况下都执行；1=返回OK时执行；2=返回不OK时执行；4=报错时执行
	 * @see DeferTask#whenAny
	 * @see DeferTask#whenOK
	 * @see DeferTask#whenFail
	 * @see DeferTask#whenError
	 * @see DeferTask#whenFail
	 */
	public static void addDeferTask(HttpServletRequest request, int whenFlag, Runnable task) {
		final String attrName = ATTR_DEFER_TASKS;
		DeferQueue queue = (DeferQueue) request.getAttribute(attrName);
		if (queue == null) {
			request.setAttribute(attrName, queue = new DeferQueue());
		}
		queue.add(whenFlag, task);
	}

	public static void executeDeferTasks(HttpServletRequest request, @Nullable Object returnValue, @Nullable Throwable e) {
		Object val = request.getAttribute(ATTR_DEFER_TASKS);
		if (val != null) {
			DeferQueue queue = (DeferQueue) val;
			// 返回非 Messager 时，视为 OK
			int flag = e != null ? DeferTask.whenError : returnValue instanceof Messager<?> result && !result.isOK() ? DeferTask.whenFail : DeferTask.whenOK;
			queue.executeAll(flag);
		}
	}

	static class DeferQueue {

		final List<DeferTask> list;

		public DeferQueue(List<DeferTask> list) {
			this.list = list;
		}

		public DeferQueue() {
			this(new ArrayList<>(2));
		}

		public void add(int whenFlag, Runnable task) {
			list.add(new DeferTask(whenFlag, task));
		}

		public void executeAll(int actualFlag) {
			for (DeferTask t : list) {
				int expect = t.whenFlag;
				if (expect == DeferTask.whenAny || (expect & actualFlag) == actualFlag) {
					t.task.run();
				}
			}
		}

	}

}