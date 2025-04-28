package com.candlk.common.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.*;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.entity.DeflateInputStream;

/**
 * 支持 解压缩处理 的响应体处理器
 */
public class EnhanceHttpResponseBodyHandler implements HttpResponse.BodyHandler<String> {

	public static final EnhanceHttpResponseBodyHandler instance = new EnhanceHttpResponseBodyHandler();

	@Override
	public HttpResponse.BodySubscriber<String> apply(HttpResponse.ResponseInfo info) {
		final String encoding = info.headers().firstValue("Content-Encoding").orElse("");
		return switch (encoding) {
			case "" -> BaseHttpUtil.responseBodyHandler.apply(info);
			case "gzip" -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(), in -> {
				try {
					return IOUtils.toString(new GZIPInputStream(in), charsetFrom(info.headers()));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			case "deflate" -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(), in -> {
				try {
					return IOUtils.toString(new DeflateInputStream(in), charsetFrom(info.headers()));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			default -> throw new UnsupportedOperationException();
		};
	}

	public static Charset charsetFrom(HttpHeaders headers) {
		return charsetFrom(headers.firstValue("Content-type").orElse(null));
	}

	public static Charset charsetFrom(@Nullable String type) {
		int charset = StringUtils.indexOfIgnoreCase(type, "charset");
		if (charset > 0) {
			int begin = -1, end = -1;
				/*
				0 = '='
				1 = ' '?
				2 = '\''?
				3 = '值'
				4 = '\''
				5 = ' '
				6 = ';'
				*/
			int quote = 0, flag = -1;
			final int length = type.length();
			for (int i = charset + 7; -1 <= flag && flag < 6 && i < length; i++) {
				char ch = type.charAt(i);

				switch (ch) {
					case '=' -> flag = flag == -1 ? 0 : -9;
					case ' ' -> {
						if (flag != -1) {
							flag = (flag == 0 || flag == 1) ? 1 : (flag == 4 || flag == 5) ? 5 : -9;
						}
					}
					case '\'' -> {
						flag = flag == 0 || flag == 1 ? 2 : flag == 3 && quote == 1 ? 4 : -9;
						quote++;
					}
					case ';' -> flag = flag == 3 || flag == 4 || flag == 5 ? 6 : -9;
					default -> {
						flag = flag == 0 || flag == 1 || flag == 2 || flag == 3 ? 3 : -9;
						if (begin == -1) {
							begin = i;
						}
						end = i + 1;
					}
				}
			}

			if (flag >= 3 && begin > 0 && end > 0 && quote % 2 == 0) {
				try {
					return Charset.forName(type.substring(begin, end));
				} catch (UnsupportedCharsetException ignored) {
				}
			}
		}
		return StandardCharsets.UTF_8;
	}

}
