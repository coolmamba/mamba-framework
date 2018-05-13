package com.mamba.framework.context.cache.autoconfigure;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.cache.CacheManager;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
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

import com.mamba.framework.context.FrameComponentOrdered;
import com.mamba.framework.context.cache.autoconfigure.CacheLoadAutoConfiguration.CacheLoaderRegistrar;
import com.mamba.framework.context.cache.loader.CacheLoader;
import com.mamba.framework.context.cache.runner.CacheLoadApplicationRunner;
import com.mamba.framework.context.cache.util.CacheRetriever;
import com.mamba.framework.context.util.Assert;

@AutoConfigureOrder(FrameComponentOrdered.CACHE)
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
			Set<String> cacheLoaderClassNames = new LinkedHashSet<String>(SpringFactoriesLoader.loadFactoryNames(CacheLoader.class, this.classLoader));
			if (Assert.isEmpty(cacheLoaderClassNames)) {
				logger.warn("在/META-INF/spring.factories配置文件中未找到缓存加载器");
				return;
			}
			Iterator<String> ite = cacheLoaderClassNames.iterator();
			while (ite.hasNext()) {
				String cacheLoaderClassName = ite.next();
				if (registry.containsBeanDefinition(cacheLoaderClassName)) {
					continue;
				}
				GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
				Class<CacheLoader<?, ?>> cacheLoaderClass = null;
				try {
					cacheLoaderClass = (Class<CacheLoader<?, ?>>) Class.forName(cacheLoaderClassName);
				} catch (ClassNotFoundException e) {
					logger.error("获取缓存加载类：" + cacheLoaderClassName + "失败");
				}
				if (null == cacheLoaderClass) {
					continue;
				}
				beanDefinition.setBeanClass(cacheLoaderClass);
				beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				// 将Synthetic设置为true，会造成此类不会被合成，例如，动态代理对其无效
				beanDefinition.setSynthetic(true);
				beanDefinition.setLazyInit(true);
				registry.registerBeanDefinition(cacheLoaderClassName, beanDefinition);
			}
		}

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}
	}
}
