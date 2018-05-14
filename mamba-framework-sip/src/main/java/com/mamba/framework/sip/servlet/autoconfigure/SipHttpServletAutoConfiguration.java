package com.mamba.framework.sip.servlet.autoconfigure;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
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
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;

import com.mamba.framework.context.FrameworkComponentOrdered;
import com.mamba.framework.context.util.BeanDefinitionRegistryUtil;
import com.mamba.framework.sip.context.cache.retriever.SipRetriever;
import com.mamba.framework.sip.servlet.SipHttpServlet;
import com.mamba.framework.sip.servlet.autoconfigure.SipHttpServletAutoConfiguration.SipHttpServletCoreComponentRegistrar;

/**
 * {@link SipHttpServlet}自动配置类
 * 
 * {@link SipHttpServletConfiguration}会创建一个{@link SipHttpServlet}对象，
 * 并根据{@link SipHttpProperties}属性信息初始化{@link SipHttpServlet}对象，
 * 最后会将{@link SipHttpServlet}对象注册到Bean工厂中。
 * 
 * {@link SipHttpServletRegistrationConfiguration}会创建一个{@link ServletRegistrationBean}对象，
 * 由于此对象实现了{@link ServletContextInitializer}接口，当Servlet加载并初始化完毕之后，
 * 会触发{@link ServletContextInitializer#onStartup(javax.servlet.ServletContext)} 方法执行。
 * 在{@link ServletRegistrationBean#onStartup(javax.servlet.ServletContext)}方法中，
 * 会将{@link SipHttpServlet}对象注册到Servlet容器中。
 * 
 * @author junmamba
 */
@AutoConfigureOrder(FrameworkComponentOrdered.SIP)
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(SipHttpServlet.class)
@AutoConfigureAfter({ EmbeddedServletContainerAutoConfiguration.class, CacheAutoConfiguration.class })
@Import(value = { SipHttpServletCoreComponentRegistrar.class })
public class SipHttpServletAutoConfiguration {
	public static final String DEFAULT_SIP_SERVLET_BEAN_NAME = "sipHttpServlet";
	public static final String DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME = "sipHttpServletRegistration";
	
	/**
	 * SipHttpServlet核心主键注册
	 * @author junmamba
	 *
	 */
	static class SipHttpServletCoreComponentRegistrar implements ImportBeanDefinitionRegistrar {
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			BeanDefinitionRegistryUtil.registerInfrastructureBeanDefinition(registry, SipRetriever.class);
		}
	}

	@Configuration
	@Conditional(DefaultHttpSipServletCondition.class)
	@ConditionalOnClass(ServletRegistration.class)
	@EnableConfigurationProperties(SipHttpProperties.class)
	protected static class SipHttpServletConfiguration {
		private final SipHttpProperties sipProperties;
		
		public SipHttpServletConfiguration(SipHttpProperties sipProperties) {
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
	private static class DefaultHttpSipServletCondition extends SpringBootCondition {
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
	@EnableConfigurationProperties(SipHttpProperties.class)
	@Import(SipHttpServletConfiguration.class)
	protected static class SipHttpServletRegistrationConfiguration {
		private final SipHttpProperties sipProperties;

		public SipHttpServletRegistrationConfiguration(SipHttpProperties sipProperties) {
			this.sipProperties = sipProperties;
		}

		@Bean(name = DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME)
		@ConditionalOnBean(value = SipHttpServlet.class, name = DEFAULT_SIP_SERVLET_BEAN_NAME)
		public ServletRegistrationBean dispatcherServletRegistration(SipHttpServlet sipServlet) {
			ServletRegistrationBean registration = new ServletRegistrationBean(sipServlet, this.sipProperties.getServletMapping());
			registration.setName(DEFAULT_SIP_SERVLET_BEAN_NAME);
			registration.setLoadOnStartup(this.sipProperties.getLoadOnStartup());
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
			
			return ConditionOutcome.match(message.found("servlet registration beans")
								   .items(Style.QUOTE, registrations)
								   .append("and none is named " + DEFAULT_SIP_SERVLET_REGISTRATION_BEAN_NAME));
		}

		private ConditionMessage.Builder startMessage() {
			return ConditionMessage.forCondition("SipServlet Registration");
		}
	}
}
