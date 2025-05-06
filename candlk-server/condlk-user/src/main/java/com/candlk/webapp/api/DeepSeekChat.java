package com.candlk.webapp.api;

import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;

@Setter
@Getter
public class DeepSeekChat {

	@JSONField(name = "id")
	public String id;
	@JSONField(name = "object")
	public String object;
	@JSONField(name = "created")
	public Integer created;
	@JSONField(name = "model")
	public String model;
	@JSONField(name = "choices")
	public List<Choices> choices;
	@JSONField(name = "usage")
	public Usage usage;
	@JSONField(name = "system_fingerprint")
	public String systemFingerprint;

	@Setter
	@Getter
	public static class Usage {

		@JSONField(name = "prompt_tokens")
		public Integer promptTokens;
		@JSONField(name = "completion_tokens")
		public Integer completionTokens;
		@JSONField(name = "total_tokens")
		public Integer totalTokens;
		@JSONField(name = "prompt_tokens_details")
		public Usage.PromptTokensDetails promptTokensDetails;
		@JSONField(name = "prompt_cache_hit_tokens")
		public Integer promptCacheHitTokens;
		@JSONField(name = "prompt_cache_miss_tokens")
		public Integer promptCacheMissTokens;

		@Setter
		@Getter
		public static class PromptTokensDetails {

			@JSONField(name = "cached_tokens")
			public Integer cachedTokens;

		}

	}

	@Setter
	@Getter
	public static class Choices {

		@JSONField(name = "index")
		public Integer index;
		@JSONField(name = "message")
		public Choices.Message message;
		@JSONField(name = "finish_reason")
		public String finishReason;

		@Setter
		@Getter
		public static class Message {

			@JSONField(name = "role")
			public String role;
			@JSONField(name = "content")
			public String content;

		}

	}

}
