package com.mamba.framework.context.date.provider;

import java.util.Date;

public class DefaultSystemDateProvider implements SystemDateProvider {
	@Override
	public Date now() {
		return new Date();
	}
}
