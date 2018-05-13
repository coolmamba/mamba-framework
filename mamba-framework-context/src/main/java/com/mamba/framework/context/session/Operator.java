package com.mamba.framework.context.session;

import java.io.Serializable;

public class Operator implements Serializable {
	private long operatorId;

	public long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(long operatorId) {
		this.operatorId = operatorId;
	}
}
