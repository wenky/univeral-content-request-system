package com.uhg.ewp.common.gotcha.requestserver.idef;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ContentRequestServer
{

    public void processContentRequest(HttpServletRequest req, HttpServletResponse resp);

}