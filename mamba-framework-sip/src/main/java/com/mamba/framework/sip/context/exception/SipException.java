package com.mamba.framework.sip.context.exception;

import com.jf.crm.common.framework.exception.BusinessException;

public class SipException extends BusinessException {
	public SipException(String key) {
		super(key);
	}

	public SipException(String key, Object arg) {
		super(key, arg);
	}

	public SipException(String key, Object[] args) {
		super(key, args);
	}
}
