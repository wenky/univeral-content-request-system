package com.uhg.umvs.bene.cms.contentretrieval.taglib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;
import com.uhg.umvs.bene.cms.contentretrieval.taglib.interfaces.ContentRetriever;

public class XStreamConfiguredContentRetrievalTag implements Tag
{
    private PageContext pc = null;
    public void setPageContext(PageContext p) {pc = p;}
    
    private Tag parent = null;
    public Tag  getParent() {return parent;}
    public void setParent(Tag t) {parent = t;}

    private String item = null;
    public String getItem() {return item;}
    public void setItem(String s) { item = s;}

    
    protected static String tagConfigFile = "ContentRetrievalTagConfig.xml";
    protected static ContentRetriever contentRetriever = null;
    
    public void loadConfig()
    {
        //synchronized (tagConfig) {    
            String classname = null;
            try { 
                URL configfileurl = Thread.currentThread().getContextClassLoader().getResource(tagConfigFile);
                InputStream contents = configfileurl.openStream();
                XStream xstream = new XStream();
                Map tagConfig = (Map)xstream.fromXML(contents);
                classname = (String)tagConfig.get("Retriever");
                contentRetriever = (ContentRetriever)Class.forName(classname).newInstance();
                contentRetriever.setConfiguration((Map)tagConfig.get("RetrieverConfig"));
                contentRetriever.init();
            } catch (IOException ioe) {
                throw new RuntimeException("ContentRetrievalTag: I/O Error loading tag configuration file, check that it is on the webapp's classpath "+tagConfigFile,ioe);
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException("ContentRetrievalTag: ClassNotFoundException on creation of content retriever "+classname,cnfe);
            } catch (ClassCastException cce) {
                throw new RuntimeException("ContentRetrievalTag: ClassCastException on creation of content retriever "+classname,cce);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("ContentRetrievalTag: IllegalAccessException on creation of content retriever "+classname,iae);
            } catch (InstantiationException ie) {
                throw new RuntimeException("ContentRetrievalTag: InstantiationException on creation of content retriever "+classname,ie);
            } 
        //}
    }
 
    public int doStartTag() throws JspException 
    {
        // lazyload, since J2EE has no obvious hooks for this
        if (contentRetriever == null) {loadConfig();}
        
        try { 
            InputStream is = contentRetriever.getContent(item, pc, this);
            IOUtils.copy(is, pc.getResponse().getOutputStream());
        } catch (IOException ioe) {
            throw new JspException("CMS retrieval error of item "+item,ioe);
        }
        
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
