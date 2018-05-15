package com.mamba.framework.context.beans.beanmapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mamba.framework.context.util.ClassUtil;
import com.mamba.framework.context.util.StringUtils;

public class BeanMapper {
	private static final String STRING_CLASS_NAME = "java.lang.String";
	private static final Class<?> LIST_CLASS = List.class;
	private static final Class<?> MAP_CLASS = Map.class;

	private static final String BASE_TYPE_INT = "int";
	private static final String BASE_TYPE_DOUBLE = "double";
	private static final String BASE_TYPE_FLOAT = "float";
	private static final String BASE_TYPE_LONG = "long";
	private static final String BASE_TYPE_SHORT = "short";
	private static final String BASE_TYPE_BYTE = "byte";
	private static final String BASE_TYPE_BOOLEAN = "boolean";
	private static final String BASE_TYPE_CHAR = "char";
	private static final String BASE_WRAPPER_TTPE_INTEGER = "java.lang.Integer";
	private static final String BASE_WRAPPER_TTPE_DOUBLE = "java.lang.Double";
	private static final String BASE_WRAPPER_TTPE_FLOAT = "java.lang.Float";
	private static final String BASE_WRAPPER_TTPE_LONG = "java.lang.Long";
	private static final String BASE_WRAPPER_TTPE_SHORT = "java.lang.Short";
	private static final String BASE_WRAPPER_TTPE_BYTE = "java.lang.Byte";
	private static final String BASE_WRAPPER_TTPE_BOOLEAN = "java.lang.Boolean";
	private static final String BASE_WRAPPER_TTPE_CHARACTER = "java.lang.Character";
	// Java基本类型
	public static final Set<String> BASE_TYPES = new HashSet<String>();
	// Java基本类型封装类型
	public static final Set<String> BASE_WRAPPER_TTPE = new HashSet<String>();
	static {
		BASE_TYPES.add(BASE_TYPE_INT);
		BASE_TYPES.add(BASE_TYPE_DOUBLE);
		BASE_TYPES.add(BASE_TYPE_FLOAT);
		BASE_TYPES.add(BASE_TYPE_LONG);
		BASE_TYPES.add(BASE_TYPE_SHORT);
		BASE_TYPES.add(BASE_TYPE_BYTE);
		BASE_TYPES.add(BASE_TYPE_BOOLEAN);
		BASE_TYPES.add(BASE_TYPE_CHAR);

		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_INTEGER);
		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_DOUBLE);
		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_FLOAT);
		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_LONG);
		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_SHORT);
		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_BYTE);
		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_BOOLEAN);
		BASE_WRAPPER_TTPE.add(BASE_WRAPPER_TTPE_CHARACTER);
	}
	
	/**
	 * 将Map转换成JavaBean
	 * 
	 * @param in
	 * @param outClass
	 * @return
	 * @throws Exception
	 */
	public final static <T> T map2Bean(Map<String, Object> in, Class<T> outClass) throws BeanMapperException {
		if (null == in || null == outClass) {
			return null;
		}
		T out = null;
		try {
			out = outClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new BeanMapperException("mapping fail, cause: " + e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new BeanMapperException("mapping fail, cause: " + e.getMessage());
		}
		map2Bean(in, out);
		return out;
	}

	/**
	 * 将Map转换成JavaBean
	 * 
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final static <T> void map2Bean(Map<String, Object> in, T out) throws BeanMapperException {
		try {
			if (null == in || null == out) {
				return;
			}
			/** @step 1: 获取所有字段域 */
			Class<T> outClass = (Class<T>) out.getClass();
			List<Field> allFields = ClassUtil.getFields(outClass);

			/** @step 2: 设值 */
			for (int i = 0; null != allFields && i < allFields.size(); i++) {
				Field field = allFields.get(i);// 字段域
				String fieldName = field.getName();// 字段名
				String mapKeyName = doGetMapKeyName(fieldName);// 获取字段名对应Map中的Key名
				Object mapValue = in.get(mapKeyName);// key对应的值
				Class<?> fieldTypeClass = field.getType();// 字段域类型
				String fieldTypeClassName = fieldTypeClass.getName();
				if (null == mapValue) {// 如果是空值，就没必要做映射了
					continue;
				}
				Class<?> mapValueClass = mapValue.getClass();
				field.setAccessible(true);
				if (mapValue instanceof Map && MAP_CLASS.isAssignableFrom(fieldTypeClass)) {
					/** @分支1: 如果都是Map类型，直接做映射 */
					field.set(out, mapValue);
				} else if (mapValue instanceof Map) {
					/** @分支2: 如果in中的key对应的value是Map类型 */
					if (isJavaNormalTypeField(field)) {
						throw new BeanMapperException("field type mismatch");
					}
					Object fieldValue = fieldTypeClass.newInstance();
					map2Bean((Map<String, Object>) mapValue, fieldValue);
					field.set(out, fieldValue);
				} else if (mapValue instanceof List) {
					/** @分支3: 如果in中的key对应的value是List类型 */
					if (!LIST_CLASS.isAssignableFrom(fieldTypeClass)) {
						throw new BeanMapperException("field type mismatch");
					}
					Type fieldGenericType = field.getGenericType();
					if (fieldGenericType instanceof ParameterizedType) {
						/** 获取List泛型 */
						Class<?> fieldGenericClass = null;
						ParameterizedType fieldParamType = (ParameterizedType) fieldGenericType;
						Type[] actualTypes = fieldParamType.getActualTypeArguments();
						for (Type aType : actualTypes) {
							if (aType instanceof Class) {
								fieldGenericClass = (Class<?>) aType;
							}
						}
						if (null == fieldGenericClass) {
							throw new BeanMapperException("field not use generic type");
						}
						
						// 判断List所使用的泛型是否跟mapValue中元素项类型一致
						List<?> srcList = (List<?>) mapValue;
						boolean isSameType = fieldGenericClass.isAssignableFrom(srcList.get(0).getClass());
						if (!isSameType && !MAP_CLASS.isAssignableFrom(srcList.get(0).getClass())) {
							throw new BeanMapperException("field type mismatch");
						}
						List<Object> desList = new ArrayList<Object>();
						for (Object mapItem : srcList) {
							if (isSameType) {// 一样的类型
								desList.add(mapItem);
							} else if (mapItem instanceof Map) {// 如果mapValue中元素项为Map类型，则进行转换
								Object fieldItemValue = fieldGenericClass.newInstance();
								map2Bean((Map) mapItem, fieldItemValue);
								desList.add(fieldItemValue);
							}
						}
						field.set(out, desList);
					} else {// 字段域使用List，必须使用泛型
						throw new BeanMapperException("field not use generic type");
					}
				} else if (mapValue.getClass().isArray()) {
					/** @分支4: 如果in中的key对应的value是数组类型 */
					field.set(out, mapValue);
					System.out.println("aaaaaaaa");
				} else {
					/** @分支5: 其他类型 */
					if (fieldTypeClass.isAssignableFrom(mapValueClass)) {
						field.set(out, mapValue);
					}  else if (BASE_TYPE_CHAR.equals(fieldTypeClassName)) {
						field.set(out, mapValue.toString().charAt(0));
					} if (BASE_TYPES.contains(fieldTypeClassName) || BASE_WRAPPER_TTPE.contains(fieldTypeClassName)) {
						Object fieldValue = convert(fieldTypeClassName, mapValue);
						if (null != fieldValue) {
							field.set(out, fieldValue);
						}
					} else if (STRING_CLASS_NAME.equals(fieldTypeClassName)) {
						field.set(out, String.valueOf(mapValue));
					}
				}
			}
		} catch (BeanMapperException e) {
			e.printStackTrace();
			throw e;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new BeanMapperException("mapping fail, cause: " + e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new BeanMapperException("mapping fail, cause: " + e.getMessage());
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new BeanMapperException("mapping fail, cause: " + e.getMessage());
		}
	}

	private static Object convert(String targetType, Object srcObject) {
		if (null == srcObject) {
			return null;
		}
		if (BASE_TYPE_BOOLEAN.equals(targetType) || BASE_WRAPPER_TTPE_BOOLEAN.equals(targetType)) {
			return Boolean.parseBoolean(String.valueOf(srcObject));
		} else if (BASE_TYPE_INT.equals(targetType) || BASE_WRAPPER_TTPE_INTEGER.equals(targetType)) {
			return Integer.parseInt(String.valueOf(srcObject));
		} else if (BASE_TYPE_LONG.equals(targetType) || BASE_WRAPPER_TTPE_LONG.equals(targetType)) {
			return Long.parseLong(String.valueOf(srcObject));
		} else if (BASE_TYPE_SHORT.equals(targetType) || BASE_WRAPPER_TTPE_SHORT.equals(targetType)) {
			return Short.parseShort(String.valueOf(srcObject));
		} else if (BASE_TYPE_FLOAT.equals(targetType) || BASE_WRAPPER_TTPE_FLOAT.equals(targetType)) {
			return Float.parseFloat(String.valueOf(srcObject));
		} else if (BASE_TYPE_DOUBLE.equals(targetType) || BASE_WRAPPER_TTPE_DOUBLE.equals(targetType)) {
			return Double.parseDouble(String.valueOf(srcObject));
		} else if (BASE_TYPE_BYTE.equals(targetType) || BASE_WRAPPER_TTPE_BYTE.equals(targetType)) {
			return Byte.parseByte(String.valueOf(srcObject));
		}
		return null;
	}

	/**
	 * 是否为Java普通类型字段
	 * 
	 * @param field
	 * @return
	 */
	private static boolean isJavaNormalTypeField(Field field) {
		Class<?> fieldType = field.getType();
		String fieldTypeName = fieldType.getName();
		if (BASE_TYPES.contains(fieldTypeName) // Java基本类型
				|| BASE_WRAPPER_TTPE.contains(fieldTypeName)// Java基本类型封装类型
				|| STRING_CLASS_NAME.equals(fieldTypeName) // String类型
				|| LIST_CLASS.isAssignableFrom(fieldType) // List类型
				|| fieldType.isArray()) { // 数组类型
			return true;
		}
		return false;
	}

	/**
	 * 根据字段域名获取对应Map中的KEY名称，默认首字母大写
	 * 
	 * 例如，JavaBean字段域名叫做name；那么，Map中的key名就是Name
	 * 
	 * @param fieldName
	 * @return
	 */
	private static String doGetMapKeyName(String fieldName) {
		return StringUtils.capitalize(fieldName);
	}
}
