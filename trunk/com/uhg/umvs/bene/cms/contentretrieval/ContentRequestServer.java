package com.uhg.umvs.bene.cms.contentretrieval;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentRequestHandler;

public class ContentRequestServer
{
    List<ContentRequestHandler> m_sourceHandlers = null;    
    public void setSourceHandlers(List<ContentRequestHandler> sourceHandlers) {m_sourceHandlers = sourceHandlers;}

    public void processContentRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        // threadlocal the req/resp?
        for (ContentRequestHandler handler : m_sourceHandlers) {
            boolean handled = handler.handleRequest(req, resp);
            if (handled) {
                // stop iteration
                break;
            }
        }
    }

}
