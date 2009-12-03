package com.uhg.umvs.bene.cms.contentretrieval.server.contentsource;

// grab content from an http server or some other URL-connectable resource.
// - useful for passthrus, as was the case with the Jackrabbit JCR jars conflicting with the jBOSS CMS jars
//   so this was used to passthru to another webapp specifically dedicated to accessing the jBOSS CMS

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hsqldb.lib.StringUtil;

import com.uhg.umvs.bene.cms.contentretrieval.server.interfaces.ContentSource;

public class LocalHTTPSource implements ContentSource
{
    // simple pass-thru (for example, if there are two different versions of jackrabbits, we can't access them in the same jvm
    // - assumes local "hard relative"
    String localWebappName = null;
    public void setLocalWebappName(String localWebappName) {this.localWebappName = localWebappName;}    
    
    // this is much more difficult for such a generic source than for other sources we have 
    // more fine-grained control of. In general, for high performance we generally want to 
    // be able to check if it exists without retrieving it. However, there isn't a standard "EXISTS"
    // HTTP verb, only GET POST PUT DELETE. 
    
    // if knowledge of the source system is known, it is desireable to implement some means of 
    // communicating "EXISTS". perhaps set up a separate server or servlet or webapp that does
    // existence checking and communication, or override this implementation with a different
    // URL construction means that the destination source can distinguish between a content request
    // URL and a content exists URL
    
    // default is to simply check if anything is returned when the request is forwarded.    
    public boolean hasContent(String contentItem, HttpServletRequest request) 
    {
        String origurl = request.getRequestURL().toString();
        //String appname = request.getServletPath();
        //String requri = request.getRequestURI();
        // protocol "://" server ":" port (contextpath servletpath pathinfo) or (requestURI) "?" querystring
        String querystring = request.getQueryString();        
        String webappname = request.getContextPath();
        String newurl = origurl.replace(webappname, localWebappName);
        if (!StringUtils.isEmpty(querystring)) { 
            newurl += '?' + querystring;
        }
        try {
            URL theurl = new URL(newurl); 
            URLConnection urlc = null;
            try {
                urlc = theurl.openConnection();
            } catch (MalformedURLException e) {     
                throw new RuntimeException("LocalHTTPSource: bad content request url "+newurl,e);
            }

            // why am I using a buffered reader here? not applicable for binary...
            BufferedReader in = new BufferedReader(new InputStreamReader(theurl.openStream()));             
            String response = IOUtils.toString(in);
            in.close();
            if (StringUtil.isEmpty(response)) {
                return false;                
            }
            return true;
                        
        } catch (IOException ioe) {
            throw new RuntimeException("LocalHTTPSource: IO Error "+newurl,ioe);
        }
        
    }

    public void getContent(String contentItem, HttpServletRequest request, HttpServletResponse resp)
    {
        // construct new url
        String origurl = request.getRequestURL().toString();
        //String appname = request.getServletPath();
        //String requri = request.getRequestURI();
        // protocol "://" server ":" port (contextpath servletpath pathinfo) or (requestURI) "?" querystring
        String querystring = request.getQueryString();        
        String webappname = request.getContextPath();
        String newurl = origurl.replace(webappname, localWebappName);
        if (!StringUtils.isEmpty(querystring)) { 
            newurl += '?' + querystring;
        }
        
        try {
            URL theurl = new URL(newurl); 
            URLConnection urlc = null;
            try {
                urlc = theurl.openConnection();
            } catch (MalformedURLException e) {     
                throw new RuntimeException("LocalHTTPSource: bad content request url "+newurl,e);
            }
            String contenttype = urlc.getContentType();
            if (!StringUtil.isEmpty(contenttype)) {
                resp.setContentType(contenttype);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(theurl.openStream()));             
            IOUtils.copy(in, resp.getOutputStream());
            resp.getOutputStream().flush();   
            in.close();
                        
        } catch (IOException ioe) {
            throw new RuntimeException("LocalHTTPSource: IO Error "+newurl,ioe);
        }
    }

}
