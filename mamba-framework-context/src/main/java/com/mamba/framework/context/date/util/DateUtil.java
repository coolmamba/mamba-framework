package com.mamba.framework.context.date.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.mamba.framework.context.date.provider.SystemDateProvider;

public class DateUtil {
	@Autowired
	private SystemDateProvider systemDateProvider;

	public Date now() {
		if (null != this.systemDateProvider) {
			return this.systemDateProvider.now();
		} else {
			return new Date();
		}
	}
}
