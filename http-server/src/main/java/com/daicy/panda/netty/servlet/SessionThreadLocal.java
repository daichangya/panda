package com.daicy.panda.netty.servlet;


import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import com.daicy.panda.netty.servlet.impl.SessionImpl;
import org.apache.commons.lang3.StringUtils;

public class SessionThreadLocal {

    public static final ThreadLocal<SessionImpl> sessionThreadLocal = new ThreadLocal<SessionImpl>();

    public static void set(SessionImpl session) {
        sessionThreadLocal.set(session);
    }

    public static void unset() {
        sessionThreadLocal.remove();
    }

    public static SessionImpl get(String requestedSessionId) {
        SessionImpl session = sessionThreadLocal.get();
        if (session != null) {
            session.touch();
        } else if (StringUtils.isNotEmpty(requestedSessionId)) {
            session = ServletContextImpl.get().getContext().findSession(requestedSessionId);
        }
        return session;
    }

    public static SessionImpl getOrCreate(String requestedSessionId) {
        SessionImpl newSession = SessionThreadLocal.get(requestedSessionId);
        if (newSession == null) {
            newSession = ServletContextImpl.get().getContext().createSession(requestedSessionId);
            newSession.setMaxInactiveInterval(ServletContextImpl.get().getPandaServerBuilder().getSessionTimeout());
            sessionThreadLocal.set(newSession);
        }
        return newSession;
    }
}
