package com.mamba.framework.context.i18n.autoconfigure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.AnnotationMetadata;

import com.mamba.framework.context.FrameworkComponentOrdered;
import com.mamba.framework.context.cache.autoconfigure.CacheLoadAutoConfiguration;
import com.mamba.framework.context.i18n.autoconfigure.I18nAutoConfiguration.I18nMessageSourceRegistrar;
import com.mamba.framework.context.i18n.messagesource.I18nMessageSource;
import com.mamba.framework.context.i18n.util.I18nMessageRetriever;
import com.mamba.framework.context.util.BeanDefinitionRegistryUtil;

@AutoConfigureOrder(FrameworkComponentOrdered.I18N)
@Configuration
@AutoConfigureAfter(CacheLoadAutoConfiguration.class)
@Import(value = { I18nMessageSourceRegistrar.class })
@EnableConfigurationProperties(I18nProperties.class)
public class I18nAutoConfiguration {
	static class I18nMessageSourceRegistrar implements ImportBeanDefinitionRegistrar {
		private static final Log logger = LogFactory.getLog(I18nMessageSourceRegistrar.class);
		
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			/** @step 1: 往bean工厂注入i18n消息源Bean定义 */
			if (registry.containsBeanDefinition(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)) {
				logger.warn("检测到bean工厂中已经存在beanName=[messageSource]的bean定义，将其移除");
				registry.removeBeanDefinition(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME);
			}
			BeanDefinitionRegistryUtil.registerInfrastructureBeanDefinition(registry,
				AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME, I18nMessageSource.class);
			
			/** @step 2: 往bean工厂注入i18n消息检索器Bean定义 */
			String i18nMessageRetrieverClassName = I18nMessageRetriever.class.getName();
			if (registry.containsBeanDefinition(i18nMessageRetrieverClassName)) {
				logger.warn("检测到bean工厂中已经存在beanName=[" + i18nMessageRetrieverClassName + "]的bean定义，将其移除");
				registry.removeBeanDefinition(i18nMessageRetrieverClassName);
			}
			BeanDefinitionRegistryUtil.registerInfrastructureBeanDefinition(registry, I18nMessageRetriever.class);
		}
	}
}
