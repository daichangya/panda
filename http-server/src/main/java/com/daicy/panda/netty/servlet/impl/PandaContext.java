package com.daicy.panda.netty.servlet.impl;

import com.daicy.panda.netty.servlet.impl.filter.FilterDef;
import com.daicy.panda.netty.servlet.impl.filter.FilterMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.servlet.Servlet;
import java.util.List;
import java.util.Map;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-12
 */
public class PandaContext {

    private final List<FilterMap> filterMapList= Lists.newArrayList();

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

    public void addServlet(String name,Servlet servlet) {
        servletMap.put(name,servlet);
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
}
