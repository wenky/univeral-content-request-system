package lweb.core;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lcontext.Context;
import lcontext.web.HttpSessionAttributes;
import lcontext.web.ServletContextAttributes;
import lcontext.web.ServletContextInitParameters;

public class ActionContext extends Context
{
    public static Context getActionContext(ServletContext sc, HttpServletRequest request, Map scratchpad, Map processingcontext, Map pluginconfig, Map webconfig)
    {
        // scratchpad, pluginconfig, httpparams, context, httpsession, servletconfig attrs, servletconfig initparams
        Context c = new Context();
        c.addNamedContext(scratchpad, "scratchpad");
        c.addNamedContext(pluginconfig, "pluginconfig");
        c.addNamedContext(request.getParameterMap(), "httpparameters");
        c.addWritableNamedContext(processingcontext, "context");
        c.addNamedContext(webconfig, "config");
        c.addNamedContext(new HttpSessionAttributes(request.getSession(false)), "session");
        c.addNamedContext(new ServletContextAttributes(sc), "servletcontext");
        c.addNamedContext(new ServletContextInitParameters(sc), "servletconfig");
        if (webconfig.containsKey("global defaults")) {
        	c.addNamedContext((Map)webconfig.get("global defaults"), "globals");
        }
        return c;
    }

}
