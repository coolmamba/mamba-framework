package com.mamba.framework.sip.context.cache.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mamba.framework.context.cache.loader.AbstractCacheLoader;
import com.mamba.framework.sip.context.cache.bean.SipExceptionCode;
import com.mamba.framework.sip.context.cache.provider.SipExceptionCodeCacheProvider;

public class SipExceptionCodeCacheLoader extends AbstractCacheLoader<String, SipExceptionCode> {
	@Override
	public Map<String, SipExceptionCode> data() {
		List<SipExceptionCodeCacheProvider> providers = getProviders(SipExceptionCodeCacheProvider.class);
		Map<String, SipExceptionCode> datas = new HashMap<String, SipExceptionCode>();
		for (int i = 0; null != providers && i < providers.size(); i++) {
			SipExceptionCodeCacheProvider sipBusiAccessProvider = providers.get(i);
			if (null != sipBusiAccessProvider) {
				List<SipExceptionCode> sipBusiAccesses = sipBusiAccessProvider.provide();
				for (int j = 0; null != sipBusiAccesses && j < sipBusiAccesses.size(); j++) {
					SipExceptionCode sipExceptionCode = sipBusiAccesses.get(j);
					String key = sipExceptionCode.getKey() + "_" + sipExceptionCode.getAccessChannel();
					datas.put(key, sipExceptionCode);
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
	public Class<SipExceptionCode> valueType() {
		return SipExceptionCode.class;
	}
}
