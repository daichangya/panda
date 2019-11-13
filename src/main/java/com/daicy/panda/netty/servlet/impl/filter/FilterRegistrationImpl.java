package com.daicy.panda.netty.servlet.impl.filter;

import com.daicy.panda.netty.servlet.impl.PandaContext;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.*;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-12
 */
public class FilterRegistrationImpl implements FilterRegistration.Dynamic {

    /**
     * The Context instance with which we are associated.
     */
    private final PandaContext context;

    private final FilterDef filterDef;

    public FilterRegistrationImpl(FilterDef filterDef, PandaContext context) {
        this.context = context;
        this.filterDef = filterDef;
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        FilterMap filterMap = new FilterMap();

        filterMap.setFilterName(filterDef.getFilterName());

        if (dispatcherTypes != null) {
            for (DispatcherType dispatcherType : dispatcherTypes) {
                filterMap.setDispatcher(dispatcherType.name());
            }
        }

        if (servletNames != null) {
            for (String servletName : servletNames) {
                filterMap.addServletName(servletName);
            }

            context.addFilterMap(filterMap);
//            if (isMatchAfter) {
//                context.addFilterMap(filterMap);
//            } else {
//                context.addFilterMapBefore(filterMap);
//            }
        }
        // else error?
    }

    @Override
    public Collection<String> getServletNameMappings() {
        Collection<String> result = new HashSet<>();

        List<FilterMap> filterMaps = context.getFilterMapList();

        for (FilterMap filterMap : filterMaps) {
            if (filterMap.getFilterName().equals(filterDef.getFilterName())) {
                for (String servletName : filterMap.getServletNames()) {
                    result.add(servletName);
                }
            }
        }
        return result;    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {

        FilterMap filterMap = new FilterMap();

        filterMap.setFilterName(filterDef.getFilterName());

        if (dispatcherTypes != null) {
            for (DispatcherType dispatcherType : dispatcherTypes) {
                filterMap.setDispatcher(dispatcherType.name());
            }
        }

        if (urlPatterns != null) {
            // % decoded (if necessary) using UTF-8
            for (String urlPattern : urlPatterns) {
                filterMap.addURLPattern(urlPattern);
            }
            context.addFilterMap(filterMap);
//            if (isMatchAfter) {
//                context.addFilterMap(filterMap);
//            } else {
//                context.addFilterMapBefore(filterMap);
//            }
        }
        // else error?
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        Collection<String> result = new HashSet<>();
        List<FilterMap> filterMaps = context.getFilterMapList();
        for (FilterMap filterMap : filterMaps) {
            if (filterMap.getFilterName().equals(filterDef.getFilterName())) {
                for (String urlPattern : filterMap.getURLPatterns()) {
                    result.add(urlPattern);
                }
            }
        }
        return result;
    }


    @Override
    public String getClassName() {
        return filterDef.getFilterClass();
    }

    @Override
    public String getInitParameter(String name) {
        return filterDef.getParameterMap().get(name);
    }

    @Override
    public Map<String, String> getInitParameters() {
        Map<String, String> result = new HashMap<>();
        result.putAll(filterDef.getParameterMap());
//        result.setLocked(true);
        return result;
    }

    @Override
    public String getName() {
        return filterDef.getFilterName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("applicationFilterRegistration.nullInitParam" +
                    name + value);
        }
        if (getInitParameter(name) != null) {
            return false;
        }

        filterDef.addInitParameter(name, value);

        return true;
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {

        Set<String> conflicts = new HashSet<>();

        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("applicationFilterRegistration.nullInitParams" +
                        entry.getKey() + entry.getValue());
            }
            if (getInitParameter(entry.getKey()) != null) {
                conflicts.add(entry.getKey());
            }
        }

        // Have to add in a separate loop since spec requires no updates at all
        // if there is an issue
        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            setInitParameter(entry.getKey(), entry.getValue());
        }

        return conflicts;
    }

    @Override
    public void setAsyncSupported(boolean asyncSupported) {
        filterDef.setAsyncSupported(Boolean.valueOf(asyncSupported).toString());
    }
}
