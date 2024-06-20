package com.candlk.webapp.action;

import java.net.InetSocketAddress;
import java.net.Proxy;

import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.common.web.Ready;
import com.candlk.context.web.Jsons;
import com.candlk.context.web.ProxyRequest;
import com.candlk.webapp.job.XAIPowerJob;
import com.candlk.webapp.job.XAIRedemptionJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/test")
public class TestAction {

	@Value("${service.proxy.host}")
	private String host;
	@Value("${service.proxy.port}")
	private Integer port;

	@Resource
	XAIPowerJob xaiPowerJob;
	@Resource
	XAIRedemptionJob xaiRedemptionJob;

	@Ready("墙外访问测试")
	@GetMapping("/ping")
	public Messager<String> addOrEdit(ProxyRequest q) {
		String tgMsg = "https://api.telegram.org/bot7098739919:AAG7V8jhpmhehF9Z5ZHL6YgA9qmmpkwV3Zg/sendMessage?chat_id=-1002081472730&text=这是一条测试消息&parse_mode=Markdown";
		RestTemplate restTemplate = new RestTemplate();
		SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
		reqfac.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		restTemplate.setRequestFactory(reqfac);
		JSONObject body = restTemplate.getForEntity(tgMsg, JSONObject.class).getBody();
		return Messager.OK(Jsons.encode(body));
	}

	@Ready("强刷排名")
	@GetMapping("/flushPower")
	public Messager<Void> flushPower(ProxyRequest q) throws Exception {
		xaiPowerJob.run();
		// xaiRedemptionJob.run();
		return Messager.OK();
	}

}
