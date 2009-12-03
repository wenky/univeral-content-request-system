package com.uhg.umvs.bene.cms.contentretrieval.server.requesthandler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.server.interfaces.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.server.interfaces.ContentSource;

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
                contentsource.getContent(item,req,resp);
                return true;
            }
        }

        // return "Not Handled"
        return false;
    }
    
    

}
