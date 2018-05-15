package com.mamba.framework.sip.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpMethod;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.mamba.framework.context.cache.event.CacheLoadedApplicationEvent;
import com.mamba.framework.context.cache.runner.CacheLoadApplicationRunner;
import com.mamba.framework.context.constant.RespEnum;
import com.mamba.framework.context.exception.BusinessException;
import com.mamba.framework.context.i18n.cache.retriever.I18nMessageCacheRetriever;
import com.mamba.framework.context.session.SessionManager;
import com.mamba.framework.context.session.core.Operator;
import com.mamba.framework.context.session.core.Session;
import com.mamba.framework.context.session.provider.OperatorProvider;
import com.mamba.framework.context.util.Assert;
import com.mamba.framework.context.util.StringUtils;
import com.mamba.framework.sip.context.cache.bean.AccessChannel;
import com.mamba.framework.sip.context.cache.bean.SipBusiAccess;
import com.mamba.framework.sip.context.cache.bean.SipExceptionCode;
import com.mamba.framework.sip.context.cache.retriever.SipCacheRetriever;
import com.mamba.framework.sip.context.constant.SipExceptionKey;
import com.mamba.framework.sip.context.exception.SipException;
import com.mamba.framework.sip.servlet.bean.SipBusiReqBodyBean;
import com.mamba.framework.sip.servlet.bean.SipPubReqInfoBean;
import com.mamba.framework.sip.servlet.bean.SipReqBean;
import com.mamba.framework.sip.servlet.bean.SipRespBean;
import com.mamba.framework.sip.servlet.event.SipHttpServletHandledEvent;

/**
 * SIP(服务接口协议) HttpServlet实现
 * @important_warning: 仅支持POST请求
 * 
 * @请求报文格式
 * {
 * 		"PubReqInfo": {// 公共参数
 * 			"OperatorId": "", 
 * 			"AccessChannel": ""
 * 		}, 
 * 		"BusiReqBody": {// 业务请求报文主体
 * 			"BusiCode":"", 
 * 			"BusiParams": {// 业务参数
 * 			}
 * 		} 
 * }
 * @响应报文格式
 * {
 * 		"PubRespInfo" : {
 * 			"RespCode" : "",
 * 			"RespDesc" : "",
 * 		},
 * 		"BusiRespBody" : {
 * 		}
 * }
 * @author junmamba
 *
 */
public class SipHttpServlet extends SipHttpServletBean implements ApplicationContextAware, BeanClassLoaderAware, InitializingBean {
	private Log logger = LogFactory.getLog(SipHttpServlet.class);

	@Autowired
	private SipCacheRetriever sipRetriever;
	
	@Autowired
	private I18nMessageCacheRetriever i18nMessageRetriever;
	
	@Autowired
	private OperatorProvider operatorProvider;
	
	private ApplicationContext context;
	private ClassLoader classLoader;
	private StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
	
	private static final String BUSI_RESP_BODY = "BusiRespBody";
	private static final String PUB_RESP_INFO = "PubRespInfo";
	private static final String PUB_RESP_INFO_RESPCODE = "RespCode";
	private static final String PUB_RESP_INFO_RESPDESC = "RespDesc";
	
	private static final int MAX_CONTEXT_LENHTH = 65536;
	private static final int DEFAULT_CONTEXT_LENHTH = 65536;
	private static final String DEFAULT_ENCODING = "UTF-8";
	
	private static final String DELIMITER = "$$$$";
	private final Map<String, Method> serviceMethodMapping = new HashMap<String, Method>();
	private final Map<String, String> serviceClassMappingAlias = new HashMap<String, String>();
	
