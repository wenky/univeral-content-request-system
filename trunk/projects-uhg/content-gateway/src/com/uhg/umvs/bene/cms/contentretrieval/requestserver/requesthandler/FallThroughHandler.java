package com.uhg.umvs.bene.cms.contentretrieval.requestserver.requesthandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.ContentResponse;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;


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
            ContentResponse response = contentsource.getContent(extrapath, req);
            
            try {
                if (response.getMimetype() != null) {
                    resp.setContentType(response.getMimetype());
                }
                IOUtils.copy(response.getContent(), resp.getOutputStream());
            } catch (IOException ioe) {
                throw new RuntimeException("IOException writing content response to HTTP Response",ioe);
            }
            return true;
        }
        
        return false;
        
    }

}
