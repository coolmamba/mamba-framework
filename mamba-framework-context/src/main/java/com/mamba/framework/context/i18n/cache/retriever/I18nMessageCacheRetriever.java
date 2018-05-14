package com.mamba.framework.context.i18n.cache.retriever;

import org.springframework.beans.factory.annotation.Autowired;

import com.mamba.framework.context.i18n.autoconfigure.I18nLocalEnum;
import com.mamba.framework.context.i18n.messagesource.I18nMessageSource;

public class I18nMessageCacheRetriever {
	@Autowired
	private I18nMessageSource i18nMessageSource;

	public String getMessage(String code) {
		return getMessage(code, null, null, I18nLocalEnum.ZH_CN);
	}

	public String getMessage(String code, Object[] args) {
		return getMessage(code, args, null, I18nLocalEnum.ZH_CN);
	}

	public String getMessage(String code, Object[] args, String defaultMessage) {
		return this.getMessage(code, args, defaultMessage, I18nLocalEnum.ZH_CN);
	}

	public String getMessage(String code, Object[] args, I18nLocalEnum local) {
		return getMessage(code, args, null, local);
	}

	public String getMessage(String code, Object[] args, String defaultMessage, I18nLocalEnum local) {
		return this.i18nMessageSource.getMessage(code, args, defaultMessage, local);
	}
}
