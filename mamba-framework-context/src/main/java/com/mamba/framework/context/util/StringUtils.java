package com.mamba.framework.context.util;

import java.util.List;

public class StringUtils extends org.springframework.util.StringUtils {
	public static final String EMPTY = "";
	public static final String COMMA = ",";
	private static final int PAD_LIMIT = 8192;

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

	/**
	 * 字符串左补齐
	 * 当字符串长度不及size时，使用空格进行左补齐
	 * @param str
	 * @param size
	 * @return
	 */
	public static String leftPad(String str, int size) {
		return leftPad(str, size, ' ');
	}

	public static String leftPad(String str, int size, char padChar) {
		if (str == null) {
			return null;
		}
		int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (pads > PAD_LIMIT) {
			return leftPad(str, size, String.valueOf(padChar));
		}
		return padding(pads, padChar).concat(str);
	}

	public static String leftPad(String str, int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = " ";
		}
		int padLen = padStr.length();
		int strLen = str.length();
		int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1 && pads <= PAD_LIMIT) {
			return leftPad(str, size, padStr.charAt(0));
		}

		if (pads == padLen) {
			return padStr.concat(str);
		} else if (pads < padLen) {
			return padStr.substring(0, pads).concat(str);
		} else {
			char[] padding = new char[pads];
			char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return new String(padding).concat(str);
		}
	}

	private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
		if (repeat < 0) {
			throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
		}
		final char[] buf = new char[repeat];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = padChar;
		}
		return new String(buf);
	}

	public static String rightPad(String str, int size) {
		return rightPad(str, size, ' ');
	}

	public static String rightPad(String str, int size, char padChar) {
		if (str == null) {
			return null;
		}
		int pads = size - str.length();
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (pads > PAD_LIMIT) {
			return rightPad(str, size, String.valueOf(padChar));
		}
		return str.concat(padding(pads, padChar));
	}

	public static String rightPad(String str, int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = " ";
		}
		int padLen = padStr.length();
		int strLen = str.length();
		int pads = size - strLen;
		if (pads <= 0) {
			return str; // returns original String when possible
		}
		if (padLen == 1 && pads <= PAD_LIMIT) {
			return rightPad(str, size, padStr.charAt(0));
		}

		if (pads == padLen) {
			return str.concat(padStr);
		} else if (pads < padLen) {
			return str.concat(padStr.substring(0, pads));
		} else {
			char[] padding = new char[pads];
			char[] padChars = padStr.toCharArray();
			for (int i = 0; i < pads; i++) {
				padding[i] = padChars[i % padLen];
			}
			return str.concat(new String(padding));
		}
	}
}
