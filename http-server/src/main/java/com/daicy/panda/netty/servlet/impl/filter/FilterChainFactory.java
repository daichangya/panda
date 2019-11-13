package com.daicy.panda.netty.servlet.impl.filter;

import com.daicy.panda.netty.servlet.impl.ServletContextImpl;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.netty.servlet.impl.filter
 * @date:19-11-12
 */
public class FilterChainFactory {


    /**
     * Construct a FilterChain implementation that will wrap the execution of
     * the specified servlet instance.
     *
     * @param request The servlet request we are processing
     * @param servlet The servlet instance to be wrapped
     *
     * @return The configured FilterChain instance or null if none is to be
     *         executed.
     */
    public static FilterChainImpl createFilterChain(HttpServletRequest request, Servlet servlet) {

        // If there is no servlet to execute, return null
        if (servlet == null)
            return null;

        // Create and initialize a filter chain object
        FilterChainImpl filterChain = new FilterChainImpl();

//        filterChain.setServlet(servlet);

        // Acquire the filter mappings for this Context
        List<FilterMap> filterMapList = ServletContextImpl.get().getContext().getFilterMapList();

        // If there are no filter mappings, we are done
        if (CollectionUtils.isEmpty(filterMapList))
            return filterChain;


        String requestPath = request.getRequestURI();

        String servletName = servlet.getServletConfig().getServletName();

        // Add the relevant path-mapped filters to this filter chain
        for (int i = 0; i < filterMapList.size(); i++) {
            FilterMap filterMap = filterMapList.get(i);
            if (!matchFiltersURL(filterMap,requestPath)) {
                continue;
            }
            FilterDef filterDef=  ServletContextImpl.get().getContext().findFilterDef(filterMap.getFilterName());
            if (filterDef == null) {
                // FIXME - log configuration problem
                continue;
            }
            filterChain.addFilter(filterDef.getFilter());
        }

        // Add filters that match on servlet name second
        for (int i = 0; i < filterMapList.size(); i++) {
            FilterMap filterMap = filterMapList.get(i);
            if (!matchFiltersServlet(filterMap,servletName)) {
                continue;
            }
            FilterDef filterDef=  ServletContextImpl.get().getContext().findFilterDef(filterMap.getFilterName());
            if (filterDef == null) {
                // FIXME - log configuration problem
                continue;
            }
            filterChain.addFilter(filterDef.getFilter());
        }

        // Return the completed filter chain
        return filterChain;
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Return <code>true</code> if the context-relative request path
     * matches the requirements of the specified filter mapping;
     * otherwise, return <code>false</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param requestPath Context-relative request path of this request
     */
    private static boolean matchFiltersURL(FilterMap filterMap, String requestPath) {

        // Check the specific "*" special URL pattern, which also matches
        // named dispatches
        if (filterMap.getMatchAllUrlPatterns())
            return true;

        if (requestPath == null)
            return false;

        // Match on context relative request path
        String[] testPaths = filterMap.getURLPatterns();

        for (int i = 0; i < testPaths.length; i++) {
            if (matchFiltersURL(testPaths[i], requestPath)) {
                return true;
            }
        }

        // No match
        return false;

    }


    /**
     * Return <code>true</code> if the context-relative request path
     * matches the requirements of the specified filter mapping;
     * otherwise, return <code>false</code>.
     *
     * @param testPath URL mapping being checked
     * @param requestPath Context-relative request path of this request
     */
    private static boolean matchFiltersURL(String testPath, String requestPath) {

        if (testPath == null)
            return false;

        // Case 1 - Exact Match
        if (testPath.equals(requestPath))
            return true;

        // Case 2 - Path Match ("/.../*")
        if (testPath.equals("/*"))
            return true;
        if (testPath.endsWith("/*")) {
            if (testPath.regionMatches(0, requestPath, 0,
                    testPath.length() - 2)) {
                if (requestPath.length() == (testPath.length() - 2)) {
                    return true;
                } else if ('/' == requestPath.charAt(testPath.length() - 2)) {
                    return true;
                }
            }
            return false;
        }

        // Case 3 - Extension Match
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash)
                    && (period != requestPath.length() - 1)
                    && ((requestPath.length() - period)
                    == (testPath.length() - 1))) {
                return testPath.regionMatches(2, requestPath, period + 1,
                        testPath.length() - 2);
            }
        }

        // Case 4 - "Default" Match
        return false; // NOTE - Not relevant for selecting filters

    }


    /**
     * Return <code>true</code> if the specified servlet name matches
     * the requirements of the specified filter mapping; otherwise
     * return <code>false</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param servletName Servlet name being checked
     */
    private static boolean matchFiltersServlet(FilterMap filterMap,
                                               String servletName) {

        if (servletName == null) {
            return false;
        }
        // Check the specific "*" special servlet name
        else if (filterMap.getMatchAllServletNames()) {
            return true;
        } else {
            String[] servletNames = filterMap.getServletNames();
            for (int i = 0; i < servletNames.length; i++) {
                if (servletName.equals(servletNames[i])) {
                    return true;
                }
            }
            return false;
        }

    }

}
