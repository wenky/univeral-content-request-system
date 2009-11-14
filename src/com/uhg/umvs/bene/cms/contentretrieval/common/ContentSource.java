package com.uhg.umvs.bene.cms.contentretrieval.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface ContentSource
{
    public boolean hasContent(String contentItem, HttpServletRequest request);
    
    public void getContent(String contentItem, HttpServletRequest request, HttpServletResponse resp);    
}
