package com.daicy.panda.netty.servlet.impl;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.util.*;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-12
 */
@Slf4j
public class ServletRegistrationImpl implements ServletRegistration.Dynamic {

    private final Servlet servlet;
    private final PandaContext context;
    private ServletSecurityElement constraint;

    public ServletRegistrationImpl(Servlet servlet,
                                   PandaContext context) {
        this.servlet = servlet;
        this.context = context;

    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {

    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {

    }

    @Override
    public void setRunAsRole(String roleName) {

    }

    protected boolean asyncSupported;

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.asyncSupported = isAsyncSupported;
    }

    private Collection<String> urlPatternMappings = new LinkedList<>();

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        ServletContextImpl context = ServletContextImpl.get();
        for (String urlPattern : urlPatterns) {
            context.addServletMapping(urlPattern, getName());
        }
        urlPatternMappings.addAll(Arrays.asList(urlPatterns));
        return new HashSet<>(urlPatternMappings);
    }

    @Override
    public Collection<String> getMappings() {
        return urlPatternMappings;
    }

    @Override
    public String getRunAsRole() {
        return null;
    }

    @Override
    public String getName() {
        return servlet.getServletConfig().getServletName();
    }

    @Override
    public String getClassName() {
        return servlet.getClass().getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        ServletConfigImpl servletConfig = (ServletConfigImpl) servlet.getServletConfig();
        return servletConfig.setInitParameter(name, value);
    }

    @Override
    public String getInitParameter(String name) {
        return servlet.getServletConfig().getInitParameter(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        if (null == initParameters) {
            return null;
        }
        Set<String> result = new HashSet<>();
        ServletConfigImpl servletConfig = (ServletConfigImpl) servlet.getServletConfig();
        for (String key : initParameters.keySet()) {
            boolean success = servletConfig.setInitParameter(key, initParameters.get(key));
            if (success) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public Map<String, String> getInitParameters() {
        ServletConfigImpl servletConfig = (ServletConfigImpl) servlet.getServletConfig();
        return servletConfig.getInitParameters();
    }
}
