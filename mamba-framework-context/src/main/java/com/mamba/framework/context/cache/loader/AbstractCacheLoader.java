package com.mamba.framework.context.cache.loader;

import java.util.ArrayList;
import java.util.List;

import javax.cache.configuration.MutableConfiguration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.support.SpringFactoriesLoader;

import com.mamba.framework.context.cache.provider.CacheProvider;

public abstract class AbstractCacheLoader<K, V> implements CacheLoader<K, V>, ApplicationContextAware, BeanClassLoaderAware {

	private ApplicationContext context;

	private ClassLoader classLoader;

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

	protected final <P extends CacheProvider<?>> List<String> getProviderNames(Class<P> cacheProviderClass) {
		return SpringFactoriesLoader.loadFactoryNames(cacheProviderClass, this.classLoader);
	}

	protected final <P extends CacheProvider<?>> List<P> getProviders(Class<P> cacheProviderClass) {
		List<String> providerNames = getProviderNames(cacheProviderClass);
		if (null == providerNames || providerNames.size() == 0) {
			return null;
		}
		List<P> providers = new ArrayList<P>();
		for (String providerName : providerNames) {
			P provider = this.context.getBean(providerName, cacheProviderClass);
			providers.add(provider);
		}
		return providers;
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
