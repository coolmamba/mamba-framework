package com.mamba.framework.context.i18n.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "i18n")
public class I18nProperties {
	private I18nLocalEnum local = I18nLocalEnum.ZH_CN;

	public I18nLocalEnum getLocal() {
		return local;
	}

	public void setLocal(I18nLocalEnum local) {
		this.local = local;
	}

}
