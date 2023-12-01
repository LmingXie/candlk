package com.candlk.common.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import me.codeplayer.util.StringUtil;

/**
 * HTTP请求相关处理工具类
 *
 * @date 2015年07月28日
 */
public abstract class ServletUtil {

	/**
	 * 获取站点的相对根路径<br>
	 * 如果当前项目名称为"test",则获取到的根路径为"/test"
	 */
	public static String getRoot(HttpServletRequest request) {
		return request.getContextPath();
	}

	/**
	 * 获取站点的绝对根路径<br>
	 * 如果当前站点域名为"domain.com"，项目名称为"test",则获取到的根路径为"{@code protocol}://domain.com/test"
	 *
	 * @param request 请求对象
	 */
	public static String getRootURL(final HttpServletRequest request) {
		final String scheme = request.getScheme();
		StringBuilder url = new StringBuilder(24).append(scheme).append("://");
		url.append(request.getServerName());
		int port = request.getServerPort();
		if (port != 80) { // 非80端口，添加端口号
			if (port != 443 || !"https".equals(scheme)) {
				url.append(':').append(port);
			}
		}
		url.append(request.getContextPath());
		return url.toString();
	}

	/**
	 * 是否是内网环境访问
	 */
	public static boolean isInnerEnv(HttpServletRequest request) {
		String host = request.getServerName();
		return inLAN(host) || "localhost".equals(host) || "127.0.0.1".equals(host);
	}

	/**
	 * 指示指定的IP是否是内网IP（不包括 127.0.0.1 等本地IP）
	 */
	public static boolean inLAN(String ipv4) {
		return ipv4.startsWith("172.") || ipv4.startsWith("10.") || ipv4.startsWith("192.");
	}

	/**
	 * 获取当前请求的来源路径，即获取 HTTP Referer 字段，如果没有则返回 null
	 */
	@Nullable
	public static String getReferer(HttpServletRequest request) {
		return request.getHeader("Referer");
	}

	/**
	 * 获取当前请求的 Accept-Language 请求头信息，如果没有则返回 null
	 */
	@Nullable
	public static String getAcceptLanguage(HttpServletRequest request) {
		return request.getHeader("Accept-Language");
	}

	/**
	 * 获取当前请求的URI<br>
	 * 该方法在Controller forward到JSP后，仍能够正确获取用户请求的URI
	 */
	public static String getRequestURI(HttpServletRequest request) {
		String uri = (String) request.getAttribute("javax.servlet.forward.request_uri");
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return uri;
	}

	/** 是否需要通过请求头来获取远程IP：> 0 表示需要；< -9 表示不需要；= [-9, 0]（默认为 0） 表示根据实际情况自动判断 */
	public static byte getRemoteIpByHeader = 1;
	/** 当 IP获取策略尚未确定时，要连续多少次没有请求头才能确认最终策略 */
	private static final byte NO_HEADER_THRESHOLD = -9;

	/**
	 * 获取当前请求的用户客户端的真实IP地址（智能防止IP伪造）
	 */
	public static String getClientIP(final HttpServletRequest request) {
		String ipx = null;
		if (getRemoteIpByHeader >= NO_HEADER_THRESHOLD) {
			// 在本项目中，实际是 "x-forwarded-for"
			final String[] headers = { "x-forwarded-for", "x-real-ip" };
			for (String name : headers) {
				String value = request.getHeader(name);
				if (value != null && !"unknown".equalsIgnoreCase(value)) {
					ipx = value;
					break;
				}
			}
			if (ipx != null && ipx.length() > 15) { // 如果ip地址长度大于15，即"1.1.1.1,1.1.1.1"的长度，则可能存在多个ip地址
				int pos = ipx.indexOf(',', 7 /* "1.1.1.1".length() */);
				if (pos > 0) {
					ipx = ipx.substring(0, pos);
				}
			}
			if (ipx != null && getRemoteIpByHeader > 0) {
				return ipx;
			}
		}
		String remoteIP = request.getRemoteAddr();
		String ip = tryTranslateLocalIP(remoteIP);
		// 进行最终的IP获取策略判断。这里之所以要用多次偏移才能确定，是担心启动后的首次请求可能来自于内网（例如：健康检查程序），才导致检测不到IP请求头
		if (getRemoteIpByHeader >= NO_HEADER_THRESHOLD && getRemoteIpByHeader <= 0) {
			if (ipx == null) {
				getRemoteIpByHeader--;
			} else if ("127.0.0.1".equals(remoteIP) || inLAN(ip)) {
				getRemoteIpByHeader = 1;
				return ipx;
			}
		}
		return ip;
	}

	static String tryTranslateLocalIP(String ip) {
		if (ip == null) { // 在某些场景下，request.getRemoteAddr() 会返回 null
			return null;
		}
		switch (ip) {
			case "127.0.0.1":
			case "0:0:0:0:0:0:0:1":
				try {
					return getLocalIP();
				} catch (SocketException e) {
					throw new IllegalStateException(e);
				}
			default:
				return ip;
		}
	}

