package com.candlk.webapp;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.context.web.Jsons;
import me.codeplayer.util.RandomUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class SendMessageTest {

	public static void main(String[] args) {
		List<String> poolNames = new ArrayList<>() {{
			add("GCRClassic🈸");
			add("GCRClassic");
			add("XaiBhais🈸");
			add("冰蛙IceFrog");
			add("ALPHA1");
			add("ALPHA3");
			add("OnePiece");
			add("冰蛙Ice_Frog2");
			add("ALPHA7Franch🈸");
			add("ALPHA6🈸");
			add("ALPHA5Franch🈸");
			add("XAI_fans");
			add("helloworld");
			add("LongXiaPool");
			add("OnlyKeysNeed🈸");
			add("TOGETHER🈸");
			add("XAI_END_GAME");
			add("LFG!Capital");
			add("CryptoTelugu");
			add("Keyholders");
			add("Arbitrum");
			add("GodBlessChin");
			add("GodBlessChin🈸");
		}};
		StringBuilder sb = new StringBuilder("*\uD83D\uDCB910000/EsXAI算力排行榜*\n\n*排名            池子     	                      算力     加成      总质押      活跃* \n");

		int counter = 1;
		for (String poolName : poolNames) {
			int len = poolName.length();
			final boolean hasEmoji = poolName.contains("🈸");
			sb.append(counter).append("           ").append(counter < 10 ? "  " : "")
					.append(poolName.replaceAll("_", "\\\\_")).append(hasEmoji ? "    " : "")
			;
			int offset = 18, maxLen = 12/*合约名*/, offsetEmoji = 15, end = offset - (hasEmoji ? offsetEmoji : 6) + maxLen - len;
			sb.append(" ".repeat(Math.max(0, end)));
			sb.append(" 	        ").append("×").append(RandomUtil.getInt(1, 6));
			final String v = RandomUtil.getInt(10, 500) + "w";
			sb.append(" 	        ").append(v);
			sb.append(v.length() > 3 ? "        " : "          ").append(RandomUtil.getInt(1, 2) > 1 ? "❌" : "✅");
			sb.append("\n");

			counter++;
		}

		sendTelegramMessage(sb.toString());
	}

	public static void sendTelegramMessage(String content) {
		String url = "https://api.telegram.org/bot7098739919:AAG7V8jhpmhehF9Z5ZHL6YgA9qmmpkwV3Zg/sendMessage";
		String host = "127.0.0.1";
		int port = 10808;
		RestTemplate restTemplate = new RestTemplate();
		SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
		reqfac.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		restTemplate.setRequestFactory(reqfac);
		final HttpEntity<JSONObject> httpEntity = new HttpEntity<>(JSONObject.of(
				"chat_id", "-1002081472730",
				"parse_mode", "Markdown",
				"text", content
		), new HttpHeaders());
		JSONObject body = restTemplate.postForEntity(url, httpEntity, JSONObject.class).getBody();
		if (body.getBoolean("ok")) {
			System.out.println("发送Telegram成功：" + Jsons.encode(body));
		} else {
			System.out.println("发送Telegram失败：" + Jsons.encode(body));
		}
	}

}
