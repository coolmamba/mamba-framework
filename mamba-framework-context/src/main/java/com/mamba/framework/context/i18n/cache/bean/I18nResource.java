package com.mamba.framework.context.i18n.cache.bean;

import java.io.Serializable;

public class I18nResource implements Serializable {
	private String code;
	private String local;
	private String content;

	public I18nResource() {
	}

	public I18nResource(String code, String local, String content) {
		this.code = code;
		this.local = local;
		this.content = content;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "I18nResource [code=" + code + ", local=" + local + ", content=" + content + "]";
	}
}
