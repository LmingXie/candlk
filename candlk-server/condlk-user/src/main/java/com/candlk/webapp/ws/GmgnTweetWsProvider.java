package com.candlk.webapp.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletionStage;
import javax.annotation.Resource;

import com.candlk.common.util.BaseHttpUtil;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetType;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.vo.GmgnTweetEventVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GmgnTweetWsProvider extends BaseHttpUtil implements Listener, TweetWsApi {

	@Resource
	TweetService tweetService;

	public WebSocket webSocket;

	final HttpClient proxyHttpClient;

	public GmgnTweetWsProvider(@Value("${service.proxy-conf}") String proxyConfig) {
		this.proxyHttpClient = getProxyOrDefaultClient(proxyConfig);
	}

	@Override
	public TweetProvider getProvider() {
		return TweetProvider.GMGN;
	}

	/** 采用JWT 更新较为频繁，实际我们只获取公共推文数据 */
	static final String token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoiRVdpZUhTbXRnUnJlRURUbndFbzVmN2VkZG11Yk1pZGFWOE10WmhkanE5NWYiLCJhdWQiOiJnbWduLmFpL2FjY2VzcyIsImNoYWluIjoic29sIiwiZGF0YSI6eyJhZGRyZXNzIjoiRVdpZUhTbXRnUnJlRURUbndFbzVmN2VkZG11Yk1pZGFWOE10WmhkanE5NWYiLCJhcHAiOiJnbWduIiwiY2hhaW4iOiJzb2wiLCJjbGllbnRfaWQiOiJnbWduX3dlYl8yMDI1MDQyMS0yNDktMDhlNGE4ZSIsImRldmljZV9pZCI6IjY0YmFlY2QwLWYwNzMtNDExYS04Yzg2LWQ3MWU4NTIzZjQ0OSIsImZhdGhlcl9pZCI6IjQzMWNiODQ4LTliNWYtNDY2Yi1iMDJkLTYxNTkwZjE5ODVmNyIsImZpbmdlcnByaW50IjoidjEzMzFjZWM3OWM2ZWY2NzIwMjg4YTU3YzFiMGEyMWE1OSIsInBsYXRmb3JtIjoid2ViIiwidXNlcl9pZCI6ImMzMmM4MWJjZC04YzFkMi1mNmMxYS05ZjM4Mi02NWMwZWNmMyJ9LCJleHAiOjE3NDY3ODQ2NTIsImlhdCI6MTc0Njc4Mjg1MiwiaXNzIjoiZ21nbi5haS9zaWduZXIiLCJqdGkiOiI5YjY4NWQzNy1mOTE2LTQxY2YtYTE5Yi00NjE0ZTNlM2VhM2QiLCJuYmYiOjE3NDY3ODI4NTIsInN1YiI6ImdtZ24uYWkvYWNjZXNzIiwidmVyIjoiMS4wIiwidmVyc2lvbiI6IjIuMCJ9.xNRE2-Q2ompRJLpQWZgtuy63HjUFWqmmyTswZttJ6-7CtEHmYmbFFIqXX4LwdRrCyIzHF7mD7XJ7iEdCOVKHtQ";

	@Override
	public void connection() {
		log.info("【{}】开始建立连接！", getProvider());
		// 建立连接
		this.webSocket = proxyHttpClient.newWebSocketBuilder()
				.connectTimeout(Duration.of(10, ChronoUnit.SECONDS))
				.header("Origin", "https://gmgn.ai")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
				.buildAsync(URI.create("wss://ws.gmgn.ai/quotation?device_id=64baecd0-f073-411a-8c86-d71e8523f449&client_id=gmgn_web_20250508-860-943999d&from_app=gmgn&app_ver=20250508-860-943999d&tz_name=Asia%2FShanghai&tz_offset=28800&app_lang=zh-CN&fp_did=718668df0879175894ff6925a2175d69&os=web&uuid=89b9f65f2ea537a6"),
						this)
				.join();
	}

	private Long lastTime = System.currentTimeMillis();

	@Override
	public Long getLastTime() {
		return lastTime;
	}

	@Override
	public boolean ping() {
		if (webSocket != null) {
			// 每30秒一次心跳
			webSocket.sendText("{\"action\":\"heartbeat\",\"client_ts\":" + System.currentTimeMillis() + "}", true);
		}
		return checkPing();
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		final String address = "EWieHSmtgRreEDTnwEo5f7eddmubMidaV8MtZhdjq95f";
		// 订阅：Twitter 特定用户监控 基础消息
		webSocket.sendText("{\"action\":\"subscribe\",\"channel\":\"twitter_monitor_basic\",\"id\":\"575a839c90321066\",\"data\":[{\"chain\":\"sol\",\"addresses\":\"" + address + "\"}]}", true);
		// 订阅：Twitter 特定用户监控 基础消息
		// webSocket.sendText("{\"action\":\"subscribe\",\"channel\":\"twitter_monitor_token\",\"id\":\"f67527668e1d666c\",\"data\":[{\"chain\":\"sol\",\"addresses\":\"" + address + "\"}]}", true);

		// 订阅：Twitter全部用户监控 基础消息
		webSocket.sendText("{\"action\":\"subscribe\",\"channel\":\"twitter_user_monitor_basic\",\"id\":\"03bc55e24cff8812\",\"access_token\":\"" + token + "\"}", true);
		// 订阅：Twitter全部用户监控 代币消息
		// webSocket.sendText("{\"action\":\"subscribe\",\"channel\":\"twitter_user_monitor_token\",\"id\":\"25238958907cf832\",\"access_token\":\"" + token + "\"}", true);

		/*
		回复 reply：{"channel":"twitter_monitor_basic","data":[{"i":"657f0f93-c012-0dd3-ec0f-7d19964f058c","tw":"reply","ti":"1920786930003824815","cp":1,"ts":"1746786265265","u":{"s":"bilal_m17","n":"🪐","a":"https://pbs.twimg.com/profile_images/1862079857384886272/m11G-w-a.jpg"},"ut":["kol"],"c":{"t":"👋"},"si":"1920785787802530275","su":{"s":"0xsenns","n":"Sen","a":"https://pbs.twimg.com/profile_images/1917193337699958784/ikbhSlmN.jpg"},"sc":{"t":"can the crab get hi? \n\n$crab","m":[{"t":"video","u":"https://video.twimg.com/amplify_video/1920785730340573184/vid/avc1/320x568/pXaWhGZXR8C8ut7N.mp4?tag=14"},{"t":"thumbnail","u":"https://pbs.twimg.com/amplify_video_thumb/1920785730340573184/img/L5ywxzZOqolEO2mL.jpg"}]}}]}
		发推 tweet：{"channel":"twitter_monitor_basic","data":[{"i":"777d5401-50b6-c540-4f28-367b5687a88d","tw":"tweet","ti":"1920787165954400556","ts":"1746786321520","u":{"s":"zaDegenApe","n":"degenApe","a":"https://pbs.twimg.com/profile_images/1882086118687973376/mUomRThn.jpg"},"ut":["kol"],"c":{"t":"My retarded ass is saying this will trade in millies sooner than you think\n\nValue you time, value #timecoin \n\n2iuxs3tH1M7PpmVNh7uj3Aree7YuMiPjDDaeyRbnpump","m":[{"t":"image","u":"https://pbs.twimg.com/media/GqgBvjhWcAA0YDA.jpg"}]}}]}
		转帖 repost：{"channel":"twitter_monitor_basic","data":[{"i":"1777e40d-1c43-8c72-dd23-41cc553b00fe","tw":"repost","ti":"1920787295000551833","cp":1,"ts":"1746786352287","u":{"s":"trader1sz","n":"TraderSZ","a":"https://pbs.twimg.com/profile_images/1887996708203667456/0OI1eSsO.jpg"},"ut":["kol"],"c":{"t":"BREAKING: 🇺🇸 🇮🇱 🇸🇦 MASSIVE L FOR ISRAEL\n\nTrump will grant Saudi Arabia a peaceful nuclear program without demanding normalization with Israel in return — Reuters","m":[{"t":"image","u":"https://pbs.twimg.com/media/GqdYpEpWYAA8gTE.jpg"}]},"si":"1920601232080551947","su":{"s":"AdameMedia","n":"ADAM","a":"https://pbs.twimg.com/profile_images/1732468757493121025/r1P4zChc.jpg"},"sc":{"t":"BREAKING: 🇺🇸 🇮🇱 🇸🇦 MASSIVE L FOR ISRAEL\n\nTrump will grant Saudi Arabia a peaceful nuclear program without demanding normalization with Israel in return — Reuters","m":[{"t":"image","u":"https://pbs.twimg.com/media/GqdYpEpWYAA8gTE.jpg"}]}}]}
		引用 quote ：{"channel":"twitter_monitor_basic","data":[{"i":"a3e433cb-dc06-c55c-5030-8ee1d2c20874","tw":"quote","ti":"1920787487015760258","cp":1,"ts":"1746786398067","u":{"s":"BCheque1","n":"BCheque","a":"https://pbs.twimg.com/profile_images/1862210696848560128/nWvxNDt8.jpg"},"ut":["kol"],"c":{"t":"ETH is the headline today \n\nETH being up is good for everything else \n\nLooking forward to discuss"},"si":"1920784633081266576","su":{"s":"modernmarket_","n":"The Modern Market Show","a":"https://pbs.twimg.com/profile_images/1847297885295001600/61cc9cJY.jpg"},"sc":{"t":"MM in 45 mins!\n\n- ETH +26% to $2.4K; everything sending higher\n\n- Coinbase acquires largest BTC and ETH options trading platform Deribit for $2.9B\n\n- US Banks receive confirmation from OCC that they can buy/sell/custody BTC\n\nx.com/i/spaces/1ynKO…"}}]}
		简介更新 description：{"channel":"twitter_monitor_basic","data":[{"i":"68ea7ab7-9a5b-9959-80aa-ac0a7d8f2798","tw":"description","ts":"1746787043355","u":{"s":"ZKSgu","n":"孤鹤.hl","a":"https://pbs.twimg.com/profile_images/1909293802344620033/9Q3V69br.jpg"},"ut":["kol"],"p":{"d":"🌊Kaito ALL CT#24 \u0026 Ethos#12｜用 #Binance 交易🔗 binance.com/join?ref=FO3QE…｜#OKX 交易所安全\u0026钱包好用🔗 ouxyi.link/ul/6CngT5?chan…","u":[{"n":"binance.com/join?ref=FO3QE…","u":"https://www.binance.com/join?ref=FO3QE2XB"},{"n":"ouxyi.link/ul/6CngT5?chan…","u":"https://www.ouxyi.link/ul/6CngT5?channelId=8888F"}]}}]}


		更新简介 x_updated_profile_bio：
		更新头像 x_updated_profile_photo：
		关注 x_followed：
        x_description: ,
        x_photo: "更新头像",
        x_banner: "更新横幅",
		 */
		// 定时任务间隔1分钟发送一次ping心跳
		Listener.super.onOpen(webSocket);
		log.info("【{}】建立连接成功！", getProvider());
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// 并发处理消息（交给线程池）
		WS_EXECUTOR.submit(() -> {
			TweetProvider provider = getProvider();
			lastTime = System.currentTimeMillis();

			final GmgnTweetEventVO event = Jsons.parseObject(data.toString(), GmgnTweetEventVO.class);
			if (event != null && CollectionUtils.isNotEmpty(event.data)) {
				final GmgnTweetEventVO.Data vo = event.data.get(0);
				if ("repost".equals(vo.twType)) {
					return; // 暂不收录转帖
				}
				final Date now = new Date();
				switch (vo.twType) {
					case "tweet", "reply", "quote"/*,"repost"*/ -> {
						if (vo.content != null) {
							final String username = vo.user.username, tweetId = vo.tweetId;
							// log.info("【{}】账户={} 订阅类型={} ID={} 内容={}", provider, username, vo.twType, tweetId, Jsons.encode(vo));
							final Tweet tweetInfo = new Tweet()
									.setProviderType(provider)
									.setType(switch (vo.twType) {
										case "tweet" -> TweetType.TWEET;
										case "reply" -> TweetType.REPLY;
										case "quote" -> TweetType.QUOTE;
										default -> throw new IllegalStateException("Unexpected value: " + vo.twType);
									})
									.setUsername(username)
									.setTweetId(tweetId)
									.setText(vo.content.text)
									.setOrgMsg(Jsons.encode(vo))
									.setUpdateTime(now)
									.setAddTime(vo.twTimestamp);

							List<GmgnTweetEventVO.Media> media = vo.content.media;
							if (CollectionUtils.isNotEmpty(media)) {
								final List<String> images = new ArrayList<>(), videos = new ArrayList<>();
								for (GmgnTweetEventVO.Media m : media) {
									switch (m.type) {
										case "image", "thumbnail" -> images.add(m.url);
										case "video" -> videos.add(m.url);
										default -> log.info("【{}】未知媒体类型：{}", provider, m.type);
									}
								}
								tweetInfo.setImages(images).setVideos(videos);
							}

							final TweetUser tweetUser = new TweetUser()
									.setProviderType(provider)
									.setUsername(username)
									.setNickname(vo.user.name)
									.setAvatar(vo.user.avatar)
									.setTweetLastTime(now);
							tweetUser.initTime(now);
							tweetService.saveTweet(tweetInfo, username, provider, tweetId, tweetUser);
						}
					}
					case "description" -> log.info("【{}】简介更新：{}", provider, data);
					default -> log.info("【{}】未知事件类型：{} 内容：{}", provider, vo.twType, data);
				}

			}

		});
		return Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		reConnection(webSocket, statusCode, reason);
		return Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		System.err.println("[WebSocket] Error: " + error.getMessage());
		Listener.super.onError(webSocket, error);
	}

	@Override
	public WebSocket getWebSocket() {
		return webSocket;
	}

}