package com.uhg.umvs.bene.cms.contentretrieval.requesthandler;

import hidden.org.codehaus.plexus.util.IOUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.uhg.umvs.bene.cms.contentretrieval.interfaces.ContentRequestHandler;


// intended to serve as the error or default handler, but can also serve as a mime-specific hardcoded handler, or perhaps for testing

public class ErrorDefaultHandler implements ContentRequestHandler
{
    Map<String,String> m_mimeMappings = null; 
    public void setMimeMappings(Map<String, String> mMimeMappings){m_mimeMappings = mMimeMappings;}
    
    String m_defaultMessage = "";
    public void setDefaultMessage(String mDefaultMessage){m_defaultMessage = mDefaultMessage;}

    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        if (m_mimeMappings != null) {
            // determine if there is extra path info
            String extrapath = req.getPathInfo();
            if (!StringUtils.isEmpty(extrapath)) {
                for (String extension : m_mimeMappings.keySet()) {
                    if (extrapath.endsWith(extension)) {
                        // get local resource
                        String localresourceURI = m_mimeMappings.get(extension);
                        writeLocalResource(req,resp,localresourceURI);
                        return true;
                    }
                }
            }
        }
        
        try { 
            resp.getOutputStream().print(m_defaultMessage);
            resp.getOutputStream().flush();
            return true;
        } catch (IOException ioe) {
            throw new RuntimeException("ErrorDefaultHandler-handlerequest - IO Error writing default message",ioe);            
        }
    }    

    public void writeLocalResource(HttpServletRequest req, HttpServletResponse resp, String localresourceURI)
    {
        try {
            URL url = new URL("http://"+req.getServerName()+":"+req.getServerPort()+"/"+localresourceURI);
            URLConnection urlconn = url.openConnection();
            InputStream stream = new BufferedInputStream(urlconn.getInputStream());
            IOUtil.copy(stream, resp.getOutputStream());
            resp.getOutputStream().flush();
            resp.getOutputStream().close();
            stream.close();
        } catch (IOException ioe) {
            throw new RuntimeException("ErrorDefaultHandler-writeLocalResource - IO Error on uri "+localresourceURI,ioe);
        }
    }
    
}