	/**
	 * 获取本地的IP地址。如果能够获取到外网IP，则返回外网IP；否则返回内网IP<br>
	 * <b>注意</b>：暂不支持IPv6地址
	 */
	public static String getLocalIP() throws SocketException {
		String ip = null;// 外网IP(或内网IP)
		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ia;
		boolean notFound = true;
		while (notFound && netInterfaces.hasMoreElements()) {
			NetworkInterface ni = netInterfaces.nextElement();
			if (!ni.isUp() || ni.isLoopback()) {
				// 如果该网络接口 未启用 或 是本地回环接口，则忽略掉
				continue;
			}
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ia = address.nextElement();
				if ((ia instanceof Inet4Address) && !ia.isLoopbackAddress()) { // 仅处理IPv4地址，并且是非回环地址
					if (!ia.isSiteLocalAddress()) { // 内网IP
						ip = ia.getHostAddress(); // 外网IP
						notFound = false;
						break;
					}
				}
			}
		}
		if (StringUtil.isEmpty(ip)) {
			try {
				ia = Inet4Address.getLocalHost();
			} catch (UnknownHostException e) {
				throw new IllegalStateException(e);
			}
			ip = ia.getHostAddress();
		}
		return ip;
	}

	/**
	 * 获取指定网络地址所对应的网络接口硬件地址(一般是Mac地址)
	 */
	public static String getMacAddress(InetAddress inetAddress) throws SocketException {
		byte[] macArray = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
		if (macArray == null) {
			return null;
		}
		StringBuilder mac = new StringBuilder(17);
		final char[] buf = "0123456789ABCDEF".toCharArray();
		for (int i = 0; i < macArray.length; i++) {
			if (i > 0) {
				mac.append('-');
			}
			mac.append(buf[macArray[i] >> 4 & 0xF]);
			mac.append(buf[macArray[i] & 0xF]);
		}
		return mac.toString();
	}

	/**
	 * 获取本地网络地址所对应的网络接口硬件地址(一般是Mac地址)
	 */
	public static String getLocalMacAddress() throws SocketException, UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		if (ip.isLoopbackAddress()) {
			boolean notFound = true;
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (notFound && nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				if (ni.isUp() && !ni.isLoopback()) {
					Enumeration<InetAddress> ips = ni.getInetAddresses();
					while (ips.hasMoreElements()) {
						ip = ips.nextElement();
						if (ip instanceof Inet4Address) {
							notFound = false;
							break;
						}
					}
				}
			}
		}
		return getMacAddress(ip);
	}

	public static final String X_REQUESTED_WITH = "X-Requested-With";
	public static final String XML_HTTP_REQUEST = "XMLHttpRequest";

	/**
	 * 判断是否是Ajax提交过来的请求
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		return XML_HTTP_REQUEST.equals(request.getHeader(X_REQUESTED_WITH));
	}

	public static final String ACCEPT = "Accept";
	public static final String APPLICATION_JSON = "application/json";

	/**
	 * 判断是否是期望接收JSON数据的请求
	 */
	public static boolean isAcceptJSON(HttpServletRequest request) {
		String accept = request.getHeader(ACCEPT);
		return accept != null && accept.startsWith(APPLICATION_JSON);
	}

	/**
	 * 将指定的对象转为JSON字符串并输出到HTTP响应流中
	 *
	 * @param response HttpServletResponse对象
	 * @param object 需要转为JSON进行输出的对象
	 */
	public static void writeJSON(HttpServletResponse response, Object object) throws IllegalStateException {
		response.setContentType("text/html;charset=UTF-8");
		try {
			response.getWriter().write(JSON.toJSONString(object));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 将指定的URL进行编码
	 */
	public static String encodeURL(String str, String encoding) {
		try {
			return URLEncoder.encode(str, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 将指定的URL基于"UTF-8"进行编码
	 */
	public static String encodeURL(String str) {
		return URLEncoder.encode(str, StandardCharsets.UTF_8);
	}

	/**
	 * 将指定的文本进行URL解码
	 */
	public static String decodeURL(String str, String encoding) {
		try {
			return URLDecoder.decode(str, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 将指定的文本进行基于"UTF-8"的URL解码
	 */
	public static String decodeURL(String str) {
		return URLDecoder.decode(str, StandardCharsets.UTF_8);
	}

	/**
	 * 设置用于下载文件的响应头信息（兼容所有浏览器）
	 */
	public static void responseHeaderForDownload(HttpServletRequest request, HttpServletResponse response, String fileName) {
		// see https://tools.ietf.org/html/rfc5987
		// see http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
		String userAgent = request.getHeader("User-Agent");
		response.setContentType("application/octet-stream; charset=UTF-8");
		String contentDisposition = null;
		if (StringUtil.notEmpty(userAgent)) {
			int pos;
			if ((pos = userAgent.indexOf("MSIE")) != -1) { // 如果是 IE
				char version = userAgent.charAt(pos + 5); // "MSIE x.0" 如果是 IE 7.0 或 8.0
				if (version == '7' || version == '8') {
					contentDisposition = "attachment; filename=" + encodeURL(fileName);
				}
			}
			/* 由于新版 Tomcat 限制响应头只能输出 ASCII，输出中文也会报错，所以先屏蔽这段代码
			else if (StringUtils.containsIgnoreCase(userAgent, "android")) { // 如果是Android浏览器
				contentDisposition = "attachment; filename=\"" + MakeAndroidSafeFileName(fileName) + "\"";
			}
			*/
		}
		if (contentDisposition == null) { // default
			final String encodedFileName = encodeURL(fileName);
			contentDisposition = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
		}
		response.setHeader("Content-Disposition", contentDisposition);
	}

	private static String MakeAndroidSafeFileName(String fileName) {
		// Android 浏览器仅支持 "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ._-+,@£$€!½§~'=()[]{}0123456789";
		final char[] newFileName = fileName.toCharArray();
		boolean changed = false;
		for (int i = 0; i < newFileName.length; i++) {
			final char c = newFileName[i];
			if (!(
					(c >= 'A' && c <= 'Z') // 65 - 90
							|| (c >= 'a' && c <= 'z') // 97 - 122
							|| (c >= '0' && c <= '9') // 48 - 57
							|| "._-+,@£$€!½§~'=()[]{}".indexOf(c) != -1
			)) { // other chars
				newFileName[i] = '_';
				changed = true;
			}
		}
		return changed ? new String(newFileName) : fileName;
	}

}
