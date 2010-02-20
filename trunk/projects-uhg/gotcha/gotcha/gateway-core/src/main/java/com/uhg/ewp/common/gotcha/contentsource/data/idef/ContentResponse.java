package com.uhg.ewp.common.gotcha.contentsource.data.idef;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ContentResponse
{
    public boolean isFound();                           // Was requested item found?
        
    public String getSourceId();                        // item name/identifier in the source system    

    public String getSourceName();                      // source system that contained the item match    
    
    public String getItemRef();                         // item reference used to lookup the item (usually the REST reference path)
    
    public long getSize();                              // content size (useful for paging/caching determinations)

    public InputStream getContent();                    // inputstream for content item

    public String getMimetype();                        // Mimetype if it can be determined

    public long getLastModified();                      // last modified (useful for caching)

    public Map<String,ContentMetaData> getMetaData();   // in case metadata values need to be returned along with the content

    public boolean isMultipart();                       // multiresponse
    
    public List<ContentResponse> getMultipartResponse();

    // ?version data? ?label/mark/state?
}
