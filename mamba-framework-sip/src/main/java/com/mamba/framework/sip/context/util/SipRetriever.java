package com.mamba.framework.sip.context.util;

import javax.cache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jf.crm.common.framework.cache.util.CacheRetriever;
import com.jf.crm.common.framework.sip.cache.loader.SipBusiAccessCacheLoader;
import com.jf.crm.common.framework.sip.cache.loader.SipExceptionCodeCacheLoader;
import com.jf.crm.common.framework.sip.model.SipBusiAccess;
import com.jf.crm.common.framework.sip.model.SipExceptionCode;

@Component
public class SipRetriever {
	private Log logger = LogFactory.getLog(SipRetriever.class);
	
	private String sipBusiAccessCacheLoaderClassName = SipBusiAccessCacheLoader.class.getName();
	private String sipExceptionCodeCacheLoaderClassName = SipExceptionCodeCacheLoader.class.getName();
	
	@Autowired
	private CacheRetriever cacheRetriever;
	
	public SipBusiAccess getSipBusiAccess(String busiCode, int channelType) {
		String key = busiCode + "_" + channelType;
		SipBusiAccess sipBusiAccess = null;
		try {
			sipBusiAccess = this.cacheRetriever.get(key, sipBusiAccessCacheLoaderClassName, String.class, SipBusiAccess.class);
		} catch (Exception e) {
			logger.error("获取Sip接入配置缓存失败，key = {" + key + "}");
		}
		return sipBusiAccess;
	}
	
	public Cache<String, SipBusiAccess> getSipBusiAccess() {
		return this.cacheRetriever.getCache(sipBusiAccessCacheLoaderClassName, String.class, SipBusiAccess.class);
	}
	
	public SipExceptionCode getSipExceptionCode(String exceptionKey, int channelType) {
		String key = exceptionKey + "_" + channelType;
		SipExceptionCode sipExceptionCode = null;
		try {
			sipExceptionCode = this.cacheRetriever.get(key, sipExceptionCodeCacheLoaderClassName, String.class, SipExceptionCode.class);
		} catch (Exception e) {
			logger.error("获取Sip异常码缓存失败，key = {" + key + "}");
		}
		return sipExceptionCode;
	}
}
