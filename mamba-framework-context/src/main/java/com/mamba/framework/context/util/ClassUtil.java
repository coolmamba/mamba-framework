package com.mamba.framework.context.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ClassUtil {
	private static final String OBJECT_CLASS_NAME = "java.lang.Object";
	private static final Class<?> SERIALIZABLE_CLASS = Serializable.class;
	private static final String FIELD_SERIALVERSIONUID = "serialVersionUID";

	/**
	 * 获取类（包括父类）中所有的字段域
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFields(Class<?> clazz) {
		if (null == clazz) {
			return null;
		}
		List<Field> allFields = new ArrayList<Field>();
		getFields(clazz, allFields);

		Class<?> supperClass = clazz.getSuperclass();// 父类
		if (!OBJECT_CLASS_NAME.equals(supperClass.getName())) {// 忽略顶层父类Object
			while (supperClass != null) {
				getFields(supperClass, allFields);

				supperClass = supperClass.getSuperclass();// 循环处理
				if (OBJECT_CLASS_NAME.equals(supperClass.getName())) {
					supperClass = null;
				}
			}
		}
		return allFields;
	}

	/**
	 * 获取类中所有的字段域
	 * 
	 * @warning 如果某个类实现类{@link Serializable}接口，忽略serialVersionUID字段域
	 * @param srcClass
	 * @param targetFields
	 */
	public static void getFields(Class<?> srcClass, final List<Field> targetFields) {
		Field[] fields = srcClass.getDeclaredFields();
		boolean isSerializable = SERIALIZABLE_CLASS.isAssignableFrom(srcClass);
		for (int i = 0; null != fields && i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			if (isSerializable && FIELD_SERIALVERSIONUID.equals(fieldName)) {
				continue;
			}
			targetFields.add(field);
		}
	}
}
