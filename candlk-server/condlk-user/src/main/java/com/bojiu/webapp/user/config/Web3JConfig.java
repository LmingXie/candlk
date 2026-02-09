package com.bojiu.webapp.user.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Slf4j
@Setter
@Getter
@Configuration
@Scope("singleton")
@ConfigurationProperties(prefix = "service.user")
public class Web3JConfig {

	@Setter
	@Getter
	public static class Web3jUrl {

		public String url;
		public String referer;
		public String origin;

	}

	public Web3jUrl web3jUrl;

	public List<Web3jUrl> web3jUrlPool = new ArrayList<>();

	@Value("${service.proxy.host}")
	private String host;
	@Value("${service.proxy.port}")
	private Integer port;

	private String tgMsgHookUrl;
	private String tgChatId;
	private String rankTgChatId;

	/** 需要监听的发送者请求（小写）地址 -> 备注 */
	public Map<String, String> spyFroms = new HashMap<>();

	private static final RestTemplate restTemplate;
	public RestTemplate proxyRestTemplate;

	static {
		final SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
		httpRequestFactory.setReadTimeout(10000);
		httpRequestFactory.setConnectTimeout(5000);
		restTemplate = new RestTemplate(httpRequestFactory);
	}

	private volatile int offset = 0, maxOffset;
	public List<Web3j> web3jPool = new ArrayList<>();

	@PostConstruct
	public void init() {
		this.maxOffset = web3jUrlPool.size() - 1;
		for (Web3jUrl url : web3jUrlPool) {
			web3jPool.add(createWeb3j(url));
		}

		// 初始化RestTemplate代理
		proxyRestTemplate = new RestTemplate();
		final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		proxyRestTemplate.setRequestFactory(factory);
	}

	public void exec(int retry, Consumer<Web3j> task) {
		final Object[] objs = pollGetWeb3j();
		final Web3j web3j = (Web3j) objs[0];   // 移到里面来！

		try {
			task.accept(web3j);
		} catch (Exception e) {
			log.info("接口被限制，进行重试，当前：{} -> {}", retry, web3jUrlPool.get(offset).url);
			if (retry > 0) {
				exec(retry - 1, task);
			} else {
				try {
					Thread.sleep(300);
				} catch (InterruptedException ignore) {
				}
				throw new RuntimeException("重试次数已用尽", e);
			}
		}
	}

	public synchronized Web3j pollingGetWeb3j() {
		return (Web3j) pollGetWeb3j()[0];
	}

	public synchronized Object[] pollGetWeb3j() {
		final Web3j web3j = web3jPool.get(offset);
		if (offset++ >= maxOffset) {
			offset = 0;
		}
		return new Object[] { web3j, offset };
	}

	private transient Web3j web3jCache;

	public Web3j createWeb3j(Web3jUrl web3jUrl) {
		final HttpService service = new HttpService(web3jUrl.url);
		if (web3jUrl.referer != null) {
			service.addHeader("referer", web3jUrl.referer);
		}
		if (web3jUrl.origin != null) {
			service.addHeader("origin", web3jUrl.origin);
		}
		return Web3j.build(service);
	}

	@Bean
	public Web3j getWeb3j() {
		return web3jCache = createWeb3j(web3jUrl);
	}

}

