package com.candlk.webapp.api;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.annotation.Nonnull;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.common.util.BaseHttpUtil;
import com.candlk.context.web.Jsons;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpMethod;

/**
 * Twitter API
 */
@Getter
@Setter
@Slf4j
public class TweetApi extends BaseHttpUtil {

	public static final String baseURI = "https://api.twitter.com/2";
	/** 用户拓展字段 */
	public static final String USER_FIELDS = "&user.fields=affiliation,created_at,description,entities,id,is_identity_verified,location,most_recent_tweet_id,name,parody,pinned_tweet_id,profile_banner_url,profile_image_url,protected,public_metrics,subscription,subscription_type,url,username,verified,verified_followers_count,verified_type,withheld";
	/** 推文拓展字段 */
	public static final String TWEET_FIELDS = "&tweet.fields=article,attachments,author_id,card_uri,community_id,context_annotations,conversation_id,created_at,display_text_range,edit_controls,edit_history_tweet_ids,entities,geo,id,in_reply_to_user_id,lang,media_metadata,note_tweet,possibly_sensitive,public_metrics,referenced_tweets,source,text,withheld";
	/** 媒体拓展字段 */
	public static final String MEDIA_FIELDS = "&media.fields=alt_text,duration_ms,height,media_key,preview_image_url,public_metrics,type,url,variants,width";

	final String authToken;
	final HttpClient proxyHttpClient;

	public TweetApi(String authToken, String proxyConfig) {
		Assert.notBlank(authToken);
		this.authToken = authToken;

		this.proxyHttpClient = getProxyOrDefaultClient(proxyConfig);
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doRequest(HttpMethod method, URI uri, String body) {
		HttpRequest.Builder builder = requestBuilder(method, uri, body, true);
		builder.setHeader("Authorization", "Bearer " + authToken);
		String responseBody = null;
		Exception ex = null;
		try {
			HttpResponse<String> response = doSend(this.proxyHttpClient, builder.build(), responseBodyHandler);
			responseBody = response.body();
			if (response.statusCode() == 200) {
				JSONObject json = Jsons.parseObject(responseBody);
				return Messager.hideData(json).setExt(responseBody);
			}
			return new Messager<JSONObject>().setExt(responseBody);
		} catch (IOException | IllegalThreadStateException e) {
			ex = e;
			return Messager.status(Messager.ERROR);
		} finally {
			if (ex == null) {
				log.info("【Twitter】请求地址：{}\n请求参数：{}\n返回数据：{}", uri, body, responseBody);
			} else {
				log.error("【Twitter】请求地址：" + uri + "\n请求参数：" + body, ex);
			}
		}
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doGet(URI uri) {
		return doRequest(HttpMethod.GET, uri, null);
	}

	public Messager<List<TweetInfo>> tweets(String ids) {
		final String uri = baseURI + "/tweets?ids=" + ids + TWEET_FIELDS + MEDIA_FIELDS;
		Messager<JSONObject> resp = doGet(URI.create(uri));
		if (resp.isOK()) {
			List<TweetInfo> tweetInfo = resp.data().getList("data", TweetInfo.class);
			return resp.castDataType(tweetInfo);
		}
		return null;
	}

	public Messager<TweetInfo> tweet(String id) {
		final String uri = baseURI + "/tweets/" + id + TWEET_FIELDS + MEDIA_FIELDS;
		Messager<JSONObject> resp = doGet(URI.create(uri));
		if (resp.isOK()) {
			JSONObject data = resp.data();
			TweetInfo tweetInfo = data.getObject("data", TweetInfo.class);
			return resp.castDataType(tweetInfo);
		}
		return null;
	}

	public Messager<List<TweetUserInfo>> users(String ids) {
		return getUsers(baseURI + "/users?ids=" + ids + USER_FIELDS);
	}

	public Messager<TweetUserInfo> user(String id) {
		return getUser(baseURI + "/users/" + id + USER_FIELDS);
	}

	public Messager<List<TweetUserInfo>> usersByUsernames(String usernames) {
		return getUsers(baseURI + "/users/by?usernames=" + usernames + USER_FIELDS);
	}

	public Messager<TweetUserInfo> usersByUsername(String username) {
		return getUser(baseURI + "/users/by/username" + username + USER_FIELDS);
	}

	private @Nullable Messager<List<TweetUserInfo>> getUsers(String uri) {
		Messager<JSONObject> resp = doGet(URI.create(uri));
		if (resp.isOK()) {
			JSONObject data = resp.data();
			List<TweetUserInfo> tweetInfo = data.getList("data", TweetUserInfo.class);
			return resp.castDataType(tweetInfo);
		}
		return null;
	}

	private @Nullable Messager<TweetUserInfo> getUser(String uri) {
		Messager<JSONObject> resp = doGet(URI.create(uri));
		if (resp.isOK()) {
			JSONObject data = resp.data();
			TweetUserInfo tweetInfo = data.getObject("data", TweetUserInfo.class);
			return resp.castDataType(tweetInfo);
		}
		return null;
	}

	/**
	 * 基于代理配置信息创建HTTP客户端
	 */
	protected static HttpClient.Builder prepareProxyClient(String host, int port, @Nullable String username, @Nullable String password) {
		final HttpClient.Builder builder = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
				.proxy(ProxySelector.of(new InetSocketAddress(host, port)));

		if (StringUtil.notEmpty(username)) {
			final PasswordAuthentication authentication = new PasswordAuthentication(username, password.toCharArray());
			builder.authenticator(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {

					if (getRequestorType() == RequestorType.PROXY) {
						return authentication;
					}
					return null;
				}
			});
		}

		return builder;
	}

	/**
	 * 基于代理配置信息创建HTTP客户端
	 *
	 * @param proxyConfig 形如 <code> "proxy://username:password@host:port" </code>
	 */
	protected static HttpClient.Builder prepareProxyClient(@Nonnull String proxyConfig) {
		// "proxy://username:password@host:port"
		final URI uri = URI.create(proxyConfig);
		String username = null, password = null;
		final String userInfo = uri.getUserInfo();
		if (StringUtil.notEmpty(userInfo)) {
			final String[] parts = userInfo.split(":", 2);
			username = parts[0];
			password = parts[1];
		}
		return prepareProxyClient(uri.getHost(), uri.getPort(), username, password);
	}

	/**
	 * 基于代理配置信息创建HTTP客户端，如果没有配置代理，则使用默认的HTTP客户端
	 *
	 * @param proxyConfig 形如 <code> "proxy://username:password@host:port" </code>
	 */
	protected static HttpClient getProxyOrDefaultClient(@Nullable String proxyConfig) {
		return StringUtil.isBlank(proxyConfig) ? defaultClient() : prepareProxyClient(proxyConfig).build();
	}

}
