package com.uhg.ewp.common.gotcha.requestparser.idef;

import javax.servlet.http.HttpServletRequest;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;

public interface ContentRequestParser
{
    public ContentRequest parseRequest(HttpServletRequest req);
}
