package com.uhg.ewp.common.gotcha.contentsource.impl.url;

import java.net.URL;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;

public class ClasspathSource extends URLSource implements ContentSource
{

    String sourceName = "Classpath";
    
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }    

    
    public ContentResponse getContent(ContentRequest contentReq)
    {        
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();        
        
        String itemurl = buildURL(contentReq);
        
        URL resourceURL = cloader.getResource(itemurl);
                
        ContentResponse response = this.getUrl(resourceURL);
        
        return response;
        
    }
}
