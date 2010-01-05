package com.uhg.umvs.bene.cms.contentretrieval.taglib.interfaces;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public interface ContentRetriever
{
    // for non-spring/XStream... Spring can ignore these methods since it should produce a fully initialized instance
    public void init();
    public void setConfiguration(Map config);
        
    public InputStream getContent(String item, PageContext pagecontext, Tag tag);
}
