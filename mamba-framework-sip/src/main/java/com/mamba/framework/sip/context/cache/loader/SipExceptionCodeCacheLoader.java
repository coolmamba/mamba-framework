package com.mamba.framework.sip.context.cache.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.jf.crm.common.framework.sip.mapper.SipExceptionCodeMapper;
import com.jf.crm.common.framework.sip.model.SipExceptionCode;
import com.mamba.framework.context.cache.loader.AbstractCacheLoader;

public class SipExceptionCodeCacheLoader extends AbstractCacheLoader<String, SipExceptionCode> {
	@Autowired
	private SipExceptionCodeMapper sipExceptionCodeMapper;
	
	@Override
	public Map<String, SipExceptionCode> data() {
		List<SipExceptionCode> sipExceptionCodes = this.sipExceptionCodeMapper.selectAllValidStateDatas();
		Map<String, SipExceptionCode> datas = new HashMap<String, SipExceptionCode>();
		for (int i = 0; null != sipExceptionCodes && i < sipExceptionCodes.size(); i++) {
			SipExceptionCode sipExceptionCode = sipExceptionCodes.get(i);
			String key = sipExceptionCode.getExceptionKey() + "_" + sipExceptionCode.getAccessChannelType();
			datas.put(key, sipExceptionCode);
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
