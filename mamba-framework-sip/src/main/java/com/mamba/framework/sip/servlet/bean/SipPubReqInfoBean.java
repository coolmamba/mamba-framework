package com.mamba.framework.sip.servlet.bean;

import java.io.Serializable;

public class SipPubReqInfoBean implements Serializable {
	private long operatorId;
	private int accessChannel;
	private String accessChannelName;

	public long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(long operatorId) {
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
