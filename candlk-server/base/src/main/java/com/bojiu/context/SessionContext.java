package com.bojiu.context;

import java.util.Objects;
import java.util.function.Consumer;

import com.bojiu.common.web.Client;
import com.bojiu.context.model.Language;
import lombok.Getter;
import lombok.Setter;

/**
 * 会话上下文（用户不存在在此）
 */
@Getter
@Setter
public class SessionContext {

	private Client client;
	private Language language;
	private String ip;

	public SessionContext() {
	}

	public SessionContext(Client client, Language language, String ip) {
		this.client = client;
		this.language = language;
		this.ip = ip;
	}

	protected transient Consumer<SessionContext> flushListener;

	public void setClient(Client client) {
		triggerFlush(this.client != client);
		this.client = client;
	}

	public void setLanguage(Language language) {
		triggerFlush(this.language, language);
		this.language = language;
	}

	public void setIp(String ip) {
		triggerFlush(this.ip, ip);
		this.ip = ip;
	}

	public void triggerFlush(boolean need) {
		if (need && flushListener != null) {
			flushListener.accept(this);
		}
	}

	public void triggerFlush(Object oldVal, Object newVal) {
		triggerFlush(!Objects.equals(oldVal, newVal));
	}

}
