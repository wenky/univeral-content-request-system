package com.uhg.umvs.bene.cms.contentretrieval.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface ContentSource
{
    public void getContent(String contentItem, HttpServletRequest request, HttpServletResponse resp);    
}
