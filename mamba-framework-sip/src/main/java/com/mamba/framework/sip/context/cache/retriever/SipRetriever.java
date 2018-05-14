package com.mamba.framework.sip.context.cache.retriever;

import javax.cache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mamba.framework.context.cache.retriever.CacheRetriever;
import com.mamba.framework.sip.context.cache.bean.AccessChannel;
import com.mamba.framework.sip.context.cache.bean.SipBusiAccess;
import com.mamba.framework.sip.context.cache.bean.SipExceptionCode;
import com.mamba.framework.sip.context.cache.loader.AccessChannelCacheLoader;
import com.mamba.framework.sip.context.cache.loader.SipBusiAccessCacheLoader;
import com.mamba.framework.sip.context.cache.loader.SipExceptionCodeCacheLoader;

public class SipRetriever {
	private Log logger = LogFactory.getLog(SipRetriever.class);
	
	private String sipBusiAccessCacheLoaderClassName = SipBusiAccessCacheLoader.class.getName();
	private String sipExceptionCodeCacheLoaderClassName = SipExceptionCodeCacheLoader.class.getName();
	private String accessChannelCacheLoaderClassName = AccessChannelCacheLoader.class.getName();

	
	@Autowired
	private CacheRetriever cacheRetriever;
	
	public SipBusiAccess getSipBusiAccess(String busiCode, int accessChannel) {
		String key = busiCode + "_" + accessChannel;
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
	
	public SipExceptionCode getSipExceptionCode(String exceptionKey, int accessChannel) {
		String key = exceptionKey + "_" + accessChannel;
		SipExceptionCode sipExceptionCode = null;
		try {
			sipExceptionCode = this.cacheRetriever.get(key, sipExceptionCodeCacheLoaderClassName, String.class, SipExceptionCode.class);
		} catch (Exception e) {
			logger.error("获取Sip异常码缓存失败，key = {" + key + "}");
		}
		return sipExceptionCode;
	}
	
	public AccessChannel getAccessChannel(int accessChannel) {
		AccessChannel accessChannelVale = null;
		String key = String.valueOf(accessChannel);
		try {
			accessChannelVale = this.cacheRetriever.get(key, accessChannelCacheLoaderClassName, String.class, AccessChannel.class);
		} catch (Exception e) {
			logger.error("获取Sip异常码缓存失败，key = {" + key + "}");
		}
		return accessChannelVale;
	}
}
