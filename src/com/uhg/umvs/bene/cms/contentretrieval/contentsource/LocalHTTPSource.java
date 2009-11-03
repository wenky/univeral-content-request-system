package com.uhg.umvs.bene.cms.contentretrieval.contentsource;



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

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;

public class LocalHTTPSource implements ContentSource
{
    // simple pass-thru (for example, if there are two different versions of jackrabbits, we can't access them in the same jvm
    // - assumes local "hard relative"
    String localWebappName = null;
    public void setLocalWebappName(String localWebappName) {this.localWebappName = localWebappName;}

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
