package com.mamba.framework.context.i18n.messagesource;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import com.mamba.framework.context.cache.retriever.CacheRetriever;
import com.mamba.framework.context.i18n.autoconfigure.I18nLocalEnum;
import com.mamba.framework.context.i18n.autoconfigure.I18nProperties;
import com.mamba.framework.context.i18n.cache.bean.I18nResource;
import com.mamba.framework.context.i18n.cache.loader.I18nResourcesCacheLoader;
import com.mamba.framework.context.util.Assert;
import com.mamba.framework.context.util.StringUtils;

/**
 * i18n_resources消息源
 * @author junmamba
 */
public class I18nMessageSource implements MessageSource {
	private static final Log logger = LogFactory.getLog(I18nMessageSource.class);

	private CacheRetriever cacheRetriever;
	private I18nProperties i18nProperties;
	private String cacheName = I18nResourcesCacheLoader.class.getName();

	public I18nMessageSource(CacheRetriever cacheRetriever, I18nProperties i18nProperties) {
		this.cacheRetriever = cacheRetriever;
		this.i18nProperties = i18nProperties;
	}

	public String getMessage(String code) {
		return getMessage(code, null, null, I18nLocalEnum.ZH_CN);
	}

	public String getMessage(String code, Object[] args) {
		return getMessage(code, args, null, I18nLocalEnum.ZH_CN);
	}

	public String getMessage(String code, Object[] args, String defaultMessage) {
		return getMessage(code, args, defaultMessage, I18nLocalEnum.ZH_CN);
	}

	public String getMessage(String code, Object[] args, String defaultMessage, I18nLocalEnum local) {
		if (local == null) {
			local = this.i18nProperties.getLocal();
		}
		if (null == local) {
			local = I18nLocalEnum.ZH_CN;
		}
		String key = code + "_" + local;
		String content = StringUtils.EMPTY;
		try {
			content = this.cacheRetriever.get(key, cacheName, String.class, I18nResource.class).getContent();
		} catch (Exception e) {
			logger.error("从缓存中无法加载到key={" + key + "}, local={" + local + "}对应的国际码信息");
		}
		if (StringUtils.isBlank(content)) {
			return content;
		}
		if (Assert.isNotEmpty(args)) {
			for (int i = 0; i < args.length; ++i) {
				if (args[i] != null) {
					content = StringUtils.replaceOnce(content, "{" + i + "}", args[i].toString());
				} else {
					content = StringUtils.replaceOnce(content, "{" + i + "}", "<null>");
				}
			}
		}
		return content;
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return getMessage(code, args, defaultMessage);
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return getMessage(code, args);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return null;
	}
}
