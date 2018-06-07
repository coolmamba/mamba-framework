package com.mamba.framework.context.date.autoconfigure;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mamba.framework.context.FrameworkComponentOrdered;
import com.mamba.framework.context.date.provider.DefaultSystemDateProvider;
import com.mamba.framework.context.date.provider.MysqlSystemDateProvider;
import com.mamba.framework.context.date.provider.SystemDateProvider;
import com.mamba.framework.context.date.runner.SystemDateApplicationRunner;

@AutoConfigureOrder(FrameworkComponentOrdered.DATE)
@AutoConfigureAfter(value = { DataSourceAutoConfiguration.class, DataSource.class })
@Configuration
public class DateAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean(SystemDateProvider.class)
	@ConditionalOnClass(name = { "com.mysql.jdbc.MySQLConnection" })
	public MysqlSystemDateProvider mysqlSystemDateProvider() {
		return new MysqlSystemDateProvider();
	}

	@Bean
	@ConditionalOnMissingBean(SystemDateProvider.class)
	public DefaultSystemDateProvider defaultSystemDateProvider() {
		return new DefaultSystemDateProvider();
	}

	@Bean
	public SystemDateApplicationRunner systemDateApplicationRunner() {
		return new SystemDateApplicationRunner();
	}
}
