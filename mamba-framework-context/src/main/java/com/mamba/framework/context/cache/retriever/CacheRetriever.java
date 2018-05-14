package com.mamba.framework.context.cache.retriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.mamba.framework.context.cache.loader.CacheLoader;
import com.mamba.framework.context.exception.BusinessException;
import com.mamba.framework.context.util.Assert;

public class CacheRetriever {
	private Log logger = LogFactory.getLog(CacheRetriever.class);

	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private ApplicationContext context;
	
	private static final String REFRESH_CACHE_NAME = "com.mamba.framework.context.cache.util.CacheRetriever$RefreshFlag";
	private static final String REFRESH_FLAG = "REFRESH_FLAG";
	private static final String REFRESHING_FLAG = "1";
	private static final String REFRESHED_FLAG = "0";


	public <K, V> V get(K key, String cacheName, Class<K> keyType, Class<V> valueType) {
		return this.cacheManager.getCache(cacheName, keyType, valueType).get(key);
	}

	public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
		return this.cacheManager.getCache(cacheName, keyType, valueType);
	}
	
	public <K, V> List<V> getAllValue(String cacheName, Class<K> keyType, Class<V> valueType) {
		Cache<K, V> cache = getCache(cacheName, keyType, valueType);
		Iterator<Cache.Entry<K, V>> ite = cache.iterator();
		List<V> result = new ArrayList<V>();
		while (ite.hasNext()) {
			Cache.Entry<K, V> entry = ite.next();
			result.add(entry.getValue());
		}
		return result;
	}
	
	public Map<String, List<String>> refresh(List<String> cacheItems) throws BusinessException {
		if (Assert.isEmpty(cacheItems)) {
			return null;
		}

		Map<String, List<String>> refreshResultMap = new HashMap<String, List<String>>();
		List<String> successList = new ArrayList<String>();
		List<String> failList = new ArrayList<String>();
		refreshResultMap.put("SUCCESS", successList);
		refreshResultMap.put("FAIL", failList);
		
		Cache<String, String> cache = null;
		try {
			cache = this.cacheManager.getCache(REFRESH_CACHE_NAME);
		} catch (Exception e) {
			logger.error("获取缓存刷新标志为失败");
		}
		if (null != cache && REFRESHING_FLAG.equals(cache.get(REFRESH_FLAG))) {// 正在刷新缓存
			// 缓存正在刷新，请稍后再试
			throw new BusinessException("FW000000");
		}
		try {
			if (null == cache) {
				MutableConfiguration<String, String> configuration = new MutableConfiguration<String, String>();
				cache = this.cacheManager.createCache(REFRESH_CACHE_NAME, configuration);
			}
			cache.put(REFRESH_FLAG, REFRESHING_FLAG);// 设置刷新标志
			
			for (String cacheName : cacheItems) {
				try {
					CacheLoader<?, ?> cacheLoader = this.context.getBean(cacheName, CacheLoader.class);
					Cache<?, ?> itemCache = getCache(cacheName, cacheLoader.configuration().getKeyType(), cacheLoader.configuration().getValueType());
					Map data = cacheLoader.data();
					itemCache.putAll(data);
					successList.add(cacheName);
				} catch (Exception e) {
					failList.add(cacheName);
				}
			}
		} catch (Exception e) {
			throw new BusinessException("FW000001", new String[] { e.getMessage() });
		} finally {
			if (null != cache) {
				cache.put(REFRESH_FLAG, REFRESHED_FLAG);// 刷新完毕标志
			}
		}
		return refreshResultMap;
	}
}
