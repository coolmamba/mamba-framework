package com.mamba.framework.context.cache.loader;

import java.util.Map;

import javax.cache.configuration.MutableConfiguration;

/**
 * 缓存加载器
 * @author junmamba
 *
 * @param <K>
 * @param <V>
 */
public interface CacheLoader<K, V> {
	public String cacheName();

	public MutableConfiguration<K, V> configuration();

	public Map<K, V> data();
}
