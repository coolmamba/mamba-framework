package com.mamba.framework.sip.servlet.bean;

import java.io.Serializable;
import java.util.Map;

public class SipBusiReqBodyBean implements Serializable {
	private String busiCode;
	private Map<String, Object> busiParams;

	public String getBusiCode() {
		return busiCode;
	}

	public void setBusiCode(String busiCode) {
		this.busiCode = busiCode;
	}

	public Map<String, Object> getBusiParams() {
		return busiParams;
	}

	public void setBusiParams(Map<String, Object> busiParams) {
		this.busiParams = busiParams;
	}
}
