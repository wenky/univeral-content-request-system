package com.cem.lweb.core;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.cem.contextmap.ContextMap;
import com.cem.contextmap.web.HttpRequestAttributes;
import com.cem.contextmap.web.HttpSessionAttributes;
import com.cem.contextmap.web.ServletConfigAttributes;
import com.cem.contextmap.web.ServletContextAttributes;

/**
 * lweb's context map created per-action (parts may change through the application)
 * 
 * precedence:
 * 
 * - scratchpad / action handler arguments / aka page scope in JSPs (gets flipped out per action handler and view handler as needed)
 * - request http parameters 
 * - request attributes / aka request scope in JSPs
 * - session attributes / aka session scope in JSPs
 * - servlet context attributes / aka application scope in JSPs
 * - action config
 * - globals config
 * - servlet config attributes
 * 
 * @author muellc4
 *
 */

public class ActionContextMap extends ContextMap
{
    public static ContextMap getActionContext(ServletContext sc, HttpServletRequest request, Map scratchpad, Map pluginconfig, Map webconfig, Map actionconfig)
    {
        ContextMap c = new ContextMap();
        c.addNamedContext(scratchpad, "scratchpad"); //?make this a special IContextItem object?
        c.addNamedContext(request.getParameterMap(), "httpparameters");
        c.addNamedContext(new HttpRequestAttributes(request), "request");
        c.addNamedContext(new HttpSessionAttributes(request.getSession(false)), "session");
        c.addNamedContext(new ServletContextAttributes(sc), "servletcontext");
        c.addNamedContext(pluginconfig, "pluginconfig");
        c.addNamedContext(actionconfig, "actionconfig");
        if (webconfig.containsKey("global defaults")) {
        	c.addNamedContext((Map)webconfig.get("global defaults"), "globals");
        }
        c.addNamedContext(new ServletConfigAttributes(sc), "servletconfig");
        return c;
    }

}
