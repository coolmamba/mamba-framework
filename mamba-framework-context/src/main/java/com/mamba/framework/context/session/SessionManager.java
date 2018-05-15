package com.mamba.framework.context.session;

import com.mamba.framework.context.session.core.Operator;
import com.mamba.framework.context.session.core.Session;

public class SessionManager {
	private static ThreadLocal<Session> s_session = new ThreadLocal<Session>();

	public static void setSession(Session session) {
		s_session.set(session);
	}

	public static Session getSession() {
		return s_session.get();
	}

	public Operator getOperator() {
		Session session = getSession();
		return session == null ? null : session.getOperator();
	}
}
