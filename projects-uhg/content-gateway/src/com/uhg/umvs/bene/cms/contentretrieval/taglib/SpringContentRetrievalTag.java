package com.uhg.umvs.bene.cms.contentretrieval.taglib;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.uhg.umvs.bene.cms.contentretrieval.taglib.interfaces.ContentRetriever;

public class SpringContentRetrievalTag implements Tag
{
    private PageContext pc = null;
    public void setPageContext(PageContext p) {pc = p;}
    
    private Tag parent = null;
    public Tag  getParent() {return parent;}
    public void setParent(Tag t) {parent = t;}

    private String item = null;
    public String getItem() {return item;}
    public void setItem(String s) { item = s;}

    
    protected static String springBeanName = "TBP-CRS-Taglib-ContentRetriever";
    protected static ContentRetriever contentRetriever = null;
    
    public void loadConfig()
    {
        ServletContext srvctx = pc.getServletContext();
        ApplicationContext appContext = WebApplicationContextUtils.getRequiredWebApplicationContext(srvctx);        
        //ApplicationContext appContext = (ApplicationContext) WebApplicationContextUtils.getWebApplicationContext(getServletConfig().getServletContext());
        contentRetriever = (ContentRetriever)appContext.getBean(springBeanName);
        contentRetriever.init();
    }
 
    public int doStartTag() throws JspException 
    {
        // lazyload, since J2EE has no obvious hooks for initialization of tag libs
        if (contentRetriever == null) {
            loadConfig();
        }
        contentRetriever.getContent(item, pc, this);
        return SKIP_BODY;
    }
    
    public int doEndTag() throws JspException 
    {
        return EVAL_PAGE;
    }
    
    public void release() 
    {
        pc = null;
        parent = null;
        item = null;
    }

}
