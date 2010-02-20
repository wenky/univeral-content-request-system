package com.uhg.ewp.common.gotcha.requesthandler.idef;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ContentRequestHandler
{
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp);
}
