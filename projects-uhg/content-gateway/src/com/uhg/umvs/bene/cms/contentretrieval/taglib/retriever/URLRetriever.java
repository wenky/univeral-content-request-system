package com.uhg.umvs.bene.cms.contentretrieval.taglib.retriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import com.uhg.umvs.bene.cms.contentretrieval.taglib.interfaces.ContentRetriever;

public class URLRetriever implements ContentRetriever
{
    String baseURL = null;
    public void setBaseURL(String baseurl) { baseURL = baseurl; }
   
        
    // kinda pointless for now, but for ease of extension/customization of behavior...
    String getItemURL(String item, PageContext pagecontext, Tag taginstance)
    {
        return baseURL + item;
    }

    public InputStream getContent(String item, PageContext pagecontext, Tag taginstance)
    {
        String itemurl = getItemURL(item,pagecontext,taginstance);
        try {
            
            URL url = new URL(itemurl);            
            URLConnection urlc = null;
            try {
                urlc = url.openConnection();
                //urlc.addRequestProperty(key, value); // add url header, allegedly...
            } catch (MalformedURLException e) {     
                throw new RuntimeException("ContentRetrievalTag-URLRetriever: bad content request url "+url,e);
            }
            return urlc.getInputStream();             
            
        } catch (IOException e) {
            throw new RuntimeException("ContentRetrievalTag-URLRetriever: IO error retrieving content from content requesturl "+itemurl,e);
        }
    }

    // for non-spring initialization
    Map configMap = null;    
    public void setConfiguration(Map config) {configMap = config;}
    public void init() { baseURL = (String)configMap.get("BaseURL"); }

}
