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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.util.BaseHttpUtil;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetType;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.service.TweetUserService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class X3TweetWsProvider implements Listener, TweetWsApi {

	public WebSocket webSocket;
	@Resource
	TweetService tweetService;

	@Override
	public TweetProvider getProvider() {
		return TweetProvider.X3;
	}

	@Override
	public void connection() {
		log.info("【{}】开始建立连接！", getProvider());
		// 建立连接
		this.webSocket = BaseHttpUtil.defaultClient().newWebSocketBuilder()
				.connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
				.header("Origin", "https://www.x3.pro")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
				.buildAsync(URI.create("wss://www.x3.pro/api/ws?Authorization=" + UUID.randomUUID()), this)
				.join();
	}

	private Long lastTime = System.currentTimeMillis();

	@Override
	public Long getLastTime() {
		return lastTime;
	}

	@Override
	public boolean ping() {
		webSocket.sendText("ping", true);
		return checkPing();
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		// 订阅：包含CA的所有帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"CA_ALL\"}", true);
		// 订阅：焦点帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"FOCUS\"}", true);
		// 订阅：账号监听帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"MONITOR\"}", true);
		// 订阅：实时帖子
		webSocket.sendText("{\"action\":1,\"topic\":\"REALTIME\"}", true);
		// 订阅：账号剃刀帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"SCRAPER_NOTICE\"}", true);

		// 定时任务间隔1分钟发送一次ping心跳

		Listener.super.onOpen(webSocket);
		log.info("【{}】建立连接成功！", getProvider());
	}

	public static final String CDN = "https://x3-media-pro-1.oss-cn-hongkong.aliyuncs.com/";

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// 并发处理消息（交给线程池）
		WS_EXECUTOR.submit(() -> {
			TweetProvider provider = getProvider();
			log.info("【{}】事件内容：{}", provider, data);
			lastTime = System.currentTimeMillis();
			JSONObject tweetData = JSON.parseObject(data.toString());
			if ("1".equals(tweetData.getString("msgType"))) {

				try {
					List<JSONObject> posts = tweetData.getJSONObject("data").getList("data", JSONObject.class);

					final Date now = new Date();
					for (JSONObject post : posts) {
						// 排除包含 CA 的推文
						final String tokens = post.getString("tokens");
						if (StringUtils.isNotEmpty(tokens)) {
							continue;
						}
						JSONObject originPost = post.getJSONObject("originPost");
						TweetType tweetType = originPost == null ? TweetType.TWEET : TweetType.QUOTE;

						JSONObject authorInfo = post.getJSONObject("author");
						final String author = authorInfo.getString("screenName");
						final String tweetId = post.getString("id").replaceFirst("x_", "");

						List<JSONObject> medias = post.getList("medias", JSONObject.class);
						// 引用图片 和 视频
						List<String> images = new ArrayList<>(), videos = new ArrayList<>();

						if (medias != null) {
							for (JSONObject media : medias) {
								Integer type = media.getInteger("type");
								(type.equals(2) ? videos : images).add(CDN + media.getString("path"));
							}
						}
						Long createTime = post.getLong("createTime");
						final String content = post.getString("content");
						if (StringUtils.isNotEmpty(content)) {
							Tweet tweetInfo = new Tweet()
									.setProviderType(provider)
									.setType(tweetType)
									.setUsername(author)
									.setTweetId(tweetId)
									.setText(content.trim())
									.setOrgMsg(post.toJSONString())
									.setImages(images)
									.setVideos(videos)
									.setUpdateTime(now)
									.setAddTime(createTime == null ? now : new EasyDate(createTime * 1000).toDate());

							final TweetUser tweetUser = new TweetUser()
									.setProviderType(provider)
									.setUserId(authorInfo.getString("id").replaceFirst("x_", ""))
									.setUsername(author)
									.setNickname(authorInfo.getString("name"))
									.setAvatar(CDN + authorInfo.getString("avatar"))
									.setBanner(CDN + authorInfo.getString("bk"))
									.setDescription(JSONObject.of("text", authorInfo.getString("introduction")).toJSONString())
									.setFollowers(authorInfo.getInteger("fanCount"))
									.setFollowing(authorInfo.getInteger("focusCount"));
							createTime = authorInfo.getLong("createTime");
							tweetUser.setAddTime(createTime == null ? now : new EasyDate(createTime * 1000).toDate());
							tweetUser.setUpdateTime(now);

							tweetService.saveTweet(tweetInfo, author, provider, tweetId, tweetUser);
						}
					}
				} catch (Exception e) {
					log.error("【{}】解析数据失败：", provider, e);
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