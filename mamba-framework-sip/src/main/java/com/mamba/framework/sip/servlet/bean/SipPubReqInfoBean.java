package com.mamba.framework.sip.servlet.bean;

import java.io.Serializable;

public class SipPubReqInfoBean implements Serializable {
	private int operatorId;
	private int accessChannel;
	private String accessChannelName;

	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}

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
}
