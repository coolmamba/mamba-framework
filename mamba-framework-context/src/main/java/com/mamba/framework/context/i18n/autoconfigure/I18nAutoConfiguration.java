package com.mamba.framework.context.i18n.autoconfigure;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import com.mamba.framework.context.FrameComponentOrdered;
import com.mamba.framework.context.cache.autoconfigure.CacheLoadAutoConfiguration;
import com.mamba.framework.context.i18n.autoconfigure.I18nAutoConfiguration.I18nMessageSourceRegistrar;
import com.mamba.framework.context.i18n.cache.provider.I18nResourceCacheProvider;
import com.mamba.framework.context.i18n.messagesource.I18nMessageSource;
import com.mamba.framework.context.i18n.util.I18nMessageRetriever;

@AutoConfigureOrder(FrameComponentOrdered.DCI18N)
@Configuration
@AutoConfigureAfter(CacheLoadAutoConfiguration.class)
@Import(value = { I18nMessageSourceRegistrar.class })
@EnableConfigurationProperties(I18nProperties.class)
public class I18nAutoConfiguration {
	static class I18nMessageSourceRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
		private static final Log logger = LogFactory.getLog(I18nMessageSourceRegistrar.class);
		
		private ClassLoader classLoader;
		@SuppressWarnings("unchecked")
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			/** @step 1: 往bean工厂注入i18n消息源Bean定义 */
			if (registry.containsBeanDefinition(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)) {
				logger.warn("检测到bean工厂中已经存在beanName=[messageSource]的bean定义，将其移除");
				registry.removeBeanDefinition(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME);
			}
			GenericBeanDefinition messageSourceBeanDefinition = createGenericBeanDefinition(I18nMessageSource.class);
			registry.registerBeanDefinition(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME, messageSourceBeanDefinition);
			
			/** @step 2: 往bean工厂注入i18n消息检索器Bean定义 */
			String i18nMessageRetrieverClassName = I18nMessageRetriever.class.getName();
			if (registry.containsBeanDefinition(i18nMessageRetrieverClassName)) {
				logger.warn("检测到bean工厂中已经存在beanName=[" + i18nMessageRetrieverClassName + "]的bean定义，将其移除");
				registry.removeBeanDefinition(i18nMessageRetrieverClassName);
			}
			GenericBeanDefinition i18nMessageRetrieverBeanDefinition = createGenericBeanDefinition(I18nMessageRetriever.class);
			registry.registerBeanDefinition(i18nMessageRetrieverClassName, i18nMessageRetrieverBeanDefinition);
			
			/** @step 3: 往bean工厂注入i18n源数据提供商bean定义 */
			List<String> providers = SpringFactoriesLoader.loadFactoryNames(I18nResourceCacheProvider.class, this.classLoader);
			for (String providerClassName : providers) {
				Class<I18nResourceCacheProvider> providerClass = null;
				try {
					providerClass = (Class<I18nResourceCacheProvider>) Class.forName(providerClassName);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (null == providerClass) {
					continue;
				}
				GenericBeanDefinition providerBeanDefinition = createGenericBeanDefinition(providerClass);
				registry.registerBeanDefinition(providerClassName, providerBeanDefinition);
			}
		}

		private GenericBeanDefinition createGenericBeanDefinition(Class<?> className) {
			GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
			genericBeanDefinition.setBeanClass(className);
			genericBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			genericBeanDefinition.setSynthetic(true);
			return genericBeanDefinition;
		}

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}
	}
}
