package com.daicy.panda.netty.servlet.impl;

import com.daicy.panda.netty.PandaServerBuilder;
import com.daicy.panda.netty.servlet.impl.filter.FilterDef;
import com.daicy.panda.netty.servlet.impl.filter.FilterRegistrationImpl;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.FileNameMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-11
 */
public class ServletContextImpl implements ServletContext {

    private static final Logger log = LoggerFactory
            .getLogger(ServletContextImpl.class);

    private static ServletContextImpl instance = new ServletContextImpl(new PandaContext());

    /**
     * The Context instance with which we are associated.
     */
    private final PandaContext context;

    private PandaServerBuilder pandaServerBuilder;

    private Map<String, Object> attributes = Maps.newHashMap();

    private Map<String, String> initParameters = Maps.newHashMap();

    private String servletContextName;

    private String contextPath = "";

    private final SevletFactory sevletFactory;

    private ServletContextImpl(PandaContext context) {
        this.context = context;
        instance = this;
        this.sevletFactory = new SevletFactory();
    }

    public static ServletContextImpl get() {
        return instance;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public int getMajorVersion() {
        return 2;
    }

    @Override
    public int getMinorVersion() {
        return 4;
    }


    @Override
    public ServletContext getContext(String uripath) {
        return this;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 4;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 2;
    }

    @Override
    public String getMimeType(String file) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(file);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return ServletContextImpl.class.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return ServletContextImpl.class.getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        return context.getServlet(name);
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(context.getServletMap().values());
    }

    @Override
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(context.getServletMap().keySet());
    }

    @Override
    public void log(String msg) {

    }

    @Override
    public void log(Exception exception, String msg) {

    }

    @Override
    public void log(String message, Throwable throwable) {

    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        initParameters.put(name, value);
        return true;
    }

    @Override
    public void setAttribute(String name, Object object) {
        this.attributes.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public String getServletContextName() {
        return servletContextName;
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null, null);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, null, servlet, null);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName,
                                                  Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName(), null, null);
    }


    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return sevletFactory.loadServlet(clazz);
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        Servlet servlet = context.getServlet(servletName);
        if (null == servlet) {
            return null;
        }
        ServletRegistration.Dynamic registration =
                new ServletRegistrationImpl(servlet, context);
        ServletSecurity annotation = servlet.getClass().getAnnotation(ServletSecurity.class);
        if (annotation != null) {
            registration.setServletSecurity(new ServletSecurityElement(annotation));
        }
        return registration;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        Map<String, Servlet> servletMap = context.getServletMap();
        if (MapUtils.isEmpty(servletMap)) {
            return null;
        }
        Map<String, ServletRegistration> result = Maps.newHashMap();
        for (Map.Entry<String, Servlet> servletEntry : servletMap.entrySet()) {
            Servlet servlet = servletEntry.getValue();
            ServletRegistration.Dynamic registration =
                    new ServletRegistrationImpl(servlet, context);
            ServletSecurity annotation = servlet.getClass().getAnnotation(ServletSecurity.class);
            if (annotation != null) {
                registration.setServletSecurity(new ServletSecurityElement(annotation));
            }
            result.put(servletEntry.getKey(), registration);
        }
        return result;
    }

    private ServletRegistration.Dynamic addServlet(String servletName, String servletClass,
                                                   Servlet servlet, Map<String, String> initParams) throws IllegalStateException {

        if (servletName == null || servletName.equals("")) {
            throw new IllegalArgumentException("applicationContext.invalidServletName" + servletName);
        }
        ServletSecurity annotation = null;
        if (null == servlet) {
            Class<?> clazz = null;
            try {
                clazz = ClassUtils.getClass(servletClass);
                servlet = sevletFactory.loadServlet(clazz);
                context.addServlet(servletName, servlet);
            } catch (Exception e) {
                throw new IllegalArgumentException("applicationContext.invalidServletClass" + servletClass);
            }
        }

        try {
            ServletConfigImpl config = new ServletConfigImpl(servletName);
            config.setServletContext(this);
            servlet.init(config);
        } catch (ServletException e) {
            log.error("servlet.init error", e);
        }
        if (initParams != null) {
            ServletConfigImpl servletConfig = (ServletConfigImpl) servlet.getServletConfig();
            for (Map.Entry<String, String> initParam : initParams.entrySet()) {
                servletConfig.setInitParameter(initParam.getKey(), initParam.getValue());
            }
        }
        context.addServlet(servletName, servlet);
        ServletRegistration.Dynamic registration =
                new ServletRegistrationImpl(servlet, context);
        annotation = servlet.getClass().getAnnotation(ServletSecurity.class);
        if (annotation != null) {
            registration.setServletSecurity(new ServletSecurityElement(annotation));
        }
        return registration;

    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return addFilter(filterName, className, null);
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, null, filter);
    }


    @Override
    public FilterRegistration.Dynamic addFilter(String filterName,
                                                Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName(), null);
    }

    private FilterRegistration.Dynamic addFilter(String filterName,
                                                 String filterClass, Filter filter) throws IllegalStateException {

        if (filterName == null || filterName.equals("")) {
            throw new IllegalArgumentException("applicationContext.invalidFilterName" + filterName);
        }

        FilterDef filterDef = context.findFilterDef(filterName);

        // Assume a 'complete' FilterRegistration is one that has a class and
        // a name
        if (filterDef == null) {
            filterDef = new FilterDef();
            filterDef.setFilterName(filterName);
            context.addFilterDef(filterDef);
        } else {
            if (filterDef.getFilterName() != null &&
                    filterDef.getFilterClass() != null) {
                return null;
            }
        }

        if (filter == null) {
            try {
                Class clazz = ClassUtils.getClass(filterClass);
                filter = createFilter(clazz);
            } catch (Exception e) {
                throw new IllegalArgumentException("applicationContext.invalidFilterClass" + filterClass);
            }
            filterDef.setFilter(filter);
            filterDef.setFilterClass(filterClass);
        } else {
            filterDef.setFilterClass(filter.getClass().getName());
            filterDef.setFilter(filter);
        }

        return new FilterRegistrationImpl(filterDef, context);
    }


    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        try {
            @SuppressWarnings("unchecked")
            T filter = (T) ConstructorUtils.invokeConstructor(c, null);
            return filter;
        } catch (InvocationTargetException e) {
            throw new ServletException(e);
        } catch (ReflectiveOperationException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        FilterDef filterDef = context.findFilterDef(filterName);
        if (filterDef == null) {
            return null;
        }
        return new FilterRegistrationImpl(filterDef, context);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        Map<String, FilterRegistrationImpl> result = new HashMap<>();

        FilterDef[] filterDefs = (FilterDef[]) context.getFilterDefMap().values().toArray();
        for (FilterDef filterDef : filterDefs) {
            result.put(filterDef.getFilterName(),
                    new FilterRegistrationImpl(filterDef, context));
        }

        return result;
    }


    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String className) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    public PandaContext getContext() {
        return context;
    }

    public PandaServerBuilder getPandaServerBuilder() {
        return pandaServerBuilder;
    }

    public void setPandaServerBuilder(PandaServerBuilder pandaServerBuilder) {
        this.pandaServerBuilder = pandaServerBuilder;
    }
}
