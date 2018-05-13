package com.mamba.framework.context.beans.beantranslator.core;

/**
 * 常见字段
 * 
 * @author junmamba
 */
public interface NormalFieldTranslator extends BeanFieldTranslator {
	/**
	 * 匹配是否为常见字段
	 * 
	 * @param fieldName
	 * @return
	 */
	public boolean match(String fieldName);
}
