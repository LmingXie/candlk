package com.bojiu.webapp.base.entity;

import com.bojiu.common.model.*;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.X;
import org.jspecify.annotations.Nullable;

@Setter
@Getter
public abstract class BizEntity extends TimeBasedEntity implements StateBean {

	//
	/** 业务状态 */
	protected Integer status;
	/** 可见状态：3=全部可见；2=商家可见；1=平台可见；0=全部不可见 */
	protected State state;

	public void init(@Nullable Integer status, @Nullable State state) {
		this.status = X.expectNotNull(status, this.status, Status.YES.value);
		this.state = X.expectNotNull(state, this.state, State.PUBLIC);
	}

	public void setStatusAndState(Integer status, State state) {
		this.status = status;
		this.state = state;
	}

	public void initActiveStatus() {
		status = Status.YES.value;
		state = State.PUBLIC;
	}

	public static final String STATUS = "status";
	public static final String STATE = "state";

}