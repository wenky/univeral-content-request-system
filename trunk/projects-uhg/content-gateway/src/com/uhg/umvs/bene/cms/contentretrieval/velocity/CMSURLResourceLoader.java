package com.uhg.umvs.bene.cms.contentretrieval.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;


public class CMSURLResourceLoader extends ResourceLoader
{
    
    private String roots[];
    
    protected HashMap templateRoots;

    private int timeout;
    public int getTimeout() { return timeout; }

    private Method timeoutMethods[];
    
    protected ExtendedProperties configProps;


    public CMSURLResourceLoader()
    {
        roots = null;
        templateRoots = null;
        timeout = -1;
    }

    public void init(ExtendedProperties configuration)
    {
        configProps = configuration;
        
        log.trace("URLResourceLoader : initialization starting.");
        roots = configuration.getStringArray("root");
        if(log.isDebugEnabled())
        {
            for(int i = 0; i < roots.length; i++)
                log.debug("URLResourceLoader : adding root '" + roots[i] + "'");

        }
        timeout = configuration.getInt("timeout", -1);
        if(timeout > 0)
            try
            {
                Class types[] = {
                    Integer.TYPE
                };
                Method conn = (java.net.URLConnection.class).getMethod("setConnectTimeout", types);
                Method read = (java.net.URLConnection.class).getMethod("setReadTimeout", types);
                timeoutMethods = (new Method[] {
                    conn, read
                });
                log.debug("URLResourceLoader : timeout set to " + timeout);
            }
            catch(NoSuchMethodException nsme)
            {
                log.debug("URLResourceLoader : Java 1.5+ is required to customize timeout!", nsme);
                timeout = -1;
            }
        templateRoots = new HashMap();
        log.trace("URLResourceLoader : initialization complete.");
    }
    
    protected String constructURL(String root, String item)
    {
        // default impl: return root + item
        // implement locale-specifics via some mechanism to look up Locale (threadlocal, injected locale lookup service, etc)
        return root + item;
    }

    public synchronized InputStream getResourceStream(String name)
        throws ResourceNotFoundException
    {
        if(StringUtils.isEmpty(name))
            throw new ResourceNotFoundException("URLResourceLoader : No template name provided");
        InputStream inputStream = null;
        Exception exception = null;
        for(int i = 0; i < roots.length;)
            try
            {
                String urlstring = constructURL(roots[i],name);
                URL u = new URL(urlstring);
                URLConnection conn = u.openConnection();
                tryToSetTimeout(conn);
                inputStream = conn.getInputStream();
                if(inputStream == null)
                    continue;
                if(log.isDebugEnabled())
                    log.debug("URLResourceLoader: Found '" + name + "' at '" + roots[i] + "'");
                templateRoots.put(name, roots[i]);
                break;
            }
            catch(IOException ioe)
            {
                if(log.isDebugEnabled())
                    log.debug("URLResourceLoader: Exception when looking for '" + name + "' at '" + roots[i] + "'", ioe);
                if(exception == null)
                    exception = ioe;
                i++;
            }

        if(inputStream == null)
        {
            String msg;
            if(exception == null)
                msg = "URLResourceLoader : Resource '" + name + "' not found.";
            else
                msg = exception.getMessage();
            throw new ResourceNotFoundException(msg);
        } else
        {
            return inputStream;
        }
    }

    public boolean isSourceModified(Resource resource)
    {
        long fileLastModified = getLastModified(resource);
        return fileLastModified == 0L || fileLastModified != resource.getLastModified();
    }

    public long getLastModified(Resource resource)
    {
        String name = resource.getName();
        String root = (String)templateRoots.get(name);
        try
        {
            URL u = new URL(root + name);
            URLConnection conn = u.openConnection();
            tryToSetTimeout(conn);
            return conn.getLastModified();
        }
        catch(IOException ioe)
        {
            String msg = "URLResourceLoader: '" + name + "' is no longer reachable at '" + root + "'";
            log.error(msg, ioe);
            throw new ResourceNotFoundException(msg, ioe);
        }
    }


    private void tryToSetTimeout(URLConnection conn)
    {
        if(timeout > 0)
        {
            Object arg[] = {
                new Integer(timeout)
            };
            try
            {
                timeoutMethods[0].invoke(conn, arg);
                timeoutMethods[1].invoke(conn, arg);
            }
            catch(Exception e)
            {
                String msg = "Unexpected exception while setting connection timeout for " + conn;
                log.error(msg, e);
                throw new VelocityException(msg, e);
            }
        }
    }

}
