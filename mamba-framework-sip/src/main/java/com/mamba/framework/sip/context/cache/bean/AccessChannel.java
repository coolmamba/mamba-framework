package com.mamba.framework.sip.context.cache.bean;

import java.io.Serializable;

public class AccessChannel implements Serializable {
	private int accessChannel;
	private String accessChannelName;

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
