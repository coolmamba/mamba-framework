package com.mamba.framework.context.cache.event;

import org.springframework.context.ApplicationEvent;

public class CacheLoadedApplicationEvent extends ApplicationEvent {
	public CacheLoadedApplicationEvent(Object source) {
		super(source);
	}
}
