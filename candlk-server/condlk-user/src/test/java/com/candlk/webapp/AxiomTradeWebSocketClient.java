package com.candlk.webapp;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.ws.AxiomWsListener;
import com.candlk.webapp.ws.WsListenerApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import software.sava.core.accounts.Signer;
import software.sava.core.encoding.Base58;

@Slf4j
public class AxiomTradeWebSocketClient {

	public static void main(String[] args) throws InterruptedException, GeneralSecurityException {
		final String walletPrivateKey = "2xJ1YaDTjYAfwomwiDQsiz6h16XJtxmG53XWnsSkppMbEkTmC1LL2Se9DDHfaQZL4J1chZ4ZHDvkFMpW6fCqq2Rg";
		final byte[] secretKey = Base58.decode(walletPrivateKey);
		Signer signer = Signer.createFromKeyPair(secretKey);

		final String signerAddress = signer.publicKey().toBase58();
		log.info("钱包地址：{}", signerAddress);

		RestTemplate restTemplate = new RestTemplate();
		// SimpleClientHttpRequestFactory reqFac = new SimpleClientHttpRequestFactory();
		// reqFac.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10818)));
		// restTemplate.setRequestFactory(reqFac);

		final HttpEntity<JSONObject> httpEntity = new HttpEntity<>(JSONObject.of(
				"walletAddress", signerAddress
		), new HttpHeaders());
		final String nonce = restTemplate.postForEntity("https://api3.axiom.trade/wallet-nonce", httpEntity, String.class).getBody();
		if (JSON.isValid(nonce)) {
			log.warn("获取随机Nonce失败：" + nonce);
			return;
		}
		log.info("获取随机Nonce：" + nonce);

		final String loginMessage = "\t\t By signing, you agree to Axiom's Terms of Use & Privacy Policy (axiom.trade/legal).\n\nNonce: " + nonce;
		// 将消息编码为 UTF-8 字节
		byte[] messageBytes = loginMessage.trim().getBytes(StandardCharsets.UTF_8);

		// 直接使用 signer 签名！
		byte[] signatureBytes = signer.sign(messageBytes);

		// 编码为 base58
		final String base58Signature = Base58.encode(signatureBytes);

		// 输出签名
		log.info("签名（Base58）：" + base58Signature);

		ResponseEntity<String> loginResp = restTemplate.postForEntity("https://api3.axiom.trade/verify-wallet-v2", JSONObject.of(
				"walletAddress", signerAddress,
				"signature", base58Signature,
				"nonce", nonce,
				"referrer", null,
				"allowRegistration", false
		), String.class);
		final String loginData = loginResp.getBody();
		if (JSON.isValid(loginData) && !loginData.contains("error")) {
			log.info("登录结果：" + Jsons.encode(loginData));

			final HttpHeaders headers = loginResp.getHeaders();
			log.info("Cookie：" + Jsons.encode(headers));

			final List<String> cookieList = headers.get(HttpHeaders.SET_COOKIE);
			if (!CollectionUtils.isEmpty(cookieList)) {
				Map<String, String> tokenMap = WsListenerApi.parseCookies(cookieList);
				final String accessToken = tokenMap.get("auth-access-token");
				final String refreshToken = tokenMap.get("auth-refresh-token");
				log.info("Access Token: {}  Refresh Token: {}", accessToken, refreshToken);

				runListener(accessToken, refreshToken);
			}
		}
	}

	public static void runListener(String accessToken, String refreshToken) throws InterruptedException {
		// 构建 HttpClient 并设置线程池
		HttpClient client = HttpClient.newBuilder().executor(WsListenerApi.WS_EXECUTOR).build();

		// 建立连接
		client.newWebSocketBuilder()
				.header("Origin", "https://axiom.trade")
				.header("cookie", "auth-refresh-token=" + refreshToken + "; auth-access-token=" + accessToken + ";")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
				.buildAsync(URI.create("wss://cluster3.axiom.trade/"), new AxiomWsListener())
				.join();

		// 阻塞主线程
		Thread.currentThread().join();
	}

}
