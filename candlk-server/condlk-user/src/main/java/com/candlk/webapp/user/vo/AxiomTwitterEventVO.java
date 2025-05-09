package com.candlk.webapp.user.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AxiomTwitterEventVO {

	public String room;
	public Content content;

	@NoArgsConstructor
	@Data
	public static class Content {

		public String taskId;
		public String eventId;
		public String handle;
		public String eventType;
		public String subscriptionType;
		public String event;
		public String createdAt;

	}

}
