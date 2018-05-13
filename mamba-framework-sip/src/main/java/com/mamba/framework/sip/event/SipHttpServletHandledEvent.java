package com.mamba.framework.sip.event;

import org.springframework.context.ApplicationEvent;

public class SipHttpServletHandledEvent extends ApplicationEvent {
	private Throwable failureCause;
	private long processingTime;
	private String requestMethod;

	public SipHttpServletHandledEvent(Object source, String requestMethod, Throwable failureCause,
			long processingTime) {
		super(source);
		this.requestMethod = requestMethod;
		this.failureCause = failureCause;
		this.processingTime = processingTime;
	}
}
