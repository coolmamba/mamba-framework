package com.mamba.framework.sip.servlet.autoconfigure;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Style;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;

import com.jf.crm.common.framework.sip.provider.AccessChannelSourceProvider;
import com.mamba.framework.context.util.Assert;
import com.mamba.framework.sip.servlet.SipHttpServlet;
import com.mamba.framework.sip.servlet.autoconfigure.SipServletAutoConfiguration.SipServletComponentRegistrar;

/**
 * {@link SipHttpServlet}自动配置类
 * 
 * {@link SipServletConfiguration}会创建一个{@link SipHttpServlet}对象，根据{@link SipProperties}信息初始化{@link SipHttpServlet}对象，
 * 最后会将{@link SipHttpServlet}对象注册到Bean工厂中。
 * 
 * 可以通过application.yaml文件来配置{@link SipProperties}相关字段值，其前缀为sip
 * 
 * {@link SipServletRegistrationConfiguration}会创建一个{@link ServletRegistrationBean}对象，
 * 由于此对象实现了{@link ServletContextInitializer}接口，当Servlet加载并初始化完毕之后，会触发{@link ServletContextInitializer#onStartup(javax.servlet.ServletContext)}
 * 方法执行。
 * 在{@link ServletRegistrationBean#onStartup(javax.servlet.ServletContext)}方法中，会将{@link SipHttpServlet}对象注册到Servlet容器中。
 * 
 * @author junmamba
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(SipHttpServlet.class)
@AutoConfigureAfter(EmbeddedServletContainerAutoConfiguration.class)
@Import(value = { SipServletComponentRegistrar.class })
public class SipServletAutoConfiguration {
	public static final String DEFAULT_SIP_SERVLET_BEAN_NAME = "sipHttpServlet";
	public static final String DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME = "sipHttpServletRegistration";
	
	static class SipServletComponentRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {
		private static final Log logger = LogFactory.getLog(SipServletComponentRegistrar.class);

		private ClassLoader classLoader;

		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			Set<String> classNames = new LinkedHashSet<String>(SpringFactoriesLoader.loadFactoryNames(AccessChannelSourceProvider.class, this.classLoader));
			if (Assert.isEmpty(classNames)) {
				logger.warn("在/META-INF/spring.factories配置文件中未找到[接入方数据源提供商]");
			}

			Iterator<String> ite = classNames.iterator();
			while (ite.hasNext()) {
				String accessChannelSourceProviderClassName = ite.next();
				if (registry.containsBeanDefinition(accessChannelSourceProviderClassName)) {
					continue;
				}
				GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
				Class<AccessChannelSourceProvider> accessChannelSourceProviderClass = null;
				try {
					accessChannelSourceProviderClass = (Class<AccessChannelSourceProvider>) Class.forName(accessChannelSourceProviderClassName);
				} catch (ClassNotFoundException e) {
					logger.error("获取[接入方数据源提供商]：" + accessChannelSourceProviderClass + " 类信息失败");
				}
				if (null == accessChannelSourceProviderClass) {
					continue;
				}
				beanDefinition.setBeanClass(accessChannelSourceProviderClass);
				beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				// 将Synthetic设置为true，会造成此类不会被合成，例如，动态代理对其无效
				beanDefinition.setSynthetic(true);
				beanDefinition.setLazyInit(true);
				registry.registerBeanDefinition(accessChannelSourceProviderClassName, beanDefinition);
			}
		}

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}
	}

	@Configuration
	@Conditional(DefaultSipServletCondition.class)
	@ConditionalOnClass(ServletRegistration.class)
	@EnableConfigurationProperties(SipProperties.class)
	protected static class SipServletConfiguration {
		private final SipProperties sipProperties;
		
		public SipServletConfiguration(SipProperties sipProperties) {
			this.sipProperties = sipProperties;
		}

		@Bean(name = DEFAULT_SIP_SERVLET_BEAN_NAME)
		public SipHttpServlet dispatcherServlet() {
			SipHttpServlet sipServlet = new SipHttpServlet();
			// 可根据sipProperties中的属性信息初始化sipServlet信息
			return sipServlet;
		}
	}
	
	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	private static class DefaultSipServletCondition extends SpringBootCondition {
		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			ConditionMessage.Builder message = ConditionMessage.forCondition("Default SipServlet");
			
			ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			List<String> dispatchServletBeans = Arrays.asList(beanFactory.getBeanNamesForType(SipHttpServlet.class, false, false));
			
			if (dispatchServletBeans.contains(DEFAULT_SIP_SERVLET_BEAN_NAME)) {
				return ConditionOutcome.noMatch(message.found("sip servlet bean").items(DEFAULT_SIP_SERVLET_BEAN_NAME));
			}
			
			if (beanFactory.containsBean(DEFAULT_SIP_SERVLET_BEAN_NAME)) {
				return ConditionOutcome.noMatch(message.found("non sip servlet bean").items(DEFAULT_SIP_SERVLET_BEAN_NAME));
			}
			
			if (dispatchServletBeans.isEmpty()) {
				return ConditionOutcome.match(message.didNotFind("sip servlet beans").atAll());
			}
			
			return ConditionOutcome.match(message.found("sip servlet bean", "sip servlet beans").items(Style.QUOTE, dispatchServletBeans)
												 .append("and none is named " + DEFAULT_SIP_SERVLET_BEAN_NAME));
		}
	}
	
	@Configuration
	@Conditional(SipServletRegistrationCondition.class)
	@ConditionalOnClass(ServletRegistration.class)
	@EnableConfigurationProperties(SipProperties.class)
	@Import(SipServletConfiguration.class)
	protected static class SipServletRegistrationConfiguration {
		private final SipProperties sipProperties;
		private final MultipartConfigElement multipartConfig;

		public SipServletRegistrationConfiguration(SipProperties sipProperties,
			ObjectProvider<MultipartConfigElement> multipartConfigProvider) {
			this.sipProperties = sipProperties;
			this.multipartConfig = multipartConfigProvider.getIfAvailable();
		}

		@Bean(name = DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME)
		@ConditionalOnBean(value = SipHttpServlet.class, name = DEFAULT_SIP_SERVLET_BEAN_NAME)
		public ServletRegistrationBean dispatcherServletRegistration(SipHttpServlet sipServlet) {
			ServletRegistrationBean registration = new ServletRegistrationBean(sipServlet, this.sipProperties.getServletMapping());
			registration.setName(DEFAULT_SIP_SERVLET_BEAN_NAME);
			registration.setLoadOnStartup(this.sipProperties.getLoadOnStartup());
			
			if (this.multipartConfig != null) {
				registration.setMultipartConfig(this.multipartConfig);
			}
			return registration;
		}
	}
	
	@Order(Ordered.LOWEST_PRECEDENCE - 10)
	private static class SipServletRegistrationCondition extends SpringBootCondition {
		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			ConditionOutcome outcome = checkDefaultSipName(beanFactory);
			if (!outcome.isMatch()) {
				return outcome;
			}
			return checkServletRegistration(beanFactory);
		}

		private ConditionOutcome checkDefaultSipName(ConfigurableListableBeanFactory beanFactory) {
			List<String> servlets = Arrays.asList(beanFactory.getBeanNamesForType(SipHttpServlet.class, false, false));
			boolean containsDispatcherBean = beanFactory.containsBean(DEFAULT_SIP_SERVLET_BEAN_NAME);
			if (containsDispatcherBean && !servlets.contains(DEFAULT_SIP_SERVLET_BEAN_NAME)) {
				return ConditionOutcome.noMatch(startMessage().found("non sip servlet").items(DEFAULT_SIP_SERVLET_BEAN_NAME));
			}
			return ConditionOutcome.match();
		}

		private ConditionOutcome checkServletRegistration(ConfigurableListableBeanFactory beanFactory) {
			ConditionMessage.Builder message = startMessage();
			List<String> registrations = Arrays.asList(beanFactory.getBeanNamesForType(ServletRegistrationBean.class, false, false));
			
			boolean containsDispatcherRegistrationBean = beanFactory.containsBean(DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME);
			if (registrations.isEmpty()) {
				if (containsDispatcherRegistrationBean) {
					return ConditionOutcome.noMatch(message.found("non servlet registration bean").items(DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME));
				}
				return ConditionOutcome.match(message.didNotFind("servlet registration bean").atAll());
			}
			
			if (registrations.contains(DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME)) {
				return ConditionOutcome.noMatch(message.found("servlet registration bean").items(DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME));
			}
			
			if (containsDispatcherRegistrationBean) {
				return ConditionOutcome.noMatch(message.found("non servlet registration bean").items(DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME));
			}
			
			return ConditionOutcome.match(message.found("servlet registration beans").items(Style.QUOTE, registrations)
								   .append("and none is named " + DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME));
		}

		private ConditionMessage.Builder startMessage() {
			return ConditionMessage.forCondition("SipServlet Registration");
		}
	}
}
