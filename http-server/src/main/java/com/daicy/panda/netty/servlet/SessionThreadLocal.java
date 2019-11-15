package com.daicy.panda.netty.servlet;


import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import com.daicy.panda.netty.servlet.impl.SessionImpl;

public class SessionThreadLocal {

    public static final ThreadLocal<SessionImpl> sessionThreadLocal = new ThreadLocal<SessionImpl>();

    public static void set(SessionImpl session) {
        sessionThreadLocal.set(session);
    }

    public static void unset() {
        sessionThreadLocal.remove();
    }

    public static SessionImpl get() {
        SessionImpl session = sessionThreadLocal.get();
        if (session != null)
            session.touch();
        return session;
    }

    public static SessionImpl getOrCreate() {
        if (SessionThreadLocal.get() == null) {

            SessionImpl newSession = ServletContextImpl.get().getContext().createSession();
            newSession.setMaxInactiveInterval(ServletContextImpl.get().getPandaServerBuilder().getSessionTimeout());
            sessionThreadLocal.set(newSession);
        }
        return get();
    }

}
