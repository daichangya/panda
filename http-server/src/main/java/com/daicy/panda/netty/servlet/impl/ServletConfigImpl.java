/*
 * Copyright 2013 by Maxim Kalina
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.daicy.panda.netty.servlet.impl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl
 * @date:19-11-11
 */
public class ServletConfigImpl implements ServletConfig {

    private ServletContext servletContext = null;

    private final String servletName;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    public ServletConfigImpl(String servletName) {
        this.servletName = servletName;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public boolean setInitParameter(String name, String value) {
        initParameters.put(name, value);
        return true;
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }
}
