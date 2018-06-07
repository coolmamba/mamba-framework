package com.mamba.framework.context.date.runner;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import com.mamba.framework.context.date.provider.SystemDateProvider;
import com.mamba.framework.context.session.SessionManager;

public class SystemDateApplicationRunner implements ApplicationRunner {
	@Autowired
	private SystemDateProvider systemDateProvider;

	public Date now() {
		if (null != this.systemDateProvider) {
			return this.systemDateProvider.now();
		} else {
			return new Date();
		}
	}

	@Override
	public void run(ApplicationArguments arguments) throws Exception {
		long dbTimestamp = now().getTime();// 数据库当前时间戳
		long hostTimestamp = System.currentTimeMillis();// 主机当前时间戳
		long hostWithDbTimestampDifferenceValue = hostTimestamp - dbTimestamp;
		SessionManager.setHostWithDbTimestampDifferenceValue(hostWithDbTimestampDifferenceValue);
	}
}
