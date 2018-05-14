package com.mamba.framework.context.cache.provider;

import java.util.List;

public interface CacheProvider<T> {
	public List<T> provide();
}
