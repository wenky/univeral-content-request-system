package com.uhg.umvs.bene.cms.contentretrieval.requesthandler;

import static com.uhg.umvs.bene.cms.contentretrieval.util.Lg.inf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.interfaces.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.interfaces.ContentSource;

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
        contentsource.getContent(item, req, resp);
        
        // no match
        return false;
    }

}
