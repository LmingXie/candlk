package com.bojiu.webapp.base.vo;

import lombok.Setter;
import me.codeplayer.util.X;

public abstract class SourceVO<T> extends AbstractVO<T> {

	@Setter
	private Object source;

	protected T getSource() {
		return X.castType(source);
	}

}