package com.daicy.panda.netty.servlet.impl;

import com.daicy.panda.netty.servlet.impl.filter.FilterDef;
import com.daicy.panda.netty.servlet.impl.filter.FilterMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Servlet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-12
 */
@Slf4j
public class PandaContext {

    public final ConcurrentHashMap<String, SessionImpl> sessions = new ConcurrentHashMap<String, SessionImpl>();

    private final List<FilterMap> filterMapList = Lists.newArrayList();

    private final Map<String, FilterDef> filterDefMap = Maps.newHashMap();

    private final Map<String, Servlet> servletMap = Maps.newHashMap();


    public FilterDef findFilterDef(String name) {
        return filterDefMap.get(name);
    }


    public void addFilterDef(FilterDef filterDef) {
        filterDefMap.put(filterDef.getFilterName(), filterDef);
    }

    public Map<String, FilterDef> getFilterDefMap() {
        return filterDefMap;
    }

    public void addServlet(String name, Servlet servlet) {
        servletMap.put(name, servlet);
    }

    public Servlet getServlet(String name) {
        return servletMap.get(name);
    }

    public Map<String, Servlet> getServletMap() {
        return servletMap;
    }

    public void addFilterMap(FilterMap filterMap) {
        filterMapList.add(filterMap);
    }

    public List<FilterMap> getFilterMapList() {
        return filterMapList;
    }


    public SessionImpl createSession(String requestedSessionId) {
        String sessionId = requestedSessionId;
        if(StringUtils.isEmpty(sessionId)){
            sessionId = this.generateNewSessionId();
        }
        log.debug("Creating new session with id {}", sessionId);

        SessionImpl session = new SessionImpl(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    public void destroySession(String sessionId) {
        log.debug("Destroying session with id {}", sessionId);
        sessions.remove(sessionId);
    }

    public SessionImpl changeSessionId(SessionImpl session) {
        log.debug("changeSessionId session with id {}", session.getId());
        String sessionId = this.generateNewSessionId();
        session.setId(sessionId);
        sessions.remove(sessionId);
        sessions.put(sessionId,session);
        return session;
    }

    public SessionImpl findSession(String sessionId) {
        if (sessionId == null)
            return null;

        return sessions.get(sessionId);
    }

    protected String generateNewSessionId() {
        return UUID.randomUUID().toString();
    }

    public void destroyInactiveSessions() {
        for (Map.Entry<String, SessionImpl> entry : sessions.entrySet()) {
            SessionImpl session = entry.getValue();
            if (session.getMaxInactiveInterval() < 0)
                continue;

            long currentMillis = System.currentTimeMillis();

            if (currentMillis - session.getLastAccessedTime() > session
                    .getMaxInactiveInterval() * 1000) {

                destroySession(entry.getKey());
            }
        }
    }

}
