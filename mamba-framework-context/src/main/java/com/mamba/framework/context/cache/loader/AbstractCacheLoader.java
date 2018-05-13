package com.mamba.framework.context.cache.loader;

import javax.cache.configuration.MutableConfiguration;

public abstract class AbstractCacheLoader<K, V> implements CacheLoader<K, V> {
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
}
