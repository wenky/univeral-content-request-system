package com.uhg.umvs.bene.cms.contentretrieval.server;

import static com.uhg.umvs.bene.cms.contentretrieval.util.Lg.err;


import com.uhg.umvs.bene.cms.contentretrieval.server.interfaces.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ContentRequestServer
{
    List<ContentRequestHandler> m_sourceHandlers = null;    
    public void setSourceHandlers(List<ContentRequestHandler> sourceHandlers) {m_sourceHandlers = sourceHandlers;}
    
    ContentRequestHandler m_defaultHandler = null;
    public void setDefaultHandler(ContentRequestHandler defaulthandler) { m_defaultHandler = defaulthandler; }

    ContentRequestHandler m_errorHandler = null;
    public void setErrorHandler(ContentRequestHandler errorhandler) { m_errorHandler = errorhandler; }


    public void processContentRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        // threadlocal the req/resp?
        // iterate through source handlers
        try {
            boolean handled = false;
            for (ContentRequestHandler handler : m_sourceHandlers) {
                handled = handler.handleRequest(req, resp);
                if (handled) {
                    // stop iteration thru sources
                    break;
                }
            }
            if (!handled && m_defaultHandler != null) {
                // return a "not found"/"not handled" text or mime-appropriate response if we can determine the desired mime type
                m_defaultHandler.handleRequest(req, resp);
            }
        } catch (Exception e) {
            try { 
                if (m_errorHandler != null) {
                    // attempt to handle the error with an error text or mime-appropriate response if we can determine the desired mime type
                    m_errorHandler.handleRequest(req, resp);
                }
            } catch (Exception ee) {/* well, we tried...*/}
            if(err())err("Error in handling request %s",req.getRequestURL().toString());
        }
    }

}
