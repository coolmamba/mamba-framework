package com.mamba.framework.context.session;

import java.util.Date;

import com.mamba.framework.context.session.core.Operator;
import com.mamba.framework.context.session.core.Session;

public class SessionManager {
	private static ThreadLocal<Session> s_session = new ThreadLocal<Session>();

	private static boolean systemDateIsInit = false;

	private static long hostWithDbTimestampDifferenceValue = 0;

	public static void setSession(Session session) {
		s_session.set(session);
	}

	public static Session getSession() {
		return s_session.get();
	}

	public static Operator getOperator() {
		Session session = getSession();
		return session == null ? null : session.getOperator();
	}

	public static void setHostWithDbTimestampDifferenceValue(long value) throws Exception {
		if (systemDateIsInit) {
			throw new Exception("<<<<初始化系统时间已经成功，不支持再次初始化>>>>");
		}
		hostWithDbTimestampDifferenceValue = value;
		systemDateIsInit = true;
	}

	public static Date now() {
		return new Date(System.currentTimeMillis() - hostWithDbTimestampDifferenceValue);
	}

	public static java.sql.Date getSqlDateNow() {
		return new java.sql.Date(System.currentTimeMillis() - hostWithDbTimestampDifferenceValue);
	}
}
