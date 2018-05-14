package com.mamba.framework.context.beans.beantranslator.autoconfigure;

import java.util.List;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import com.mamba.framework.context.FrameworkComponentOrdered;
import com.mamba.framework.context.beans.beantranslator.autoconfigure.BeanTranslatorAutoConfiguration.BeanTranslatorRegistrar;
import com.mamba.framework.context.beans.beantranslator.core.BeanFieldTranslator;
import com.mamba.framework.context.beans.beantranslator.core.BeanTranslator;
import com.mamba.framework.context.cache.autoconfigure.CacheLoadAutoConfiguration;
import com.mamba.framework.context.util.BeanDefinitionRegistryUtil;

@AutoConfigureOrder(FrameworkComponentOrdered.BEAN_TRANSLATOR)
@Configuration
@AutoConfigureAfter(value = { CacheLoadAutoConfiguration.class })
@Import(value = { BeanTranslatorRegistrar.class })
public class BeanTranslatorAutoConfiguration {

	static class BeanTranslatorRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
		private ClassLoader classLoader;

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			BeanDefinitionRegistryUtil.registerInfrastructureBeanDefinition(registry, BeanTranslator.class, false);

			List<String> beanFieldTranslatorNames = SpringFactoriesLoader.loadFactoryNames(BeanFieldTranslator.class, this.classLoader);
			for (int i = 0; null != beanFieldTranslatorNames && i < beanFieldTranslatorNames.size(); i++) {
				String beanFieldTranslatorName = beanFieldTranslatorNames.get(i);
				try {
					Class<?> clazz = ClassUtils.forName(beanFieldTranslatorName, this.classLoader);
					BeanDefinitionRegistryUtil.registerInfrastructureBeanDefinition(registry, clazz);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (LinkageError e) {
					e.printStackTrace();
				}
			}
		}
	}
}
