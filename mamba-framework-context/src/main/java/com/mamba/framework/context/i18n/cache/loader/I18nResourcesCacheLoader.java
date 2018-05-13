package com.mamba.framework.context.i18n.cache.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.support.SpringFactoriesLoader;

import com.mamba.framework.context.cache.loader.AbstractCacheLoader;
import com.mamba.framework.context.i18n.cache.bean.I18nResource;
import com.mamba.framework.context.i18n.provider.I18nResourceProvider;

public class I18nResourcesCacheLoader extends AbstractCacheLoader<String, I18nResource> implements ApplicationContextAware, BeanClassLoaderAware {

	private ApplicationContext context;

	private ClassLoader classLoader;

	@Override
	public Map<String, I18nResource> data() {
		List<String> providers = SpringFactoriesLoader.loadFactoryNames(I18nResourceProvider.class, this.classLoader);
		Map<String, I18nResource> map = new HashMap<String, I18nResource>();
		for (String providerClassName : providers) {
			I18nResourceProvider provider = this.context.getBean(providerClassName, I18nResourceProvider.class);
			List<I18nResource> datas = provider.datas();
			for (int i = 0; null != datas && i < datas.size(); i++) {
				I18nResource i18nResource = datas.get(i);
				String key = i18nResource.getCode() + "_" + i18nResource.getLocal();
				map.put(key, i18nResource);
			}
		}
		return map;
	}

	@Override
	public Class<String> keyType() {
		return String.class;
	}

	@Override
	public Class<I18nResource> valueType() {
		return I18nResource.class;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
