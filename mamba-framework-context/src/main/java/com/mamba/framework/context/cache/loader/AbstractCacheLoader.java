package com.mamba.framework.context.cache.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.cache.configuration.MutableConfiguration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.mamba.framework.context.cache.provider.CacheProvider;

public abstract class AbstractCacheLoader<K, V> implements CacheLoader<K, V>, ApplicationContextAware {

	private ApplicationContext context;

	@Override
	public String cacheName() {
		return getClass().getName();
	}

	@Override
	public final MutableConfiguration<K, V> configuration() {
		MutableConfiguration<K, V> configuration = new MutableConfiguration<K, V>();
		configuration.setTypes(keyType(), valueType());
		return configuration;
	}

	public abstract Class<K> keyType();

	public abstract Class<V> valueType();

	protected final <P extends CacheProvider<?>> List<CacheProvider<?>> getProviders(Class<P> cacheProviderClass) {
		Map<String, P> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.context, cacheProviderClass, true, false);
		List<CacheProvider<?>> cacheProviders = new ArrayList<>(matchingBeans.values());
		List<CacheProvider<?>> providers = new ArrayList<CacheProvider<?>>();
		for (int i = 0; null != cacheProviders && i < cacheProviders.size(); i++) {
			CacheProvider<?> provider = cacheProviders.get(i);
			if (cacheProviderClass.isAssignableFrom(provider.getClass())) {
				providers.add(provider);
			}
		}
		return providers;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}
}
