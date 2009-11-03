package com.uhg.umvs.bene.cms.contentretrieval.taglib;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

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
            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(url);

            client.executeMethod(method);
            String htmlpage = method.getResponseBodyAsString();
            
            pc.getOut().write(htmlpage);
            
        } catch (HttpException e) {
            throw new RuntimeException("ContentRetrievalTag: http error on url "+url,e);
        } catch (IOException e) {
            throw new RuntimeException("ContentRetrievalTag: IO error on url "+url,e);
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
