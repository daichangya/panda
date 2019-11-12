package com.daicy.panda.netty.servlet.impl.filter;

import com.google.common.collect.Maps;

import javax.servlet.Servlet;
import java.util.Map;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-12
 */
public class PandaContext {

    private final Map<String, FilterMap> filterMapsAfter = Maps.newHashMap();

    private final Map<String, FilterMap> filterMapsBefore = Maps.newHashMap();

    private final Map<String, FilterDef> filterMaps = Maps.newHashMap();

    private final Map<String, Servlet> servletMap = Maps.newHashMap();


    public FilterDef findFilterDef(String name) {
        return filterMaps.get(name);
    }


    public void addFilterDef(FilterDef filterDef) {
        filterMaps.put(filterDef.getFilterName(), filterDef);
    }

    public Map<String, FilterDef> getFilterMaps() {
        return filterMaps;
    }

    public void addServlet(String name,Servlet servlet) {
        servletMap.put(name,servlet);
    }

    public Servlet getServlet(String name) {
       return servletMap.get(name);
    }

    public Map<String, Servlet> getServletMap() {
        return servletMap;
    }
}
