package com.candlk.context.web;

import java.util.List;

import com.candlk.common.context.Env;
import com.candlk.common.util.Common;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * 在入参时，对 图片进行压缩与CDN处理
 *
 * @see ImageConverter#convert(String)
 */
public class ImageConverter implements Converter<String, String> {

	public static final String CDN = "https://cdn.candlk.com";
	/** OSS压缩后缀 */
	public static final String COMPRESS_SUFFIX = "!ai";
	/** 覆盖的域名 */
	public static final String COVER_DOMAIN = "https://candlk-prod.oss-ap-southeast-1.aliyuncs.com";
	/** 覆盖的域名 */
	public static final String UAT_COVER_DOMAIN = "https://candlk-uat.oss-ap-southeast-1.aliyuncs.com";

	@Override
	public String convert(String source) {
		boolean replaceCdn;
		if (StringUtil.notEmpty(source) && ((replaceCdn = (Env.inProduction() && source.startsWith(COVER_DOMAIN))) || source.startsWith(UAT_COVER_DOMAIN))) {
			List<String> paths = Common.splitAsStringList(source);
			if (paths != null) {
				StringBuilder sb = new StringBuilder(source.length());
				boolean first = false;
				for (String path : paths) {
					if (first) {
						sb.append(Common.SEP);
					} else {
						first = true;
					}
					sb.append(replaceCdn ? path.replace(COVER_DOMAIN, CDN) : path);
					final String suffix = StringUtils.substringAfterLast(path, ".");
					if (StringUtil.notEmpty(suffix) && "jpg".equalsIgnoreCase(suffix) || "png".equalsIgnoreCase(suffix)) {
						sb.append(COMPRESS_SUFFIX);
					}
				}
				return sb.toString();
			}
		}
		return source;
	}

}
