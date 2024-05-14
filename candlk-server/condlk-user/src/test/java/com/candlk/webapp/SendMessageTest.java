package com.candlk.webapp;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
		StringBuilder sb = new StringBuilder("*\uD83D\uDCB91/Keys Stake Computing Power Rank *\n\n*Rank      Power    Tier     EsXAI      Keys    Active           Pool* \n");

		int counter = 1;
		for (String poolName : poolNames) {
			sb.append(counter).append("         ").append(counter < 10 ? "  " : "");
			sb.append(" 	  ").append(BigDecimal.valueOf(RandomUtil.getDouble()).setScale(2, RoundingMode.HALF_UP));
			sb.append(" 	        ×").append(RandomUtil.getInt(1, 6));
			final String v = RandomUtil.getInt(10, 100) + "w";
			sb.append(" 	      ").append(v).append(v.length() > 3 ? "        " : "          ");
			sb.append(RandomUtil.getInt(10, 100)).append(v.length() > 2 ? "        " : "          ");
			sb.append(RandomUtil.getInt(1, 2) > 1 ? "❌" : "✅").append("          ");
			sb.append(poolName.replaceAll("_", "\\\\_"));
			sb.append("\n");

			counter++;
		}
		sb.setLength(0);
		sb.append("*Redemption Stat*\n")
				.append("Time: *2024-05-12* \n")
				.append("\n*ToDay: * \n")
				.append("Redemption: 963,129.07XAI\n")
				.append("Burn: 1,963,129.07XAI\n")
				.append("\n*Week(2024-05-06): * \n")
				.append("Redemption: 963,129.07XAI\n")
				.append("Burn: 1,963,129.07XAI\n")
				.append("\n*Month(2024-05): * \n")
				.append("Redemption: 963,129.07XAI\n")
				.append("Burn: 1,963,129.07XAI\n")
				.append("\n*History Total: * \n")
				.append("Redemption Total: 963,129.07XAI\n")
				.append("Burn Total: 1,963,129.07XAI\n")
		;

		sendTelegramMessage(
				"\uD83D\uDCAF*Notify：Full Keys Pool Redemption !* \n\n"
						+ "Pool：[" + "冰蛙｜Ice_Frog2" + "](app.xai.games/pool/" + "0x54b0a0596c40d3c7dc6a16675d121743a0cc9e9c" + "/summary)  \n"
						+ "Keys Staked：*" + "750" + "*  \n"
						+ "EsXAI Staked：*" + "526,201 " + "*  \n"
						+ "Tier：*×" + "3" + "*  \n"
						+ "Power：*" + "5.23" + "*  \n"
						+ "[\uD83D\uDC49\uD83D\uDC49Click View](https://arbiscan.io/tx/" + "0x0054ffa71c5a38eea6b9bc179424ebbe4e9f1b14f7a4a872766c242c553a32d4" + ")");
	}

	public static String getEsXAIRank(List<String> poolNames) {

		StringBuilder sb = new StringBuilder("*\uD83D\uDCB910000/EsXAI Stake Computing Power Rank *\n\n*Rank      Power    Tier     EsXAI      Keys    Active           Pool* \n");

		int counter = 1;
		for (String poolName : poolNames) {
			sb.append(counter).append("         ").append(counter < 10 ? "  " : "");
			sb.append(" 	  ").append(BigDecimal.valueOf(RandomUtil.getDouble()).setScale(2, RoundingMode.HALF_UP));
			sb.append(" 	        ×").append(RandomUtil.getInt(1, 6));
			final String v = RandomUtil.getInt(10, 100) + "w";
			sb.append(" 	      ").append(v).append(v.length() > 3 ? "        " : "          ");
			sb.append(RandomUtil.getInt(10, 100)).append(v.length() > 2 ? "        " : "          ");
			sb.append(RandomUtil.getInt(1, 2) > 1 ? "❌" : "✅").append("          ");
			sb.append(poolName.replaceAll("_", "\\\\_"));
			sb.append("\n");

			counter++;
		}

		return sb.toString();
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
