package com.mamba.framework.sip.servlet.bean;

import java.io.Serializable;
import java.util.Map;

public class SipRespBean implements Serializable {
	private Map<String, Object> responseMap;
	private SipReqBean sipReqBean;

	public SipRespBean() {
	}

	public SipRespBean(Map<String, Object> responseMap, SipReqBean sipReqBean) {
		super();
		this.responseMap = responseMap;
		this.sipReqBean = sipReqBean;
	}

	public Map<String, Object> getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(Map<String, Object> responseMap) {
		this.responseMap = responseMap;
	}

	public SipReqBean getSipReqBean() {
		return sipReqBean;
	}

	public void setSipReqBean(SipReqBean sipReqBean) {
		this.sipReqBean = sipReqBean;
	}
}
