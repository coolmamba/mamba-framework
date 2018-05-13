package com.mamba.framework.context.beans.beantranslator.metadata;

import java.util.ArrayList;
import java.util.List;

public class BeanMetadata {
	private Class<?> beanClass;
	private final List<BeanFieldMetadata> fieldMetaDatas = new ArrayList<BeanFieldMetadata>();;

	public BeanMetadata() {
	}

	public BeanMetadata(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public List<BeanFieldMetadata> getFieldMetaDatas() {
		return fieldMetaDatas;
	}

	public void addFieldMetaDatas(List<BeanFieldMetadata> fieldMetaDatas) {
		if (null != fieldMetaDatas && fieldMetaDatas.size() > 0) {
			this.fieldMetaDatas.addAll(fieldMetaDatas);
		}
	}

	public void addFieldMetaData(BeanFieldMetadata fieldMetaData) {
		if (null != fieldMetaData) {
			this.fieldMetaDatas.add(fieldMetaData);
		}
	}
}
