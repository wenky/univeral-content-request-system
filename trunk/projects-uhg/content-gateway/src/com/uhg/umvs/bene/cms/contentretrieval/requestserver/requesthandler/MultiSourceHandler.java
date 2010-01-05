package com.uhg.umvs.bene.cms.contentretrieval.requestserver.requesthandler;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.ContentResponse;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;

public class MultiSourceHandler implements ContentRequestHandler
{
    Map<String,ContentSource> contentsources;
    public void setContentSources(Map<String, ContentSource> contentsources) { this.contentsources = contentsources; }
    
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        // determine source
        String source = req.getParameter("source");
        String item = req.getParameter("item");
        if (source != null && item != null) {
            if (contentsources.containsKey(source)) {
                ContentSource contentsource = contentsources.get(source);
                ContentResponse response = contentsource.getContent(item, req);
                
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
        }

        // return "Not Handled"
        return false;
    }
    
    

}
