package com.liteserv.core;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liteserv.config.LSConfiguration;

// provide Threadlocal facility

public class LSRequestContext 
{
    private static ThreadLocal localrequest = new ThreadLocal();
    private static ThreadLocal localresponse = new ThreadLocal();
    private static ThreadLocal localconfig = new ThreadLocal();
    private static ThreadLocal localcontext = new ThreadLocal();
    
    public static void setHttpObjects(HttpServletRequest req, HttpServletResponse resp, ServletContext ctx, LSConfiguration config)
    {
        localrequest.set(req);
        localresponse.set(resp);
        localcontext.set(ctx);
        localconfig.set(config);
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

    public static LSConfiguration getLSConfiguration()
    {
        return (LSConfiguration)localconfig.get();
    }

}
