package com.uhg.umvs.bene.cms.contentretrieval.requesthandler;

import static com.uhg.umvs.bene.cms.contentretrieval.util.Lg.inf;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;



public class JBossCMSSimpleHandler implements ContentRequestHandler
{
    ContentSource contentsource;
    public void setContentSource(ContentSource contentsource) { this.contentsource = contentsource; }
    
    String sourcename;
    public void setSourceName(String name) { this.sourcename = name; }
    
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {   
        // check for checkexists request property
        String headerexists = req.getHeader("checkexists");
        String attrexists = (String)req.getAttribute("checkexists");
        
        // determine source
        String source = req.getParameter("source");
        String item = req.getParameter("item");
                
        if (source != null) {
            if (source.equals(sourcename)) {
                if ("true".equals(headerexists)) {
                    if (contentsource.hasContent(item, req)) {
                        try {
                            resp.getOutputStream().print("exists");
                            resp.getOutputStream().flush();
                        } catch (IOException ioe) {
                            throw new RuntimeException("JBossCMSSimpleHandler: error intercepting checkexists, IO Error writing response",ioe);
                        }
                    }
                    return true;
                } else {
                    inf("Request Match: handling item %s from source %s",item,source);
                    contentsource.getContent(item, req, resp);
                    return true;
                }
            }
        }
        
        // no match
        return false;
    }
    
    

}
