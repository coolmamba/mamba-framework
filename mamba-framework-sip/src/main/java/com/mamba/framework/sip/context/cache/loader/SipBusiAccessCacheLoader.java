package com.mamba.framework.sip.context.cache.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.jf.crm.common.framework.cache.loader.AbstractCacheLoader;
import com.jf.crm.common.framework.sip.mapper.SipBusiAccessMapper;
import com.jf.crm.common.framework.sip.model.SipBusiAccess;

public class SipBusiAccessCacheLoader extends AbstractCacheLoader<String, SipBusiAccess> {
	@Autowired
	private SipBusiAccessMapper sipBusiAccessMapper;
	
	@Override
	public Map<String, SipBusiAccess> data() {
		List<SipBusiAccess> sipBusiAccesses = this.sipBusiAccessMapper.selectAllValidStateDatas();
		Map<String, SipBusiAccess> datas = new HashMap<String, SipBusiAccess>();
		for (int i = 0; null != sipBusiAccesses && i < sipBusiAccesses.size(); i++) {
			SipBusiAccess sipBusiAccess = sipBusiAccesses.get(i);
			String key = sipBusiAccess.getBusiCode() + "_" + sipBusiAccess.getAccessChannelType();
			datas.put(key, sipBusiAccess);
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
