package com.mamba.framework.context.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;

public class BeanUtils extends org.springframework.beans.BeanUtils {
	public static <S, T> List<T> copyProperties(List<S> source, Class<T> targetListItemClass) throws BeansException {
		if (Assert.isEmpty(source) || null == targetListItemClass) {
			return null;
		}

		List<T> target = new ArrayList<T>();
		for (S s : source) {
			try {
				T t = targetListItemClass.newInstance();
				copyProperties(s, t);
				target.add(t);
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new BeanCreationException("不能实例化目标对象，原因：" + e.getMessage());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new BeanCreationException("不能实例化目标对象，原因：" + e.getMessage());
			}
		}
		return target;
	}
}
