package com.mamba.framework.context.beans.beantranslator.metadata;

public class BeanFieldMetadata {
	private String srcFieldName;
	private String targetFieldName;
	private Class<?> targetFieldClass;
	private String fieldTranslatorBeanName;

	public BeanFieldMetadata() {
	}

	public BeanFieldMetadata(String srcFieldName, String targetFieldName, Class<?> targetFieldClass) {
		this(srcFieldName, targetFieldName, targetFieldClass, null);
	}

	public BeanFieldMetadata(String srcFieldName, String targetFieldName, Class<?> targetFieldClass, String fieldTranslatorBeanName) {
		this.srcFieldName = srcFieldName;
		this.targetFieldName = targetFieldName;
		this.targetFieldClass = targetFieldClass;
		this.fieldTranslatorBeanName = fieldTranslatorBeanName;
	}

	public String getSrcFieldName() {
		return srcFieldName;
	}

	public void setSrcFieldName(String srcFieldName) {
		this.srcFieldName = srcFieldName;
	}

	public String getTargetFieldName() {
		return targetFieldName;
	}

	public void setTargetFieldName(String targetFieldName) {
		this.targetFieldName = targetFieldName;
	}

	public Class<?> getTargetFieldClass() {
		return targetFieldClass;
	}

	public void setTargetFieldClass(Class<?> targetFieldClass) {
		this.targetFieldClass = targetFieldClass;
	}

	public String getFieldTranslatorBeanName() {
		return fieldTranslatorBeanName;
	}

	public void setFieldTranslatorBeanName(String fieldTranslatorBeanName) {
		this.fieldTranslatorBeanName = fieldTranslatorBeanName;
	}
}
