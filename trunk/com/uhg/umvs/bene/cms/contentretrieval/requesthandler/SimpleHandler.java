package com.uhg.umvs.bene.cms.contentretrieval.requesthandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;

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
                contentsource.getContent(item, req, resp);
                return true;
            }
        }
        
        // no match
        return false;
    }
    
    

}
