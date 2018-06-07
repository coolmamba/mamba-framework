package com.mamba.framework.context.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	public static final String YYYYMMDD = "yyyyMMdd";
	public static final String YYYYMM = "yyyyMM";

	/**
	 * 将时间格式化成字符串(格式：yyyyMMdd)
	 * 
	 * @param date
	 * @return
	 */
	public static final String format(Date date) {
		return format(date, null);
	}

	/**
	 * 将时间格式化为制定格式(format)的字符串
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static final String format(Date date, String format) {
		if (null == date) {
			return null;
		}
		if (StringUtils.isBlank(format)) {
			format = YYYYMMDD;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
}