	private SerializeConfig fastJsonConfig = new SerializeConfig(); 

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;
		SipReqBean sipReqBean = null;
		try {
			// 校验请求信息
			checkRequestInfo(request);
			// 解析请求报文
			sipReqBean = parseRequestContent(request);;
			// 请求分发
			SipRespBean sipRespBean = doDispatch(sipReqBean);
			// 返回相应结果
			response.setContentType("text/json; charset=UTF-8");
			response.getWriter().print(JSONObject.toJSONString(sipRespBean.getResponseMap(), this.fastJsonConfig));
		} catch (ServletException ex) {
			failureCause = ex;
		} catch (IOException ex) {
			failureCause = ex;
		} catch (InvocationTargetException ex) {
			failureCause = ex.getTargetException();
		}catch (Throwable ex) {
			failureCause = ex;
		} finally {
			// 清空会话
			SessionManager.setSession(null);
			
			// 发布SIP请求完成事件
			long processingTime = System.currentTimeMillis() - startTime;
			this.context.publishEvent(new SipHttpServletHandledEvent(this, request.getMethod(), failureCause, processingTime));
			
			// 打印异常堆栈信息
			if (failureCause != null) {
				logger.error(getExceptionFullStackMessage(failureCause));
			}
			
			// 将异常信息写入到响应结果中
			if (null != failureCause) {
				int accessChannel = (sipReqBean == null ? -1 : sipReqBean.getPubReqInfo().getAccessChannel());
				writeFailInfoToResponse(response, failureCause, accessChannel);
			}
		}
	}
	
	/**
	 * 校验请求信息
	 * @param request
	 * @throws IOException
	 */
	private void checkRequestInfo(HttpServletRequest request) throws IOException {
		String method = request.getMethod().toUpperCase();
		// 系统仅支持POST请求
		if (!HttpMethod.POST.name().equals(method)) {
			throw new SipException(SipExceptionKey.SIP000000, method);
		}
		// 系统不支持文件上传
		if (multipartResolver.isMultipart(request)) {
			throw new SipException(SipExceptionKey.SIP000001);
		}
		// 系统仅支持JSON数据格式
		String contentType = request.getContentType();
		if (!"application/json".equals(contentType)) {
			throw new SipException(SipExceptionKey.SIP000002);
		}
		// 报文数据长度校验
		int contentLength = request.getContentLength();
		if (contentLength > MAX_CONTEXT_LENHTH) {
			throw new SipException(SipExceptionKey.SIP000003);
		}
		if (contentLength == 0) {
			throw new SipException(SipExceptionKey.SIP000004);
		}
	}
	
	/**
	 * 请求分发
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private SipRespBean doDispatch(SipReqBean sipReqBean) throws Exception {
		// 初始化会话
		initSession(sipReqBean);
		
		// 获取SIP接入配置
		SipBusiAccess sipBusiAccess = getSipBusiAccess(sipReqBean);
		String serviceClassName = sipBusiAccess.getServiceClassName();
 		Object service = getService(serviceClassName, sipBusiAccess.getBusiCode());
 		
 		// 发起方法调用
		String key = serviceClassName + DELIMITER + sipBusiAccess.getServiceMethodName();
		Method method = this.serviceMethodMapping.get(key);
		Map<String, Object> busiParams = sipReqBean.getBusiReqBody().getBusiParams();
		if (busiParams == null) {
			busiParams = new HashMap<String, Object>();
		}
		
		Object result = method.invoke(service, busiParams);
		if (null == result) {
			result = new HashMap<String, Object>();
		}
		
		Map<String, Object> responseMap = new HashMap<String, Object>();
		// 公共部分
		Map<String, Object> pubRespInfoMap = new HashMap<String, Object>();
		pubRespInfoMap.put(PUB_RESP_INFO_RESPCODE, RespEnum.SUCCESS.respCode);
		pubRespInfoMap.put(PUB_RESP_INFO_RESPDESC, RespEnum.SUCCESS.respDesc);

		responseMap.put(PUB_RESP_INFO, pubRespInfoMap);// 公共部分
		responseMap.put(BUSI_RESP_BODY, result);// 业务部分
 		
		return new SipRespBean(responseMap, sipReqBean);
	}
	
	/**
	 * 初始化会话信息
	 * @param sipReqBean
	 */
	private void initSession(SipReqBean sipReqBean) {
		Session session = new Session();
		// 操作员信息
		Operator operator = this.operatorProvider.getOperator(sipReqBean.getPubReqInfo().getOperatorId());
		session.setOperator(operator);
		// 渠道信息
		session.setAccessChannel(sipReqBean.getPubReqInfo().getAccessChannel());
		session.setAccessChannelName(sipReqBean.getPubReqInfo().getAccessChannelName());
		SessionManager.setSession(session);
	}
	
	/**
	 * 获取SIP服务
	 * 
	 * @param serviceClassName
	 * @return
	 */
	private Object getService(String serviceClassName, String busiCode) throws SipException {
		Object service;
		try {
			String serviceClassAliasName = this.serviceClassMappingAlias.get(serviceClassName);
			if (StringUtils.isNotBlank(serviceClassAliasName)) {
				service = this.context.getBean(serviceClassAliasName);
			} else {
				try {
					service = this.context.getBean(serviceClassName);
				} catch (BeansException e) {
					logger.error("ApplicationContext根据类全路径名：{" + serviceClassName + "}获取服务对象失败");
					logger.error("通过别名再次尝试");
					String[] arr = serviceClassName.split("\\.");
					serviceClassAliasName = StringUtils.uncapitalize(arr[arr.length - 1]);
					service = this.context.getBean(serviceClassAliasName);
					this.serviceClassMappingAlias.put(serviceClassName, serviceClassAliasName);
				}
			}
		} catch (Exception e) {
			throw new SipException(SipExceptionKey.SIP000012, new String[] { busiCode, serviceClassName });
		}
		if (null == service) {
			throw new SipException(SipExceptionKey.SIP000012, new String[] { busiCode, serviceClassName });
		}
		return service;
	}
	
	/**
	 * 解析请求报文
	 * @param request
	 * @return
	 * @throws SipException
	 */
	private SipReqBean parseRequestContent(HttpServletRequest request) throws SipException {
		// 读取请求内容
		String content = readRequestContent(request);

		// 解析
		SipReqBean sipReqBean = null;
		try {
			sipReqBean = JSONObject.parseObject(content, SipReqBean.class);
		} catch (Exception e) {
			logger.error(getExceptionFullStackMessage(e));
			throw new SipException(SipExceptionKey.SIP000006);
		}
		if (null == sipReqBean) {
			throw new SipException(SipExceptionKey.SIP000006);
		}

		// 校验
		SipPubReqInfoBean pubReqInfoBean = sipReqBean.getPubReqInfo();
		SipBusiReqBodyBean busiReqBody = sipReqBean.getBusiReqBody();
		if (null == pubReqInfoBean || null == busiReqBody) {
			throw new SipException(SipExceptionKey.SIP000007);
		}

		AccessChannel accessChannelBean = this.sipRetriever.getAccessChannel(pubReqInfoBean.getAccessChannel());
		if (null == accessChannelBean) {// 接入渠道非法
			throw new SipException(SipExceptionKey.SIP000008, pubReqInfoBean.getAccessChannel());
		}
		pubReqInfoBean.setAccessChannelName(accessChannelBean.getAccessChannelName());
		
		if (StringUtils.isBlank(busiReqBody.getBusiCode())) {// 业务编码为空
			throw new SipException(SipExceptionKey.SIP000009);
		}
		return sipReqBean;
	}
	
	/**
	 * 读取请求内容
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String readRequestContent(HttpServletRequest request) throws SipException {
		String content = StringUtils.EMPTY;
		try {
			int contentLength = request.getContentLength();
			byte[] buffer = (byte[]) null;
			if (contentLength > DEFAULT_CONTEXT_LENHTH)
				buffer = new byte[contentLength];
			else {
				buffer = new byte[DEFAULT_CONTEXT_LENHTH];
			}
			
			String encode = request.getCharacterEncoding();
			if (StringUtils.isBlank(encode)) {
				encode = DEFAULT_ENCODING;
			}
			
			InputStream in = request.getInputStream();
			int length = in.read(buffer);

			byte[] data = new byte[length];
			System.arraycopy(buffer, 0, data, 0, length);
			content = new String(data, encode).trim();
		} catch (Exception e) {
			logger.error(getExceptionFullStackMessage(e));
			throw new SipException(SipExceptionKey.SIP000005);
		}
		if (StringUtils.isBlank(content)) {
			throw new SipException(SipExceptionKey.SIP000004);
		}
		return content;
	}
	
	/**
	 * 获取SIP业务接入配置
	 * 
	 * @param busiCode
	 * @param accessChannel
	 * @return
	 * @throws SipException
	 */
	private SipBusiAccess getSipBusiAccess(SipReqBean sipReqBean) throws SipException {
		int accessChannel = sipReqBean.getPubReqInfo().getAccessChannel();
		String accessChannelName = sipReqBean.getPubReqInfo().getAccessChannelName();
		String busiCode = sipReqBean.getBusiReqBody().getBusiCode();
		
		// 获取SIP接入配置
		SipBusiAccess sipBusiAccess = this.sipRetriever.getSipBusiAccess(busiCode, accessChannel);
		if (null == sipBusiAccess && accessChannel != -1) {
			sipBusiAccess = this.sipRetriever.getSipBusiAccess(busiCode, -1);
		}
		if (null == sipBusiAccess) {
			throw new SipException(SipExceptionKey.SIP000010, new String[] {busiCode, accessChannelName});
		}
		
		// 接口配置检查
		String serviceClassName = sipBusiAccess.getServiceClassName();
		String serviceMethodName = sipBusiAccess.getServiceMethodName();
		if (StringUtils.isBlank(serviceClassName) || StringUtils.isBlank(serviceMethodName)) {
			throw new SipException(SipExceptionKey.SIP000011, busiCode);
		}
		return sipBusiAccess;
	}
	
	/**
	 * 将异常信息写入到response中
	 * @param response
	 * @param failureCause
	 * @param accessChannel
	 * @throws IOException
	 */
	private void writeFailInfoToResponse(HttpServletResponse response, Throwable failureCause, int accessChannel) throws IOException {
		// 获取SIP异常编码配置
		String exceptionKey = getExceptionKey(failureCause);
		SipExceptionCode sipExceptionCode = this.sipRetriever.getSipExceptionCode(exceptionKey, accessChannel);
		if (null == sipExceptionCode && accessChannel != -1) {
			sipExceptionCode = this.sipRetriever.getSipExceptionCode(exceptionKey, -1);
		}
		
		Map<String, Map<String, String>> responseMap = new HashMap<String, Map<String, String>>();
		Map<String, String> respHeadMap = new HashMap<String, String>();
		
		String respCode = StringUtils.EMPTY;
		String respDesc = StringUtils.EMPTY;
		if (null != sipExceptionCode) {// 如果配置了SIP_EXCEPTION_CODE
			respCode = sipExceptionCode.getCode();
			respDesc = sipExceptionCode.getDesc();
			if (StringUtils.isBlank(respDesc)) {
				respDesc = getExceptionMessage(failureCause);
			}
		}
		if (StringUtils.isBlank(respCode)) {
			if (failureCause instanceof BusinessException) {
				respCode = RespEnum.BUSINESS_SO_FAIL.respCode;
				respDesc = getExceptionMessage(failureCause);
			} else {
				respCode = RespEnum.SYSTEM_EXCEPTION.respCode;
				respDesc = RespEnum.SYSTEM_EXCEPTION.respDesc;
			}
		} 
		
		respHeadMap.put(PUB_RESP_INFO_RESPCODE, respCode);
		respHeadMap.put(PUB_RESP_INFO_RESPDESC, respDesc);
		responseMap.put(PUB_RESP_INFO, respHeadMap);
		response.setContentType("text/json; charset=UTF-8");
		response.getWriter().print(JSONObject.toJSONString(responseMap));
	}
	
	private String getExceptionMessage(Throwable ex) {
		if (ex instanceof BusinessException) {
			BusinessException businessException = (BusinessException) ex;
			return this.i18nMessageRetriever.getMessage(businessException.getKey(), businessException.getArgs());
		} else {
			return ex.getMessage();
		}
	}
	
	@Override
	protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing JF SipServlet '" + getServletName() + "'");
		if (logger.isInfoEnabled()) {
			logger.info("SipServlet '" + getServletName() + "' 开始初始化工作");
		}
		long startTime = System.currentTimeMillis();
		// 初始化fastJson配置，设置响应报文key首字母大写
		this.fastJsonConfig.propertyNamingStrategy = PropertyNamingStrategy.PascalCase;;
		
		if (this.logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			this.logger.info("SipServlet '" + getServletName() + "' 初始化工作完成，耗时： " + elapsedTime + " ms");
		}
	}
	
	@Override
	protected final void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}
	
	@Override
	protected  final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}
	
	@Override
	protected final void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}
	
	@Override
	protected final void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}
	
	@Override
	protected final void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}
	
	@Override
	protected final void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.processRequest(request, response);
	}
	
	private String getExceptionFullStackMessage(Throwable e) {
        String ret = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            // out
            e.printStackTrace(pw);
            // flush
            pw.flush();
            sw.flush();
            ret = sw.toString();
            // close
            sw.close();
            pw.close();
        } catch (Exception e1) {
			if (logger.isErrorEnabled()) {
				logger.error("", e1);
			}
            ret = "系统异常";
        }
        return ret;
    }
	
	private String getExceptionKey(Throwable ex) {
		String exceptionKey = StringUtils.EMPTY;
		BusinessException businessException = null;
		if (ex instanceof BusinessException) {
			businessException = (BusinessException) ex;
			exceptionKey = businessException.getKey();
		}
		if (StringUtils.isBlank(exceptionKey)) {
			exceptionKey = "SIP00000001";
		}
		return exceptionKey;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}
	
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	protected final void initServiceClass() throws ClassNotFoundException, LinkageError {
		Cache<String, SipBusiAccess> cache = this.sipRetriever.getSipBusiAccess();
		Iterator<Cache.Entry<String, SipBusiAccess>> ite = cache.iterator();
		// 所有的服务类
		Set<String> allServiceClassNames = new HashSet<String>();
		// 服务类与方法名之间的映射
		Map<String, Set<String>> serviceClassMappingMethodName = new HashMap<String, Set<String>>();
		
		/** 获取所有的SIP服务类和方法名 */
		while (ite.hasNext()) {
			Cache.Entry<String, SipBusiAccess> entry = ite.next();
			SipBusiAccess sipBusiAccess = entry.getValue();
			String serviceClassName = sipBusiAccess.getServiceClassName();// 服务类
			String serviceMethodname = sipBusiAccess.getServiceMethodName();// 方法名
			
			if (StringUtils.isBlank(serviceClassName) || StringUtils.isBlank(serviceMethodname)) {
				continue;
			}

			serviceClassName = serviceClassName.trim();
			serviceMethodname = serviceMethodname.trim();

			// 所有的服务类
			allServiceClassNames.add(serviceClassName);
			
			// 服务类与方法名之间的映射
			Set<String> serviceMethodNames = serviceClassMappingMethodName.get(serviceClassName);
			if (null == serviceMethodNames) {
				serviceMethodNames = new HashSet<String>();
				serviceClassMappingMethodName.put(serviceClassName, serviceMethodNames);
			}
			serviceMethodNames.add(serviceMethodname);
		}
		
		Iterator<String> allServiceClassNamesIte = allServiceClassNames.iterator();
		while (allServiceClassNamesIte.hasNext()) {
			String serviceClassName = allServiceClassNamesIte.next();
			final Set<String> serviceMethodNames = serviceClassMappingMethodName.get(serviceClassName);
			if (Assert.isEmpty(serviceMethodNames)) {
				continue;
			}
			
			Class<?> serviceClass = null;
			try {
				serviceClass = ClassUtils.forName(serviceClassName, this.classLoader);
			} catch (Exception e) {
				logger.error("加载服务类：{" + serviceClassName + "}失败");
			}
			if (null == serviceClass) {
				continue;
			}
			ReflectionUtils.doWithMethods(serviceClass, new ReflectionUtils.MethodCallback() {
				@Override
				public void doWith(Method method) {
					if (!Modifier.isPublic(method.getModifiers())) {
						return;
					}
					if (Modifier.isStatic(method.getModifiers())) {
						return;
					}
					// 参数只能是Map<String, Object>
					if (!(null != method.getParameterTypes() && 1 == method.getParameterTypes().length)) {
						return;
					}
					String paramClass = method.getParameterTypes()[0].getName();
					if(!"java.util.Map".equals(paramClass)) {
						return;
					}
					if (!serviceMethodNames.contains(method.getName())) {
						return;
					}
					String key = serviceClassName + DELIMITER + method.getName();
					serviceMethodMapping.put(key, method);
				}
			});
		}
	}
	
	/**
	 * 缓存加载完成监听器
	 * @author junmamba
	 */
	private class CacheLoadedListener implements ApplicationListener<CacheLoadedApplicationEvent> {
		@Override
		public void onApplicationEvent(CacheLoadedApplicationEvent event) {
			SipHttpServlet.this.onCacheLoadedApplicationEvent(event);
		}
	}
	
	public void onCacheLoadedApplicationEvent(CacheLoadedApplicationEvent event) {
		try {
			initServiceClass();
		} catch (Exception e) {
			logger.error(getExceptionFullStackMessage(e));
		}
	}
	
	/**
	 * SipHttpServlet对象实例化之后，向上下文中注册CacheLoadedListener监听器。
	 * 后续{@link CacheLoadApplicationRunner}完成缓存加载工作，会通过应用程序上下文发布{@link CacheLoadedApplicationEvent}事件。
	 * CacheLoadedListener监听到CacheLoadedListener时间，即可完成相关操作
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.context instanceof ConfigurableWebApplicationContext) {
			((ConfigurableWebApplicationContext) this.context).addApplicationListener(new CacheLoadedListener());
		}
	}
}
