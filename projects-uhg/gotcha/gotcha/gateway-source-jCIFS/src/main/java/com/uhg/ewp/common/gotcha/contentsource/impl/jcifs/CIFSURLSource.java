package com.uhg.ewp.common.gotcha.contentsource.impl.jcifs;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.contentsource.impl.url.URLSource;

public class CIFSURLSource extends URLSource
{
    
    // set
    String sourceName = "CIFS-URL";


    public String getSourceName()
    {
        return sourceName;
    }


    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }


    public ContentResponse getContent(ContentRequest contentReq)
    {
        // do we have to do this every time?
        jcifs.Config.registerSmbURLHandler();        
                        
        ContentResponse response = super.getContent(contentReq);
        
        return response;
        
    }
}
