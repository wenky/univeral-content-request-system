package com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces;

import javax.servlet.http.HttpServletRequest;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.ContentResponse;


public interface ContentSource
{

    public boolean hasContent(String contentItem, HttpServletRequest request);

    public ContentResponse getContent(String contentItem, HttpServletRequest req);
}