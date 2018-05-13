package com.mamba.framework.context.cache.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.support.SpringFactoriesLoader;

import com.mamba.framework.context.cache.event.CacheLoadedApplicationEvent;
import com.mamba.framework.context.cache.loader.CacheLoader;
import com.mamba.framework.context.util.Assert;

public class CacheLoadApplicationRunner implements ApplicationRunner, ApplicationContextAware, BeanClassLoaderAware {
	private static final Log logger = LogFactory.getLog(CacheLoadApplicationRunner.class);

	private ApplicationContext context;
	
	private ClassLoader classLoader;

	@Autowired
	private CacheManager cacheManager;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void run(ApplicationArguments args) throws Exception {
		/** @step 1: 获取/META-INF/spring.factories文件中配置的缓存加载器 */
		List<String> cacheLoaderClassNames = SpringFactoriesLoader.loadFactoryNames(CacheLoader.class, this.classLoader);
		if (Assert.isEmpty(cacheLoaderClassNames)) {
			logger.warn("在/META-INF/spring.factories配置文件中未找到缓存加载器");
			return;
		}
		
		/** @step 2: 实例化缓存加载器 */
		List<CacheLoader<?, ?>> cacheLoaders = new ArrayList<CacheLoader<?, ?>>();
		for (String cacheLoaderClassName : cacheLoaderClassNames) {
			CacheLoader<?, ?> cacheLoader = this.context.getBean(cacheLoaderClassName, CacheLoader.class);
			if (cacheLoader instanceof ApplicationContextAware) {
				((ApplicationContextAware) cacheLoader).setApplicationContext(this.context);
			}
			if (cacheLoader instanceof BeanClassLoaderAware) {
				((BeanClassLoaderAware) cacheLoader).setBeanClassLoader(this.classLoader);
			}
			cacheLoaders.add(cacheLoader);
		}
		// 排序
		AnnotationAwareOrderComparator.sort(cacheLoaders);
		
		/** @step 3: 触发加载器执行，将缓存加载到缓存管理器 */
		for(CacheLoader<?, ?> cacheLoader : cacheLoaders) {
			String cacheName = cacheLoader.cacheName();
			Cache<?, ?> cache = cacheManager.createCache(cacheName, cacheLoader.configuration());
			Map data = cacheLoader.data();
			cache.putAll(data);
		}
		
		// 发布
		this.context.publishEvent(new CacheLoadedApplicationEvent("Cache Loaded"));
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
