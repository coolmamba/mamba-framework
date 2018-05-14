package com.mamba.framework.sip.context.cache.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mamba.framework.context.cache.loader.AbstractCacheLoader;
import com.mamba.framework.context.cache.provider.CacheProvider;
import com.mamba.framework.sip.context.cache.bean.SipBusiAccess;
import com.mamba.framework.sip.context.cache.provider.SipBusiAccessCacheProvider;

public class SipBusiAccessCacheLoader extends AbstractCacheLoader<String, SipBusiAccess> {
	@Override
	public Map<String, SipBusiAccess> data() {
		List<CacheProvider<?>> providers = getProviders(SipBusiAccessCacheProvider.class);
		Map<String, SipBusiAccess> datas = new HashMap<String, SipBusiAccess>();
		for (int i = 0; null != providers && i < providers.size(); i++) {
			CacheProvider<?> provider = providers.get(i);
			if (null != provider && provider instanceof SipBusiAccessCacheProvider) {
				List<SipBusiAccess> sipBusiAccesses = ((SipBusiAccessCacheProvider) provider).provide();
				for (int j = 0; null != sipBusiAccesses && j < sipBusiAccesses.size(); j++) {
					SipBusiAccess sipBusiAccess = sipBusiAccesses.get(j);
					if (null == sipBusiAccess.getAccessChannel()) {
						sipBusiAccess.setAccessChannel(-1);
					}
					String key = sipBusiAccess.getBusiCode() + "_" + sipBusiAccess.getAccessChannel();
					datas.put(key, sipBusiAccess);
				}
			}
		}
		return datas;
	}

	@Override
	public Class<String> keyType() {
		return String.class;
	}

	@Override
	public Class<SipBusiAccess> valueType() {
		return SipBusiAccess.class;
	}

}
