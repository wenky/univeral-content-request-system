package com.uhg.umvs.bene.cms.contentretrieval.requesthandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;


// implements a basic fall-through: we ask the source if it can find the content. 
// if found, we execute and return. Otherwise, return false and let another handler handle it.

// this can work well as a "last-ditch" handler or series of handlers if the previous handlers 
// couldn't explicitly identify source (usually a very fast CPU task of URL pattern analysis). 

public class FallThroughHandler implements ContentRequestHandler
{
    ContentSource contentsource;
    public void setContentSource(ContentSource contentsource) { this.contentsource = contentsource; }
    
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp) {
        String extrapath = req.getPathInfo();
        
        if (contentsource.hasContent(extrapath, req)) {
            contentsource.getContent(extrapath, req, resp);
            return true;
        }
        
        return false;
        
    }

}
