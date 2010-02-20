package com.uhg.ewp.common.gotcha.contentsource.data.idef;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface ContentRequest
{
    public boolean isParsedSuccessfully(); 
    
    public String getContentItem();     // item reference (usually extracted from parsed REST url)             
    
    public Locale getLocale();          // locale, if applicable
    
    public Map<String,ContentMetaData> getRequestMetaData();    // metadata possibly needed to locate the requested content, usually from the HttpRequest
    
    public Set<String> getResponseMetaDataFields();   // metadata to be queried from the content source in addition to the content
    
}
