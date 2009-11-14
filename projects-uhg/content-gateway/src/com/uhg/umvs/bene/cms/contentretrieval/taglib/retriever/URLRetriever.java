package com.uhg.umvs.bene.cms.contentretrieval.taglib.retriever;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.io.IOUtils;

import com.uhg.umvs.bene.cms.contentretrieval.taglib.interfaces.ContentRetriever;

public class URLRetriever implements ContentRetriever
{
    String baseURL = null;
    
    public void init(Map config)
    {
        // get base url from
        baseURL = (String)config.get("BaseURL");
    }
    
    // kinda pointless, but for ease of extension/customization of behavior...
    String getItemURL(String item, PageContext pagecontext, Tag taginstance)
    {
        return baseURL + item;
    }
    

    public void getContent(String item, PageContext pagecontext, Tag taginstance)
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
            BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));             
            IOUtils.copy(in, pagecontext.getOut());
            pagecontext.getOut().flush();
            in.close();
            
        } catch (IOException e) {
            throw new RuntimeException("ContentRetrievalTag-URLRetriever: IO error retrievign content from content requesturl "+itemurl,e);
        }

    }


}
