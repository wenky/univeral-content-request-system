package com.uhg.umvs.bene.cms.contentretrieval.taglib.interfaces;

import java.util.Map;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public interface ContentRetriever
{
    public void init(Map config);
    
    public void getContent(String item, PageContext pagecontext, Tag tag);
}
