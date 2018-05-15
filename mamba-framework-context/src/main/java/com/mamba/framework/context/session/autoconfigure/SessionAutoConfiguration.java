package com.mamba.framework.context.session.autoconfigure;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import com.mamba.framework.context.FrameworkComponentOrdered;
import com.mamba.framework.context.cache.autoconfigure.CacheLoadAutoConfiguration;
import com.mamba.framework.context.session.autoconfigure.SessionAutoConfiguration.SessionRegistrar;
import com.mamba.framework.context.session.provider.OperatorProvider;
import com.mamba.framework.context.util.Assert;
import com.mamba.framework.context.util.BeanDefinitionRegistryUtil;

@AutoConfigureOrder(FrameworkComponentOrdered.SESSION)
@Configuration
@AutoConfigureAfter(value = { CacheLoadAutoConfiguration.class })
@Import(value = { SessionRegistrar.class })
public class SessionAutoConfiguration {
	static class SessionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
		private static final Log logger = LogFactory.getLog(SessionRegistrar.class);

		private ClassLoader classLoader;

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			List<String> operatorProviderClassNames = SpringFactoriesLoader.loadFactoryNames(OperatorProvider.class, this.classLoader);
			if (Assert.isEmpty(operatorProviderClassNames)) {
				logger.error("未配置操作员提供商");
				return;
			}
			if (operatorProviderClassNames.size() != 1) {
				logger.error("操作员提供商存在多个");
				return;
			}
			
			String operatorProviderClassName = operatorProviderClassNames.get(0);
			if (registry.containsBeanDefinition(operatorProviderClassName)) {
				logger.info("Spring IoC容器中已经有操作员提供商了，忽略");
				return;
			}
			Class<?> clazz = null;
			try {
				clazz = Class.forName(operatorProviderClassName);
			} catch (ClassNotFoundException e) {
				logger.error("加载类：" + operatorProviderClassName + "失败，原因：" + e.getMessage());
			}
			if (null == clazz) {
				return;
			}
			BeanDefinitionRegistryUtil.registerInfrastructureBeanDefinition(registry, clazz);
		}
	}
}
