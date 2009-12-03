package com.uhg.umvs.bene.cms.contentretrieval.requestserver.requesthandler;

import static com.uhg.umvs.bene.cms.contentretrieval.util.Lg.inf;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentRequestHandler;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;

public class JBossCMSExtraPathHandler implements ContentRequestHandler
{
    ContentSource contentsource;
    public void setContentSource(ContentSource contentsource) { this.contentsource = contentsource; }
    
    String sourceprefix;
    public void setSourcePrefix(String name) { this.sourceprefix = name; }
    
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        // check for checkexists request property
        String headerexists = req.getHeader("checkexists");
        
        if (inf())try{Enumeration<String> hdrs=req.getHeaderNames();while(hdrs.hasMoreElements()) {String h = hdrs.nextElement(); inf("hdr: "+h+" val: "+req.getHeader(h)); }}catch(Exception e){}
        
        String attrexists = (String)req.getAttribute("checkexists");
        
        
        String extrapath = req.getPathInfo();
        if (!extrapath.startsWith(sourceprefix)) {
            return false;
        }
        
        // determine source
        String item = null;
        try {
            item = extrapath.substring(sourceprefix.length());
        } catch (Exception e) {
            throw new RuntimeException("JBossCMSExtraPathHandler: Error getting item key from url extrapath "+extrapath+" for matching prefix "+sourceprefix,e);
        }

        if ("true".equals(headerexists)) {
            if (contentsource.hasContent(item, req)) {
                try {
                    resp.getOutputStream().print("exists");
                    resp.getOutputStream().flush();
                } catch (IOException ioe) {
                    throw new RuntimeException("JBossCMSExtraPathHandler: error intercepting checkexists, IO Error writing response",ioe);
                }
            }
            return true;
        } else {
            inf("Request Match: handling item %s from sourceprefix %s",item,sourceprefix);
            contentsource.getContent(item, req, resp);
        }
        
        // no match
        return false;
    }

}
