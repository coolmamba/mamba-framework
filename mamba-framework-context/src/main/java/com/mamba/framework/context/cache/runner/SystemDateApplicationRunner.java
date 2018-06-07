package com.mamba.framework.context.cache.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import com.mamba.framework.context.date.util.DateUtil;
import com.mamba.framework.context.session.SessionManager;

public class SystemDateApplicationRunner implements ApplicationRunner {
	@Autowired
	private DateUtil dateUtil;

	@Override
	public void run(ApplicationArguments arguments) throws Exception {
		long dbTimestamp = dateUtil.now().getTime();// 数据库当前时间戳
		long hostTimestamp = System.currentTimeMillis();// 主机当前时间戳
		long hostWithDbTimestampDifferenceValue = hostTimestamp - dbTimestamp;
		SessionManager.setHostWithDbTimestampDifferenceValue(hostWithDbTimestampDifferenceValue);
	}
}
