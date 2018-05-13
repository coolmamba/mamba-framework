package com.mamba.framework.context.util;

import java.util.List;

public class StringUtils extends org.springframework.util.StringUtils {
	public static final String EMPTY = "";
	public static final String COMMA = ",";

	public static String replaceOnce(String text, String repl, String with) {
		return replace(text, repl, with, 1);
	}

	public static String replace(String text, String repl, String with, int max) {
		if (text == null || Assert.isBlank(repl) || with == null || max == 0) {
			return text;
		}

		StringBuffer buf = new StringBuffer(text.length());
		int start = 0, end = 0;
		while ((end = text.indexOf(repl, start)) != -1) {
			buf.append(text.substring(start, end)).append(with);
			start = end + repl.length();

			if (--max == 0) {
				break;
			}
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	public static boolean isBlank(String str) {
		return (null == str || "".equals(str.trim()));
	}

	public static boolean isNotBlank(String str) {
		return (null != str && !"".equals(str.trim()));
	}

	/**
	 * 使用英文逗号(,)将数组连接成字符串
	 * 
	 * @param list
	 * @return
	 */
	public static String join(List<?> list) {
		return join(list, COMMA);
	}

	/**
	 * 使用连接符将数组连接成字符串
	 * 
	 * @param list
	 * @param joinStr
	 * @return
	 */
	public static String join(List<?> list, String joinStr) {
		if (null == list || list.size() == 0) {
			return EMPTY;
		}
		if (null == joinStr) {
			joinStr = COMMA;
		}
		StringBuilder sb = new StringBuilder();
		boolean hasItem = false;
		for (Object item : list) {
			if (null == item) {
				continue;
			}
			if (!hasItem) {
				hasItem = true;
			}
			sb.append(item.toString()).append(joinStr);
		}

		if (hasItem) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return sb.toString();
		}
	}
}
