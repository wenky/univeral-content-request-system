package com.uhg.umvs.bene.cms.contentretrieval.taglib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

public class ContentRetrievalTag implements Tag
{
    private PageContext pc = null;
    public void setPageContext(PageContext p) {pc = p;}
    
    private Tag parent = null;
    public Tag  getParent() {return parent;}
    public void setParent(Tag t) {parent = t;}

    private String item = null;
    public String getItem() {return item;}
    public void setItem(String s) { item = s;}

    private String source = null;
    public String getSource() {return source;}
    public void setSource(String s) { source = s;}
    
 
    public int doStartTag() throws JspException 
    {
        // where do we get the base url? hardcode for now...
        String filePath = pc.getServletContext().getContextPath()+"/tagCRS?item="+getItem()+(source == null ? "" : "&source="+getSource());
        ServletRequest req = pc.getRequest();
        String url = "http://"+req.getLocalName()+":"+req.getLocalPort()+filePath;
        
        try {
            
            URL theurl = new URL(url);            
            URLConnection urlc = null;
            try {
                urlc = theurl.openConnection();
            } catch (MalformedURLException e) {     
                throw new RuntimeException("ContentRetrievalTag: bad content request url "+url,e);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(theurl.openStream()));             
            IOUtils.copy(in, pc.getOut());
            pc.getOut().flush();
            in.close();
            
        } catch (IOException e) {
            throw new RuntimeException("ContentRetrievalTag: IO error retrievign content from content requesturl "+url,e);
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
            item = null; source = null;
    }

}
