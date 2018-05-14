package com.mamba.framework.context.i18n.cache.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mamba.framework.context.cache.loader.AbstractCacheLoader;
import com.mamba.framework.context.cache.provider.CacheProvider;
import com.mamba.framework.context.i18n.cache.bean.I18nResource;
import com.mamba.framework.context.i18n.cache.provider.I18nResourceCacheProvider;

public class I18nResourcesCacheLoader extends AbstractCacheLoader<String, I18nResource> {

	@Override
	public Map<String, I18nResource> data() {
		List<CacheProvider<?>> providers = getProviders(I18nResourceCacheProvider.class);
		Map<String, I18nResource> map = new HashMap<String, I18nResource>();
		for (CacheProvider<?> provider : providers) {
			if (null != provider && provider instanceof I18nResourceCacheProvider) {
				List<I18nResource> datas = ((I18nResourceCacheProvider) provider).provide();
				for (int i = 0; null != datas && i < datas.size(); i++) {
					I18nResource i18nResource = datas.get(i);
					String key = i18nResource.getCode() + "_" + i18nResource.getLocal();
					map.put(key, i18nResource);
				}
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
}
