package com.mamba.framework.context.session.core;

import java.io.Serializable;

public class Session implements Serializable {
	private int accessChannel;

	private String accessChannelName;

	private Operator operator;

	public int getAccessChannel() {
		return accessChannel;
	}

	public void setAccessChannel(int accessChannel) {
		this.accessChannel = accessChannel;
	}

	public String getAccessChannelName() {
		return accessChannelName;
	}

	public void setAccessChannelName(String accessChannelName) {
		this.accessChannelName = accessChannelName;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

}
