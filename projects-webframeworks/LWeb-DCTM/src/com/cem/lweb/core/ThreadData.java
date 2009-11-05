package com.cem.lweb.core;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cem.contextmap.ContextMap;


// provide Threadlocal facility

public class ThreadData 
{
    private static ThreadLocal localrequest = new ThreadLocal();
    private static ThreadLocal localresponse = new ThreadLocal();
    private static ThreadLocal localconfig = new ThreadLocal();
    private static ThreadLocal localcontext = new ThreadLocal();
    private static ThreadLocal localactionctx = new ThreadLocal();
    
    public static void setHttpObjects(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, Map config, ContextMap actioncontext)
    {
        localrequest.set(req);
        localresponse.set(resp);
        localcontext.set(ctx);
        localconfig.set(config);
        localactionctx.set(actioncontext);
    }
    
    public static HttpServletRequest getHttpRequest()
    {
        return (HttpServletRequest)localrequest.get();
    }

    public static HttpServletResponse getHttpResponse()
    {
        return (HttpServletResponse)localresponse.get();
    }

    public static ServletContext getServletContext()
    {
        return (ServletContext)localcontext.get();
    }

    public static Map getConfiguration()
    {
        return (Map)localconfig.get();
    }

    public static ContextMap getActionContext()
    {
        return (ContextMap)localactionctx.get();
    }

    public static void setActionContext(ContextMap ctx)
    {
        localactionctx.set(ctx);
    }

    
}
