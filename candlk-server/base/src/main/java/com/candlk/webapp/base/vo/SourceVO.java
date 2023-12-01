package com.candlk.webapp.base.vo;

import me.codeplayer.util.X;

public abstract class SourceVO<T> extends AbstractVO<T> {

	private Object source;

	protected T getSource() {
		return X.castType(source);
	}

	public void setSource(Object source) {
		this.source = source;
	}

}
