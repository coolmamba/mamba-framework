package com.mamba.framework.sip.servlet.bean;

import java.io.Serializable;

public class SipReqBean implements Serializable {
	private SipPubReqInfoBean pubReqInfo;
	private SipBusiReqBodyBean busiReqBody;

	public SipPubReqInfoBean getPubReqInfo() {
		return pubReqInfo;
	}

	public void setPubReqInfo(SipPubReqInfoBean pubReqInfo) {
		this.pubReqInfo = pubReqInfo;
	}

	public SipBusiReqBodyBean getBusiReqBody() {
		return busiReqBody;
	}

	public void setBusiReqBody(SipBusiReqBodyBean busiReqBody) {
		this.busiReqBody = busiReqBody;
	}
}
