package com.bojiu.context.config;

import java.io.PrintWriter;
import java.util.List;

import com.bojiu.common.context.Env;
import com.bojiu.common.util.Common;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.*;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 精简堆栈的异常日志输出转换器
 *
 * @see org.apache.logging.log4j.core.pattern.ThrowablePatternConverter
 */
@Plugin(name = "ThinThrowablePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "thinEx" })
public class ThinThrowablePatternConverter extends LogEventPatternConverter {

	/** 指示包含还是排除指定包前缀，默认是包含 */
	private final boolean include;
	@Nullable
	private final String[] packages;
	private final int stackDepth;

	/**
	 * Constructor.
	 *
	 * @param name Name of converter.
	 * @param style CSS style for output.
	 * @param options options, may be null.
	 * @deprecated Use ThinThrowablePatternConverter(String name, String stule, String[] options, Configuration config)
	 */
	@Deprecated
	protected ThinThrowablePatternConverter(final String name, final String style, final String[] options) {
		this(name, style, options, null);
	}

	/**
	 * Constructor.
	 *
	 * @param name name of converter
	 * @param style CSS style for output
	 * @param options options, may be null.
	 * @param config the Configuration or {@code null}
	 */
	protected ThinThrowablePatternConverter(final String name, final String style, final String[] options, final Configuration config) {
		super(name, style);
		List<String> list = null;
		int stackDepth = 7;
		boolean include = true;
		if (options != null && options.length > 0) {
			String raw = options[0];
			String pkgs = StringUtils.removeStart(raw, '!');
			//noinspection StringEquality
			include = pkgs == raw;
			list = Common.splitAsStringList(pkgs);
			if (options.length >= 2) {
				stackDepth = Math.max(NumberUtil.getInt(options[1], stackDepth), 1);
			}
		}
		this.include = include;
		this.packages = !Env.inLocal() && X.isValid(list) ? list.toArray(new String[0]) : null;
		this.stackDepth = stackDepth;
	}

	/**
	 * Gets an instance of the class.
	 *
	 * @param config The Configuration or {@code null}.
	 * @param options pattern options, may be null.  If first element is "short",
	 * only the first line of the throwable will be formatted.
	 * @return instance of class.
	 */
	public static ThinThrowablePatternConverter newInstance(final Configuration config, final String[] options) {
		return new ThinThrowablePatternConverter("ThenEx", "throwable", options, config);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void format(final LogEvent event, final StringBuilder buffer) {
		final Throwable t = event.getThrown();
		if (t != null) {
			formatOption(t, buffer);
		}
	}

	private void formatOption(final Throwable throwable, final StringBuilder buffer) {
		final int len = buffer.length();
		if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
			buffer.append(' ');
		}
		if (packages != null) {
			printStack(throwable, this.stackDepth, this.include, this.packages, buffer);
		} else {
			throwable.printStackTrace(new PrintWriter(new StringBuilderWriter(buffer)));
		}
	}

	/**
	 * This converter obviously handles throwables.
	 *
	 * @return true.
	 */
	@Override
	public boolean handlesThrowable() {
		return true;
	}

	/**
	 * 递归逆向打印堆栈及cause(即从最底层的异常开始往上打)
	 *
	 * @param t 原始异常
	 * @param stackDepth 每一个异常栈的打印深度
	 * @param sb 字符串构造器
	 */
	public static void printStack(Throwable t, int stackDepth, boolean include, @Nullable String[] packages, StringBuilder sb) {
		if (t instanceof com.baomidou.mybatisplus.core.exceptions.MybatisPlusException) {
			t.printStackTrace(new PrintWriter(new StringBuilderWriter(sb)));
			return;
		}
		doPrintStack(t, 7, stackDepth, include, packages, sb);
	}

	public static void doPrintStack(Throwable t, int remainCausedBy, final int singleStackDepth, boolean include, @Nullable String[] packages, StringBuilder sb) {
		sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append('\n');
		StackTraceElement[] elements = t.getStackTrace();
		for (int i = 0, remainDepth = singleStackDepth; i < elements.length && remainDepth > 0; i++) {
			StackTraceElement line = elements[i];
			// 前面2行不跳过
			if (i > 1 && shouldSkipLine(line, include, packages)) {
				continue;
			}
			// sb.append("\tat ").append(line).append('\n');
			appendBetterLine(line, sb);
			// appendSimpleLine(line, sb);
			remainDepth--;
		}
		if (remainCausedBy-- > 0 && t.getCause() != null) {
			sb.append("Caused by: ");
			doPrintStack(t.getCause(), remainCausedBy, singleStackDepth, include, packages, sb);
		}
	}

	protected static boolean shouldSkipLine(StackTraceElement line, boolean include, @Nullable String[] packages) {
		if (packages != null) {
			final String className = line.getClassName();
			final int length = className.length();
			return include != matchPrefix(packages, className, length);
		}
		return false;

	}

	private static boolean matchPrefix(@NonNull String[] packages, String className, int length) {
		for (String pkg : packages) {
			int prefixSize = pkg.length();
			if (prefixSize >= length || className.charAt(prefixSize) != '.') {
				continue;
			}
			if (className.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see StackTraceElement#toString()
	 */
	public static void appendBetterLine(StackTraceElement e, StringBuilder sb) {
		sb.append("\tat ");
		String moduleName = e.getModuleName();
		if (moduleName != null && !moduleName.isEmpty()) {
			sb.append(moduleName).append("/").append(e.getClassName());
		} else {
			sb.append(e.getClassName());
		}
		sb.append('.').append(e.getMethodName());
		if (e.isNativeMethod()) {
			sb.append("(Native Method)");
		} else {
			String fileName = e.getFileName();
			if (fileName != null) {
				sb.append('(').append(fileName).append(':');
				final int lineNumber = e.getLineNumber();
				if (lineNumber >= 0) {
					sb.append(lineNumber);
				}
				sb.append(')');
			} else {
				sb.append("(Unknown Source)");
			}
		}
		sb.append('\n');
	}

	/**
	 * @see StackTraceElement#toString()
	 */
	protected static void appendSimpleLine(StackTraceElement line, StringBuilder sb) {
		sb.append('\t').append(line.getClassName()).append('#').append(line.getMethodName()).append(':').append(line.getLineNumber()).append('\n');
	}

	/**
	 * @see StackTraceElement#toString()
	 */
	protected static void appendStdLine(StackTraceElement line, StringBuilder sb) {
		sb.append("\tat ").append(line).append('\n');
	}

}