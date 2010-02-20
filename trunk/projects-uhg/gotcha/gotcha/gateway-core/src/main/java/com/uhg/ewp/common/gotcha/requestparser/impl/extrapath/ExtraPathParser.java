package com.uhg.ewp.common.gotcha.requestparser.impl.extrapath;

import javax.servlet.http.HttpServletRequest;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentRequest;
import com.uhg.ewp.common.gotcha.requestparser.idef.ContentRequestParser;

public class ExtraPathParser implements ContentRequestParser
{

    public ContentRequest parseRequest(HttpServletRequest servletreq)
    {
        String extrapath = servletreq.getPathInfo();
        BaseContentRequest contentrequest = new BaseContentRequest();
        contentrequest.setParsedSuccessfully(true);
        contentrequest.setContentItem(extrapath);
        return contentrequest;
    }

}
