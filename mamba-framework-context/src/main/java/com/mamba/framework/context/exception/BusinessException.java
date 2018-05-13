package com.mamba.framework.context.exception;

public class BusinessException extends RuntimeException {
	private String key;
	private Object[] args;

	public BusinessException(String key) {
		this(key, null);
	}

	public BusinessException(String key, Object arg) {
		this.key = key;
		this.args = new Object[] { arg };
	}

	public BusinessException(String key, Object[] args) {
		this.key = key;
		this.args = args;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
}
