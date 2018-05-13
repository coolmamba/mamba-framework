package com.mamba.framework.context.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Assert {
	public static <T> boolean isEmpty(T[] args) {
		return (null == args || args.length == 0);
	}

	public static <T> boolean isNotEmpty(T[] args) {
		return !isEmpty(args);
	}

	public static <T> boolean isEmpty(List<T> list) {
		return (null == list || list.size() == 0);
	}

	public static <T> boolean isNotEmpty(List<T> list) {
		return !isEmpty(list);
	}

	public static <K, V> boolean isEmpty(Map<K, V> map) {
		return (null == map || map.isEmpty());
	}

	public static <K, V> boolean isNotEmpty(Map<K, V> map) {
		return !isEmpty(map);
	}

	public static <T> boolean isEmpty(Set<T> set) {
		if (null == set || set.isEmpty()) {
			return true;
		}
		return false;
	}

	public static <T> boolean isNotEmpty(Set<T> set) {
		return !isEmpty(set);
	}

	public static boolean isBlank(String str) {
		return StringUtils.isBlank(str);
	}

	public static boolean isNotBlank(String str) {
		return StringUtils.isNotBlank(str);
	}
}
