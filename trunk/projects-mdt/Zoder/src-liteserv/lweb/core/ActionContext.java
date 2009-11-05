package lweb.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lcontext.Context;
import lcontext.web.HttpSessionAttributes;
import lcontext.web.ServletContextAttributes;
import lcontext.web.ServletContextInitParameters;

public class ActionContext extends Context
{
    List contextlist = null;
    public ActionContext(ServletContext sc, HttpServletRequest request, Map scratchpad, Map processingcontext, Map pluginconfig, Map webconfig)
    {
        // scratchpad, pluginconfig, httpparams, context, httpsession, servletconfig attrs, servletconfig initparams
        contextlist = new ArrayList(7);
        contextlist.add(scratchpad);
        contextlist.add(pluginconfig);
        contextlist.add(request.getParameterMap());
        contextlist.add(processingcontext);
        contextlist.add(webconfig);
        contextlist.add(new HttpSessionAttributes(request.getSession(false)));
        contextlist.add(new ServletContextAttributes(sc));
        contextlist.add(new ServletContextInitParameters(sc));
        super.initContext(contextlist,3);
    }

    public List getContextList(){
        return contextlist;
    }

    public Map getProcessingContextMap() {
        return (Map)contextlist.get(3);
    }
    
    public void setActionContext(Map actioncontext) {
        contextlist.set(3,actioncontext);
    }
    public void setPluginConfig(Map pluginconfig) {
        contextlist.set(1, pluginconfig);
    }
    public void setArgMap(Map argmap) {
        contextlist.set(0, argmap);
    }
    
}
