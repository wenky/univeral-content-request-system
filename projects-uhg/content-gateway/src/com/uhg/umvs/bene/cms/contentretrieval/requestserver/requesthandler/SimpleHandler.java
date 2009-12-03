package com.uhg.umvs.bene.cms.contentretrieval.requestserver.requesthandler;

import static com.uhg.umvs.bene.cms.contentretrieval.util.Lg.inf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;



public class SimpleHandler implements ContentRequestHandler
{
    ContentSource contentsource;
    public void setContentSource(ContentSource contentsource) { this.contentsource = contentsource; }
    
    String sourcename;
    public void setSourceName(String name) { this.sourcename = name; }
    
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {        
        // determine source
        String source = req.getParameter("source");
        String item = req.getParameter("item");
        if (source != null) {
            if (source.equals(sourcename)) {
                inf("Request Match: handling item %s from source %s",item,source);
                contentsource.getContent(item, req, resp);
                return true;
            }
        }
        
        // no match
        return false;
    }
    
    

}
