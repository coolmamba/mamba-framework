package com.mamba.framework.context.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import com.mamba.framework.context.exception.BusinessException;

public class ExceptionUtil {
	public static void throwBusinessException(String key, Object... args) throws BusinessException {
		throw new BusinessException(key, args);
	}

	public static void throwBusinessException(String key) throws BusinessException {
		throw new BusinessException(key);
	}

	/**
	 * 异常解包
	 * @param wrapped
	 * @return
	 */
	public static Throwable unwrapThrowable(Throwable wrapped) {
		Throwable unwrapped = wrapped;
		while (true) {
			if (unwrapped instanceof InvocationTargetException) {
				unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
			} else if (unwrapped instanceof UndeclaredThrowableException) {
				unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
			} else {
				return unwrapped;
			}
		}
	}
}
