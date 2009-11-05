package com.cem.lweb.loadresource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cem.base.EEx;
import com.cem.lweb.core.ThreadData;
import com.cem.lweb.loadresource.ReplacementHttpServletResponse;



/** 
 *  The purpose of this class is to provide features and management of the process 
 *  of loading files or urls for processing, without adding too much bookkeeping.
 *  
 *  One of the nice things about MVC style web frameworks is the action abstraction,
 *  which allows a certain flexibility to refactorings. By applying resource designations
 *  to url parameters which abstract the actual resource location and retrieval means,
 *  we provide more flexiblity to deployment, and can likely parameterize actions so they
 *  are more reusable (less action definitions/XML). 
 *  
 *  TODO: 
 *  - caching
 *  - ?access restrictions?
 *  
 * @author muellc4
 *
 */

public class ResourceLoader 
{
    public static String loadResource(String local, String url, String file, String keyedresource)
    {
        if (local != null && !"".equals(local.trim())) {
            ServletContext ctx = ThreadData.getServletContext();
            return loadWebResource(ctx,local);
        } else if (url != null && !"".equals(url.trim())) {
            return loadUrl(url);
        } else if (file != null && !"".equals(file.trim())) {
            return loadFile(file);
        } else if (keyedresource != null && !"".equals(keyedresource.trim())) {
            return loadKeyedResource(keyedresource);
        }
        return null; // ? throw exception ? 
    }
    

    public static String loadUrl(String uri)
    {        
        try {
            URL httpUrl = new URL( uri );
            String urlcontents = loadUrl(httpUrl);
            return urlcontents;
        } catch (IOException ioe) {
            throw EEx.create("RscMgr-IOE","unable to retrieve URL resource %s",uri,ioe);                    
        } 
    }
    
    public static String loadUrl(URL httpurl)
    {
        try {
            InputStream is = httpurl.openStream();
            String contents = convertInputStreamToString(is);
            return contents;
        } catch (IOException ioe) {
            throw EEx.create("RscMgr-IOE","unable to retrieve URL resource %s",httpurl.toString(),ioe);                    
        } 
        
    }
    
    public static String callActiveUrl(ServletContext ctx, HttpServletRequest request,HttpServletResponse response, String uri) 
    {
        try {
            // do an explicit URL lookup - as in, may not be a local resource...
            ReplacementHttpServletResponse wrapperresponse = new ReplacementHttpServletResponse(response);
            RequestDispatcher dispatcher = ctx.getRequestDispatcher(uri);
            dispatcher.include(request, wrapperresponse);
            String responsediv = wrapperresponse.toString();
            return responsediv;
        } catch (IOException ioe) {
            throw EEx.create("RscMgr-IOE","unable to execute active servlet container resource %s",uri,ioe);                                
        } catch (ServletException se) {
            throw EEx.create("RscMgr-SE","unable to execute active servlet container resource %s",uri,se);                                            
        }
    }

    // load a resource visible to the current Servlet Context, should be of form "/resourcename" as a relative path to the servlet's context path
    public static String loadWebResource(ServletContext ctx, String uri)
    {
        try {
            // do an explicit URL lookup - as in, may not be a local resource...
            URL url = ctx.getResource(uri);
            String contents = loadUrl(url.toExternalForm());
            return contents;
        } catch (IOException ioe) {
            throw EEx.create("RscMgr-IOE","unable to retrieve servlet container resource %s",uri,ioe);                                
        }
    }
    
    // load a file from the file system (generally, such explicit file system path references should be discouraged...)
    public static String loadFile(String filepath)
    {
        try { 
            InputStream filecontentstream = new FileInputStream(filepath);
            String contents = convertInputStreamToString(filecontentstream);
            return contents;
        } catch (FileNotFoundException fnfe) {
            throw EEx.create("RscMgr-FNF","Could not find file %s",filepath,fnfe);                                
        }
    }

    public static String loadKeyedResource(String resource)
    {
        Map config = ThreadData.getConfiguration();
        Map resources = (Map)config.get("resources");
        
        Map rsc = (Map)resources.get(resource);

        
        if ("url".equalsIgnoreCase((String)rsc.get("type"))) { // ok, this one is kinda useless
            // do an explicit URL lookup - as in, may not be a local resource...
            String uri = (String)rsc.get("location");
            try {
                URL httpUrl = new URL( uri );
                InputStream is = httpUrl.openStream();
                String contents = convertInputStreamToString(is);
                return contents;
            } catch (IOException ioe) {
                throw EEx.create("RscMgr-IOE","unable to retrieve URL resource %s",uri,ioe);                    
            } 
        } else if ("file".equalsIgnoreCase((String)rsc.get("type"))) {
            // default non-subclasses LSResource is a file relative to webapp root via ServletContext
            String uri = (String)rsc.get("location");
            ServletContext ctx = ThreadData.getServletContext();
            InputStream is = ctx.getResourceAsStream(uri);
            String contents = convertInputStreamToString(is);
            return contents;
        }
        return "resource not found";
    }
    
    public static String convertInputStreamToString(InputStream is)
    {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
        StringBuffer buf = new StringBuffer();
        int nextChar = 0;
        try { 
            while ((nextChar = rdr.read()) != -1) {
                buf.append((char)nextChar);
            }
        } catch (IOException ioe) {
            throw EEx.create("RscMgr-IOE","unable to load resource",ioe);
        }
        return buf.toString();
        
    }

}
