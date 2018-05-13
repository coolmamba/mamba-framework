package com.mamba.framework.context.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class BeanDefinitionRegistryUtil {
	public static void registerInfrastructureBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass) {
		registerBeanDefinition(registry, beanClass, BeanDefinition.ROLE_INFRASTRUCTURE, true, true);
	}
	
	public static void registerInfrastructureBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass, boolean isLazyInit) {
		registerBeanDefinition(registry, beanClass, BeanDefinition.ROLE_INFRASTRUCTURE, true, isLazyInit);
	}

	public static void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass, int role,
			boolean isSynthetic, boolean isLazyInit) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setRole(role);
		// 将Synthetic设置为true，会造成此类不会被合成，例如，动态代理对其无效
		beanDefinition.setSynthetic(isSynthetic);
		beanDefinition.setLazyInit(isLazyInit);
		registry.registerBeanDefinition(beanClass.getName(), beanDefinition);
	}
}
