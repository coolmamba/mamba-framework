package com.mamba.framework.context.date.provider;

public class MysqlSystemDateProvider extends DatabaseSystemDateProvider {
	@Override
	protected String getNowDateSQL() {
		return "SELECT NOW() AS NOW FROM DUAL";
	}
}
