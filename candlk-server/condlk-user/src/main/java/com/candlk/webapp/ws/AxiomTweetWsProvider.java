package com.candlk.webapp.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionStage;
import javax.annotation.Resource;

import com.alibaba.fastjson2.*;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.security.AES;
import com.candlk.context.model.RedisKey;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.user.entity.AxiomTwitter;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetType;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.service.TweetUserService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import software.sava.core.accounts.Signer;
import software.sava.core.encoding.Base58;

@Slf4j
@Service
public class AxiomTweetWsProvider implements Listener, TweetWsApi {

	public static final byte[] key = "Z>vB5uO^yJ/JQf#|w6p:=Va!fY_8W+IA".getBytes(StandardCharsets.UTF_8);
	public static final AES aes = new AES(key);
	public static final Base64.Decoder decoder = Base64.getDecoder();
	private WebSocket webSocket;
	@Resource
	TweetService tweetService;
	@Resource
	TweetUserService tweetUserService;

	@Override
	public TweetProvider getProvider() {
		return TweetProvider.AXIOM;
	}

	@Override
	public void connection() {
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
				Map<String, String> tokenMap = TweetWsApi.parseCookies(cookieList);
				final String accessToken = tokenMap.get("auth-access-token");
				final String refreshToken = tokenMap.get("auth-refresh-token");
				log.info("Access Token: {}  Refresh Token: {}", accessToken, refreshToken);

				// 构建 HttpClient 并设置线程池
				HttpClient client = HttpClient.newBuilder().executor(TweetWsApi.WS_EXECUTOR).build();

				// 建立连接
				this.webSocket = client.newWebSocketBuilder()
						.header("Origin", "https://axiom.trade")
						.header("cookie", "auth-refresh-token=" + refreshToken + "; auth-access-token=" + accessToken + ";")
						.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
						.buildAsync(URI.create("wss://cluster3.axiom.trade/"), new AxiomTweetWsProvider())
						.join();
			}
		}
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		System.out.println("[WebSocket] Connected.");

		// 订阅：活跃账户列表变更事件
		// webSocket.sendText("{\"action\":\"join\",\"room\":\"twitter_active_list\"}", true);
		// 订阅：帖子变更事件（活跃账户）
		webSocket.sendText("{\"action\":\"join\",\"room\":\"twitter_feed_v2\"}", true);

		Listener.super.onOpen(webSocket);
	}

	public static final List<String> eventTypes = List.of(
			"tweet.update", // 帖子变更事件（活跃账户）
			"following.update", // 账户关注列表变更
			"profile.update", // 用户的资料信息有变更
			"profile.pinned.update" // 置顶推文
	);

	public static final SimpleDateFormat SDF = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// 并发处理消息（交给线程池）
		WS_EXECUTOR.submit(() -> {
			// System.out.println("[Message] " + data);

			AxiomTwitter axiomTwitter = Jsons.parseObject(data.toString(), AxiomTwitter.class);
			if (axiomTwitter != null) {
				AxiomTwitter.Content content = axiomTwitter.content;
				final String event = content.event;
				String[] split = event.split(":");
				byte[] iv = decoder.decode(split[0]);
				try {
					final byte[] decrypt = aes.decrypt(decoder.decode(split[1]), iv);
					JSONObject postInfo = JSON.parseObject(decrypt);
					// System.out.println("解密：" + Jsons.encode(postInfo));
					final String eventType = content.eventType;
					if (eventTypes.contains(eventType)) {
						log.info("账户={} 订阅类型={} 事件ID={} 事件内容={}", content.handle, content.subscriptionType, content.eventId, Jsons.encode(postInfo));
					} else {
						log.warn("未知事件类型：账户={} 订阅类型={} 事件ID={} 事件内容={}", content.handle, content.subscriptionType, content.eventId, Jsons.encode(postInfo));
					}
					switch (eventType) {
						case "tweet.update" -> {
							JSONObject tweet = postInfo.getJSONObject("tweet");
							if (tweet != null) {
								// 解析并推文数据
								final String tweetId = tweet.getString("id");
								TweetType tweetType = TweetType.of(tweet.getString("type").toLowerCase());
								final String author = axiomTwitter.content.handle;
								JSONObject body = tweet.getJSONObject("body");
								// 使用正则表达式去除以 https://t.co/ 开头的推文尾部 短链接
								final String text = body.getString("text").replaceAll("https://t\\.co/\\S+", "").trim();

								JSONObject media = tweet.getJSONObject("media");
								// 引用图片
								JSONArray images = media.getJSONArray("images");
								// 引用的视频
								JSONArray videos = media.getJSONArray("videos");

								final Date now = new Date();
								Tweet tweetInfo = new Tweet()
										.setProviderType(TweetProvider.AXIOM)
										.setType(tweetType)
										.setUsername(author)
										.setTweetId(tweetId)
										.setText(text)
										.setOrgMsg(postInfo.toJSONString())
										.setImages(images == null ? "" : images.toJSONString())
										.setVideos(videos == null ? "" : videos.toJSONString())
										.setUpdateTime(now);
								try {
									// 默认返回 0 时区时间
									Date date = SDF.parse(tweet.getString("created_at"));
									tweetInfo.setAddTime(date);
								} catch (Exception e) {
									log.error("【{}】解析日期失败：", getProvider(), e);
									tweetInfo.setAddTime(now);
								}
								try {
									// 添加推文
									tweetService.save(tweetInfo);

									if (!tweetUserService.updateTweetLastTime(author, tweetInfo.getAddTime())) {
										// Redis 记录新用户
										RedisUtil.getStringRedisTemplate().opsForSet().add(RedisKey.TWEET_NEW_USERS, author);
									}

									// TODO AI 分词并提取 代币名称和简称

									// TODO 分词匹配
								} catch (DuplicateKeyException e) { // 违反唯一约束
									log.warn("【{}】推文已存在：{}", getProvider(), tweetId);
								}

							}
						}
						case "following.update" -> {
							// TODO 更新账号统计数据
						}
					}
				} catch (GeneralSecurityException e) {
					log.error("解密失败：", e);
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

	public static void main(String[] args) throws ParseException {
		String input = "Wed Apr 23 01:27:57 +0000 2025";

		// 用 SimpleDateFormat 解析
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
		Date date = sdf.parse(input);

		System.out.println(new EasyDate(date).toDateTimeString());
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		log.error("【{}】 Ws Error: ", getProvider(), error);
		Listener.super.onError(webSocket, error);
	}

	/*
		转发：
		{"eventId":"1914850477059002380","handle":"BigBagHodler","userId":1768659346500604000,"subscriptionType":"new_tweet","type":"tweet.update","tweet":{"id":"1914850477059002380","type":"RETWEET","created_at":"Wed Apr 23 01:15:04 +0000 2025","author":{"handle":"BigBagHodler","profile":{"avatar":"https://pbs.twimg.com/profile_images/1907650769194532865/jq8rQmY5_normal.jpg","name":"Zeke"}},"body":{"text":"","urls":[],"mentions":[]},"media":{"images":[],"videos":[]},"subtweet":{"id":"1914850477059002380","type":"RETWEET","created_at":"Wed Apr 23 01:15:04 +0000 2025","author":{"handle":"narracanz","profile":{"avatar":"https://pbs.twimg.com/profile_images/1912996625804627968/2pQYazzT_normal.jpg","name":"narc"}},"body":{"text":"RT @narracanz: $uponly\n\nhttps://t.co/UFNPDix8M5","urls":[{"name":"solscan.io/tx/2hbRf873e1w…","url":"https://solscan.io/tx/2hbRf873e1wQZGkT2h7MzZzL5M3ru32NNRxst2Sp5pkjSniZQHoTyawmo17BFJ3jk3pH59d3pp6ne4VwNSV8dxXp","tco":"https://t.co/UFNPDix8M5"}],"mentions":[]},"media":{"images":[],"videos":[]}}}}

		回复：
		{"eventId":"1914853717402136830","handle":"BillyM2k","userId":30699048,"subscriptionType":"new_tweet","type":"tweet.update","tweet":{"id":"1914853717402136830","type":"REPLY","created_at":"Wed Apr 23 01:27:57 +0000 2025","author":{"handle":"BillyM2k","profile":{"avatar":"https://pbs.twimg.com/profile_images/1763798368470663168/RgrEadkn_normal.jpg","name":"Shibetoshi Nakamoto"}},"body":{"text":"@avery_bartlett https://t.co/dwiWX7Ukg7","urls":[],"mentions":[{"id":"1486713888045350912","name":"Averyᴸ¹","handle":"avery_bartlett"}]},"media":{"images":[],"videos":[]},"subtweet":{"id":"1914852447769460815","type":"TWEET","created_at":"2025-04-23T01:27:57.976Z","author":{"handle":"avery_bartlett","verified":false,"profile":{"avatar":"https://pbs.twimg.com/profile_images/1813920742901219328/o8k-daj6_normal.jpg","name":"Averyᴸ¹"}},"body":{"text":"@BillyM2k Now you have to think your own thoughts","urls":[],"mentions":[{"id":"30699048","name":"Shibetoshi Nakamoto","handle":"BillyM2k"}]},"media":{"images":[],"videos":[]}}}}

		发帖：
		{"eventId":"1914853997451862523","handle":"AutismCapital","userId":1297651178256257000,"subscriptionType":"new_tweet","type":"tweet.update","tweet":{"id":"1914853997451862523","type":"TWEET","created_at":"Wed Apr 23 01:29:03 +0000 2025","author":{"handle":"AutismCapital","profile":{"avatar":"https://pbs.twimg.com/profile_images/1607967887305887745/pN-Bl6pc_normal.jpg","name":"Autism Capital 🧩"}},"body":{"text":"Coming home to enjoy our RFK approved treat https://t.co/GjZE3Ckbzo","urls":[],"mentions":[]},"media":{"images":["https://pbs.twimg.com/media/GpLtj_Wa4AAq0No.jpg","https://pbs.twimg.com/media/GpLtj_WbEAA-VnV.jpg"],"videos":[]}}}

		引用：
		{"eventId":"1914854855593939212","handle":"The__Solstice","userId":2650025562,"subscriptionType":"new_tweet","type":"tweet.update","tweet":{"id":"1914854855593939212","type":"QUOTE","created_at":"Wed Apr 23 01:32:28 +0000 2025","author":{"handle":"The__Solstice","profile":{"avatar":"https://pbs.twimg.com/profile_images/1887664813406588928/UKUkNPdP_normal.jpg","name":"TheS◎Lstice"}},"body":{"text":"Actually insane","urls":[],"mentions":[]},"media":{"images":[],"videos":[]},"subtweet":{"id":"1914737450972516483","type":"TWEET","created_at":"2025-04-23T01:32:31.205Z","author":{"handle":"kkashi_yt","verified":false,"profile":{"avatar":"https://pbs.twimg.com/profile_images/1748436185700319232/Gnb6EO6K_normal.jpg","name":"Kakashi"}},"body":{"text":"@whale_alert Answer from google. https://t.co/HbmMrPOg6E","urls":[],"mentions":[{"id":"1039833297751302144","name":"Whale Alert","handle":"whale_alert"}]},"media":{"images":["https://pbs.twimg.com/media/GpKDj4eaUAAWQ5z.jpg"],"videos":[]}}}}

		关注者更新：
		{"eventId":"6a1f3178-b1ed-46d8-b47d-5ec9e9a18b75","handle":"ShockedJS","userId":1216547400170582000,"subscriptionType":"new_following","type":"following.update","user":{"id":"1216547400170582016","handle":"ShockedJS","jointed_at":"Mon Jan 13 02:28:07 +0000 2020","profile":{"name":"JS","location":"Onchain Trader","avatar":"https://pbs.twimg.com/profile_images/1820704145604591617/C8cT-FR5_normal.jpg","banner":"https://pbs.twimg.com/profile_banners/1216547400170582016/1648194317","pinned":["1908753252088152448"],"url":{"name":"https://t.co/CPlRIApBHH","url":"https://t.co/CPlRIApBHH","tco":"https://t.co/CPlRIApBHH"},"description":{"text":"Building @Shocked | Free Discord: https://t.co/QMf33avHpN","urls":[{"url":"https://t.co/CPlRIApBHH","expanded_url":"https://dashboard.shocked.io","display_url":"dashboard.shocked.io","indices":[0,23]}]}},"metrics":{"media":715,"tweets":9987,"following":3559,"followers":62622}},"following":{"id":"1911079849424248833","handle":"adnangajan","jointed_at":"Sat Apr 12 15:32:03 +0000 2025","profile":{"name":"Adnan Gajan | @tokeryfinance 🔗","avatar":"https://pbs.twimg.com/profile_images/1914538609467596800/VhyK4kxy_normal.jpg","banner":"https://pbs.twimg.com/profile_banners/1911079849424248833/1744645903","pinned":["1912460744735490308"],"url":{"name":"http://www.tokery.finance","url":"http://www.tokery.finance","tco":"http://www.tokery.finance"},"description":{"text":"bringing traditional finance on-chain | Co-Founder @tokeryfinance & @tokeryfdn | Former @LGUS and @simonpropertygp","urls":[]}},"metrics":{"media":6,"tweets":60,"following":55,"followers":2736}}}

		个人资料更新：
		{"eventId":"3d42e695-134e-4cbd-837c-08d5170f0b2d","handle":"CryptoGatsu","userId":1346970305500115000,"subscriptionType":"user_update","type":"profile.update","user":{"id":1346970305500115000,"handle":"CryptoGatsu","jointed_at":"Thu Jan 07 00:02:09 +0000 2021","profile":{"name":"cryptogatsu","avatar":"https://pbs.twimg.com/profile_images/1904270004532760576/G6J3qOB8_normal.jpg","banner":"https://pbs.twimg.com/profile_banners/1346970305500114944/1744112817","pinned":["1914909941397381392"],"url":{"name":"https://t.co/ueRaPLTuEy","url":"https://t.co/ueRaPLTuEy","tco":"https://t.co/ueRaPLTuEy"},"description":{"text":"100x each week","urls":[{"url":"https://t.co/ueRaPLTuEy","expanded_url":"https://axiom.trade/@gatsu","display_url":"axiom.trade/@gatsu","indices":[0,23]}]}},"metrics":{"media":12668,"tweets":42050,"following":847,"followers":15041}},"before":{"id":1346970305500115000,"handle":"CryptoGatsu","jointed_at":"Thu Jan 07 00:02:09 +0000 2021","profile":{"name":"cryptogatsu","avatar":"https://pbs.twimg.com/profile_images/1904270004532760576/G6J3qOB8_normal.jpg","banner":"https://pbs.twimg.com/profile_banners/1346970305500114944/1744112817","pinned":["1912705252605992998"],"url":{"name":"https://t.co/ueRaPLTuEy","url":"https://t.co/ueRaPLTuEy","tco":"https://t.co/ueRaPLTuEy"},"description":{"text":"100x each week","urls":[{"url":"https://t.co/ueRaPLTuEy","expanded_url":"https://axiom.trade/@gatsu","display_url":"axiom.trade/@gatsu","indices":[0,23]}]}},"metrics":{"media":12668,"tweets":42050,"following":847,"followers":15041}}}

		置顶推文：
		{"eventId":"3bed7ffe-9c59-4d84-9610-f7acab4ecdd6","handle":"MarioNawfal","userId":1319287761048723500,"subscriptionType":"user_update","type":"profile.pinned.update","user":{"id":"1319287761048723458","handle":"MarioNawfal","jointed_at":"Thu Oct 22 14:42:25 +0000 2020","profile":{"name":"Mario Nawfal","avatar":"https://pbs.twimg.com/profile_images/1670905743619268609/pYItlWat_normal.jpg","banner":"https://pbs.twimg.com/profile_banners/1319287761048723458/1743760560","pinned":["1914444031305851045"],"url":{"name":"https://t.co/Lru9VAixI8","url":"https://t.co/Lru9VAixI8","tco":"https://t.co/Lru9VAixI8"},"description":{"text":"Largest Show on X | Founder @ibcgroupio | Investor 600+ Startups","urls":[{"url":"https://t.co/Lru9VAixI8","expanded_url":"https://roundtable.live","display_url":"roundtable.live","indices":[0,23]}]}},"metrics":{"media":97422,"tweets":128790,"following":46786,"followers":2208818}},"pinned":[{"id":"1914444031305851045","type":"QUOTE","created_at":"Mon Apr 21 22:20:00 +0000 2025","author":{"handle":"MarioNawfal","profile":{"avatar":"https://pbs.twimg.com/profile_images/1670905743619268609/pYItlWat_normal.jpg","name":"Mario Nawfal"}},"body":{"text":"🚨 EXCLUSIVE: TECH LEADER & FIRST INVESTOR IN OPEN AI SPEAKS OUT\n\nIn this exclusive interview, Vinod Khosla reveals why AI will trigger mass job loss by 2030, global abundance by 2050—and a new kind of war for control of the future.\n\n@vkhosla: \n\n“Strength in AI will determine economic strength—as a different kind of weapon of war.”\n\nPremieres tomorrow, only on 𝕏","urls":[],"mentions":[{"id":"42226885","name":"Vinod Khosla","handle":"vkhosla"}]},"media":{"images":[],"videos":["https://video.twimg.com/amplify_video/1914333561668141056/vid/avc1/480x270/QvvpAU2H7lBFfW1L.mp4?tag=16"]},"subtweet":{"id":"1909702790613811631","type":"TWEET","created_at":"2025-04-23T05:44:24.703Z","author":{"handle":"MarioNawfal","verified":false,"profile":{"avatar":"https://pbs.twimg.com/profile_images/1670905743619268609/pYItlWat_normal.jpg","name":"Mario Nawfal"}},"body":{"text":"🚨🇽🇰EXCLUSIVE INTERVIEW – KOSOVO PRESIDENT: “WE’RE THE MOST PRO-AMERICAN COUNTRY ON EARTH!”\n\nStatues of Bill Clinton. Streets named after him. And 97% of the country backing the U.S.—whether it’s a Democrat or Republican in power.\n\nKosovo’s President @VjosaOsmaniPRKS opens up in a wide-ranging, unfiltered conversation on war, survival, NATO, and why Kosovars still see America as their greatest ally.\n\nShe details growing up under Serbian oppression. The genocide her people endured. And why Kosovo’s path to independence is nothing like Crimea, Donbas, or South Ossetia.\n\n“Everywhere around Kosovo, the pain is so deep. But you will never, ever meet anyone that wants revenge. Only justice, no revenge.”\n\nFrom fighting for freedom to joining NATO and confronting China’s growing influence—this is the insider view from Europe’s youngest country, told by the woman leading it.\n\n4:26 - How was Kosovo part of Yugoslavia, and how did it become an independent country?\n\n7:11 - An explanation of the history of Kosovo.\n\n11:12 - “This was a war against innocent women and children.”\n\n12:22 - “Kosovo is the most pro-American nation on Earth.”\n\n14:51 - What’s President Osmani’s take on NATO as an alliance and its military actions beyond Kosovo in recent years?\n\n18:02 - NATO’s intervention in Kosovo had a lasting positive impact beyond securing the people’s survival and independence.\n\n22:41 - Would it be fair to argue that NATO’s role in Kosovo wasn’t as purely defensive as its actions in the Middle East?\n\n24:50 - Historically, Kosovo has played a role in defending Western countries.\n\n27:10 - Kosovo has a statue built of Bill Clinton and a boulevard named after him?\n\n28:55 - Was NATO right to expand eastward after promising Russia it wouldn’t? How much did that influence Putin’s decision to invade Ukraine?\n\n30:02 - Is NATO’s eastward expansion provocative?\n\n33:49 - “The moment a leader puts human suffering as the centerpiece of its decision-making, that’s when peace is possible.”\n\n34:42 - With U.S.–Russia ties improving, does President Osmani believe NATO is still a necessary alliance?\n\n38:42 - How much did NATO’s eastward expansion influence Putin’s invasion? What are President Osmani’s thoughts on Putin’s use of the Kosovo precedent to justify it?\n\n40:15 - If China had missile bases near the U.S., wouldn’t it be seen as a threat—like the Monroe Doctrine suggests? So, why wouldn’t Russia see NATO’s buildup the same way?\n\n44:10 - The common link between both cases is the similar crimes committed—Milosevic in Kosovo and Putin in Ukraine.\n\n46:17 - Does President Osmani think a U.S.–Russia alliance is possible, especially with China rising as a major threat? Would she support such an alliance?\n\n49:08 - Would the world be better today if NATO had welcomed Russia in the 2000s?\n\n51:08 - If Russia joined NATO, would that undermine NATO’s purpose—since it was built to counter Russia?\n\n54:02 - What are President Osmani’s thoughts on Ukraine nearly signing a peace deal in March 2022? Does she think NATO is truly incentivized to end conflicts, or does its structure complicate that?\n\n58:41 - What’s President Osmani’s take on USAID’s role and recent criticism, especially given Kosovo’s status as a recipient of USAID?\n\n1:04:56 - How is President Osmani adapting to Trump’s America, given his criticism of the EU and NATO?\n\n1:07:47 - “We now live in peace because of American leadership.”\n\n1:09:53 - Do the EU’s actions in Romania, Poland, and Hungary—like election interference—worry President Osmani as Kosovo seeks to join?\n\n1:13:03 - President Osmani shares what she experienced during the 1999 war in Kosovo before U.S. intervention.\n\n1:20:11 - “Everywhere around Kosovo, the pain is so deep. But you will never, ever meet anyone that wants revenge.”","urls":[],"mentions":[{"id":"95693222","name":"Vjosa Osmani","handle":"VjosaOsmaniPRKS"}]},"media":{"images":[],"videos":["https://video.twimg.com/amplify_video/1909624502733905921/vid/avc1/480x270/9l1eGKjuE49sbNfF.mp4?tag=16"]}}}]}
	 */
}