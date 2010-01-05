package com.uhg.umvs.bene.cms.contentretrieval.requestserver.requesthandler;

import static com.uhg.umvs.bene.cms.contentretrieval.util.Lg.inf;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.ContentResponse;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;

public class ExtraPathHandler implements ContentRequestHandler
{
    ContentSource contentsource;
    public void setContentSource(ContentSource contentsource) { this.contentsource = contentsource; }
    
    String sourceprefix;
    public void setSourcePrefix(String name) { this.sourceprefix = name; }
    
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        String extrapath = req.getPathInfo();
        if (!extrapath.startsWith(sourceprefix)) {
            return false;
        }
        
        // determine source
        String item = null;
        try {
            item = extrapath.substring(sourceprefix.length());
        } catch (Exception e) {
            throw new RuntimeException("ExtraPathHandler: Error getting item key from url extrapath "+extrapath+" for matching prefix "+sourceprefix,e);
        }
        
        inf("Request Match: handling item %s from sourceprefix %s",item,sourceprefix);
        ContentResponse response = contentsource.getContent(extrapath, req);
        
        try {
            if (response.getMimetype() != null) {
                resp.setContentType(response.getMimetype());
            }
            IOUtils.copy(response.getContent(), resp.getOutputStream());
        } catch (IOException ioe) {
            throw new RuntimeException("IOException writing content response to HTTP Response",ioe);
        }
        
        
        // no match
        return false;
    }

}
