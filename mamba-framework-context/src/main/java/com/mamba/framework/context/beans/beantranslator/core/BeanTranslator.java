package com.mamba.framework.context.beans.beantranslator.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

import com.mamba.framework.context.beans.beantranslator.metadata.BeanFieldMetadata;
import com.mamba.framework.context.beans.beantranslator.metadata.BeanMetadata;
import com.mamba.framework.context.beans.beantranslator.metadata.FieldTranslatorMetadata;
import com.mamba.framework.context.util.Assert;
import com.mamba.framework.context.util.ClassUtil;
import com.mamba.framework.context.util.StringUtils;

public class BeanTranslator implements InitializingBean, BeanClassLoaderAware {
	private Log logger = LogFactory.getLog(BeanTranslator.class);

	@Autowired
	private ApplicationContext context;
	
	private ClassLoader classLoader;
	
	private final List<BeanMetadata> beanMetaDatas = new ArrayList<BeanMetadata>();
	private final List<BeanFieldTranslator> fieldTranslators = new ArrayList<BeanFieldTranslator>();
	private final List<NormalFieldTranslator> normalFieldTranslators = new ArrayList<NormalFieldTranslator>();

	private final Map<String, List<Field>> classNameMappingFields = new HashMap<String, List<Field>>();
	
	public <T> List<T> translate(List<T> beans) {
		if (null == beans || beans.size() == 0) {
			return beans;
		}
		List<T> translateResults = new ArrayList<T>();
		for (T bean : beans) {
			translateResults.add(translate(bean));
		}

		beans.clear();
		beans.addAll(translateResults);
		return beans;
	}

	
	@SuppressWarnings("unchecked")
	public <T> T translate(T bean) {
		/** @step 1: 准备工作 */
		if (null == bean) {
			return bean;
		}
		BeanMetadata beanMetaData = getBeanMetaData(bean.getClass().getName());
		if (null == beanMetaData) {
			return bean;
		}
		if (Assert.isEmpty(beanMetaData.getFieldMetaDatas())) {
			return bean;
		}

		/** @step 2: 增加属性字段并生成代理对象 */
		BeanGenerator generator = new BeanGenerator();
		generator.setSuperclass(bean.getClass());
		for (BeanFieldMetadata beanFieldMetaData : beanMetaData.getFieldMetaDatas()) {
			generator.addProperty(beanFieldMetaData.getTargetFieldName(), beanFieldMetaData.getTargetFieldClass());
		}
		Object targetBean = generator.create();
		
		/** @step 3: 设置代理对象中的字段值 */
		BeanMap targetBeanMap = BeanMap.create(targetBean);
		for (BeanFieldMetadata beanFieldMetaData : beanMetaData.getFieldMetaDatas()) {
			BeanFieldTranslator beanFieldTranslator = getFieldTranslator(beanFieldMetaData);
			if (null == beanFieldTranslator) {
				continue;
			}
			FieldTranslatorMetadata fieldTranslatorMetaData = new FieldTranslatorMetadata();
			fieldTranslatorMetaData.setSrcBean(bean);
			fieldTranslatorMetaData.setSrcFieldName(beanFieldMetaData.getSrcFieldName());
			fieldTranslatorMetaData.setSrcFieldVlaue(getFieldValue(bean, beanFieldMetaData.getSrcFieldName()));
			targetBeanMap.put(beanFieldMetaData.getTargetFieldName(), beanFieldTranslator.translate(fieldTranslatorMetaData));
		}
		
		BeanUtils.copyProperties(bean, targetBean);
		return (T) targetBean;
	}

	private BeanMetadata getBeanMetaData(String className) {
		if (StringUtils.isBlank(className)) {
			return null;
		}
		for (BeanMetadata metaData : this.beanMetaDatas) {
			if (className.equals(metaData.getBeanClass().getName())) {
				return metaData;
			}
		}
		return null;
	}
	
	private BeanFieldTranslator getFieldTranslator(BeanFieldMetadata metaData) {
		BeanFieldTranslator beanFieldTranslator = null;
		if (this.context != null && StringUtils.isNotBlank(metaData.getFieldTranslatorBeanName())) {
			try {
				beanFieldTranslator = this.context.getBean(metaData.getFieldTranslatorBeanName(), BeanFieldTranslator.class);
			} catch (Exception e) {
				logger.error("Get Bean Fail From Context, Cause: " + e.getMessage());
			}
		}
		if (null == beanFieldTranslator) {
			beanFieldTranslator = getNormalFieldTranslator(metaData.getSrcFieldName());
		}
		return beanFieldTranslator;
	}
	
	private BeanFieldTranslator getNormalFieldTranslator(String fieldName) {
		for (NormalFieldTranslator normalFieldTranslator : this.normalFieldTranslators) {
			if (normalFieldTranslator.match(fieldName)) {
				return normalFieldTranslator;
			}
		}
		return null;
	}
	

	@Override
	public void afterPropertiesSet() throws Exception {
		/** @step 1: 预先注册需要进行翻译的Bean元数据 */
		List<BeanMetadataRegister> beanMetaDataRegisters = SpringFactoriesLoader.loadFactories(BeanMetadataRegister.class, this.classLoader);
		for (int i = 0; null != beanMetaDataRegisters && i < beanMetaDataRegisters.size(); i++) {
			BeanMetadataRegister register = beanMetaDataRegisters.get(i);
			List<BeanMetadata> datas = register.register();
			if (Assert.isNotEmpty(datas)) {
				this.beanMetaDatas.addAll(datas);
			}
		}
		
		/** @step 2: 预加载所有字段 */
		for (BeanMetadata beanMetaData : this.beanMetaDatas) {
			List<Field> allFields = ClassUtil.getFields(beanMetaData.getBeanClass());
			for (int i = 0; null != allFields && i < allFields.size(); i++) {
				allFields.get(i).setAccessible(true);
			}
			this.classNameMappingFields.put(beanMetaData.getBeanClass().getName(), allFields);
		}
		
		/** @step 3: 预加载所有的字段翻译器 */
		Map<String, BeanFieldTranslator> allBeanFieldTranslatorMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				this.context, BeanFieldTranslator.class, true, false);
		if (Assert.isNotEmpty(allBeanFieldTranslatorMap)) {
			this.fieldTranslators.addAll(new ArrayList<BeanFieldTranslator>(allBeanFieldTranslatorMap.values()));
			for (BeanFieldTranslator fieldTranslator : this.fieldTranslators) {
				if (fieldTranslator instanceof NormalFieldTranslator) {
					this.normalFieldTranslators.add((NormalFieldTranslator) fieldTranslator);
				}
			}
		}
	}
	
	private Object getFieldValue(Object bean, String fieldName) {
		Field field = getField(bean.getClass(), fieldName);
		if (null == field) {
			return null;
		}
		try {
			return field.get(bean);
		} catch (Exception e) {
			logger.error("GetFieldValue Fail, Cause: " + e.getMessage());
		}
		return null;
	}

	private Field getField(Class<?> clazz, String fieldName) {
		List<Field> allFields = this.classNameMappingFields.get(clazz.getName());
		for (int i = 0; null != allFields && i < allFields.size(); i++) {
			if (allFields.get(i).getName().equals(fieldName)) {
				return allFields.get(i);
			}
		}
		return null;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
