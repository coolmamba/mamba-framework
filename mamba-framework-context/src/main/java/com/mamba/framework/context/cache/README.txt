一、cache模块概述
	cache模块是基于JCache(即JSR-107规范)。目前，有很多缓存开源框架都遵守此规范，如：Ehcache 3.X、Redis等等，这些开源框架我们俗称JCache缓存提供商

	我们可以在pom.xml文件中，通过dependency引入想要的缓存提供商，例如，采用Ehcache 3.X，如下：
	<dependency>
		<groupId>org.ehcache</groupId>
		<artifactId>ehcache</artifactId>
	</dependency>
	<dependency>
		<groupId>javax.cache</groupId>
		<artifactId>cache-api</artifactId>
	</dependency>

二、如何让你的缓存生效
	1、添加pom.xml包依赖
	由于工程都是基于SpringBoot(见jf-crm/pom.xml)，所以，我们只需要在pom.xml（这里我们放在jf-crm-common工程）添加如下依赖项：
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-cache</artifactId>
	</dependency>
	<dependency>
		<groupId>org.ehcache</groupId>
		<artifactId>ehcache</artifactId>
	</dependency>
	<dependency>
		<groupId>javax.cache</groupId>
		<artifactId>cache-api</artifactId>
	</dependency>
	
	2、启动自动配置
	2.1、SpringBoot的核心就是自动装配，在/META-INF/spring.factories（这里我们放在jf-crm-common工程）添加缓存加载自动配置，如下：
	org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
	com.jf.crm.common.framework.cache.autoconfigure.CacheLoadAutoConfiguration
	2.2、在SpringBoot启动类（如：HttpServiceLauncher）中添加@EnableCaching注解
	2.3、在application.yaml配置文中配置缓存信息
	
	3、自定义缓存加载器【重点】
	3.1、开发人员可以通过实现CacheLoader接口自定义缓存加载器，但是建议继承AbstractCacheLoader抽象类。
	3.2、在工程的/META-INF/spring.factories文件中配置需要被执行的缓存加载器，key为com.jf.crm.common.framework.cache.loader.CacheLoader，例如：
	com.jf.crm.common.framework.cache.loader.CacheLoader=\
	com.jf.crm.common.framework.i18n.cache.loader.I18nResourcesCacheLoader,\
	com.jf.crm.common.base.cache.loader.DcStaticDataCacheLoader
	
	注意：CacheLoader中的V必须实现序列化接口Serializable
	
	3.3、缓存加载器会被加入到Spring应用程序上下文中，所以你可以实现**Aware接口获取Spring中其他的并信息，也可以通过@Autowired注入其他bean信息。
	3.4、由于工程默认的缓存提供商为ehcache 3.X，所以，我们可以在ehcache.xml为缓存制定对应的缓存策略
	
	4、缓存获取
	可以通过CacheRetriever类来获取缓存信息，框架层面会初始化一个CacheRetriever对象，并注入到应用程序上下文中。所以，如果你的对象也注入到来应用程序上下文中，
	就可以通过如下方式获取到CacheRetriever对象实例：
	@Autowired
	private CacheRetriever cacheRetriever;
	
三、基本原理
	详细见：CacheLoadAutoConfiguration和CacheLoadApplicationRunner类实现
	