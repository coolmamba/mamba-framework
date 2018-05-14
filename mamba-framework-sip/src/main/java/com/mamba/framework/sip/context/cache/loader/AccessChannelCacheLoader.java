package com.mamba.framework.sip.context.cache.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mamba.framework.context.cache.loader.AbstractCacheLoader;
import com.mamba.framework.context.cache.provider.CacheProvider;
import com.mamba.framework.sip.context.cache.bean.AccessChannel;
import com.mamba.framework.sip.context.cache.provider.AccessChannelCacheProvider;

public class AccessChannelCacheLoader extends AbstractCacheLoader<String, AccessChannel> {
	@Override
	public Map<String, AccessChannel> data() {
		Map<String, AccessChannel> datas = new HashMap<String, AccessChannel>();
		List<CacheProvider<?>> providers = getProviders(AccessChannelCacheProvider.class);
		for (int i = 0; null != providers && i < providers.size(); i++) {
			CacheProvider<?> provider = providers.get(i);
			if (null != provider && provider instanceof AccessChannelCacheProvider) {
				List<AccessChannel> channels = ((AccessChannelCacheProvider) provider).provide();
				for (int j = 0; null != channels && j < channels.size(); j++) {
					AccessChannel channel = channels.get(j);
					String key = String.valueOf(channel.getAccessChannel());
					datas.put(key, channel);
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
	public Class<AccessChannel> valueType() {
		return AccessChannel.class;
	}
}
