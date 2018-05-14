package com.mamba.framework.context.cache.autoconfigure;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.cache.CacheManager;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import com.mamba.framework.context.FrameworkComponentOrdered;
import com.mamba.framework.context.cache.autoconfigure.CacheLoadAutoConfiguration.CacheLoaderRegistrar;
import com.mamba.framework.context.cache.loader.CacheLoader;
import com.mamba.framework.context.cache.provider.CacheProvider;
import com.mamba.framework.context.cache.runner.CacheLoadApplicationRunner;
import com.mamba.framework.context.cache.util.CacheRetriever;
import com.mamba.framework.context.util.Assert;
import com.mamba.framework.context.util.BeanDefinitionRegistryUtil;

@AutoConfigureOrder(FrameworkComponentOrdered.CACHE)
@Configuration
@ConditionalOnClass(CacheManager.class)
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, DataSourceAutoConfiguration.class, DataSource.class })
@Import(value = { CacheLoaderRegistrar.class })
public class CacheLoadAutoConfiguration {
	@Bean
	public CacheLoadApplicationRunner cacheLoadApplicationRunner() {
		return new CacheLoadApplicationRunner();
	}

	@Bean
	public CacheRetriever cacheRetriever() {
		return new CacheRetriever();
	}

	static class CacheLoaderRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
		private static final Log logger = LogFactory.getLog(CacheLoaderRegistrar.class);
		
		private ClassLoader classLoader;
		
		@SuppressWarnings("unchecked")
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			/** 注册缓存缓存加载类 */
			registerCacheLoaderBeanDefinition(registry, CacheLoader.class);
			/** 注册缓存缓存提供类 */
			registerCacheLoaderBeanDefinition(registry, CacheProvider.class);
		}
		
		private void registerCacheLoaderBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass) {
			Set<String> factoryNames = new LinkedHashSet<String>(SpringFactoriesLoader.loadFactoryNames(beanClass, this.classLoader));
			if (Assert.isEmpty(factoryNames)) {
				logger.warn("在/META-INF/spring.factories配置文件中未找到: " + beanClass.getName());
				return;
			}
			Iterator<String> ite = factoryNames.iterator();
			while (ite.hasNext()) {
				String factoryName = ite.next();
				if (registry.containsBeanDefinition(factoryName)) {
					continue;
				}
				Class<?> clazz = null;
				try {
					clazz = Class.forName(factoryName);
				} catch (ClassNotFoundException e) {
					logger.error("加载类：" + factoryName + "失败，原因：" + e.getMessage());
				}
				if (null == clazz) {
					continue;
				}
				BeanDefinitionRegistryUtil.registerInfrastructureBeanDefinition(registry, clazz);
			}
		}

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}
	}
}
