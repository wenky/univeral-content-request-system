package com.uhg.umvs.bene.cms.contentretrieval.server.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ContentRequestHandler
{
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp);
}
