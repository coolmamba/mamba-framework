package com.mamba.framework.context.beans.beantranslator.metadata;

public class FieldTranslatorMetadata {
	private Object srcBean;
	private String srcFieldName;
	private Object srcFieldVlaue;

	public Object getSrcBean() {
		return srcBean;
	}

	public void setSrcBean(Object srcBean) {
		this.srcBean = srcBean;
	}

	public String getSrcFieldName() {
		return srcFieldName;
	}

	public void setSrcFieldName(String srcFieldName) {
		this.srcFieldName = srcFieldName;
	}

	public Object getSrcFieldVlaue() {
		return srcFieldVlaue;
	}

	public void setSrcFieldVlaue(Object srcFieldVlaue) {
		this.srcFieldVlaue = srcFieldVlaue;
	}
}
