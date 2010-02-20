package com.uhg.ewp.common.gotcha.contentsource.data.impl;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;

public class BaseContentRequest implements ContentRequest
{
    
    // --- PROPERTIES ----    
    /* getset */
    boolean parsedSuccessfully = false;    
    /* getset */
    Map<String,ContentMetaData> requestMetaData = null;    
    /* getset */
    Set<String> responseMetaDataFields = null;    
    /* getset */
    Locale locale = null;
    /* getset */
    String contentItem = null;
    
    
    
    // getter setters ...
    public Map<String,ContentMetaData> getRequestMetaData() { return requestMetaData; }    
    public void setRequestMetaData(Map<String,ContentMetaData> metaData) { this.requestMetaData = metaData; }

    public Set<String> getResponseMetaDataFields() { return responseMetaDataFields; }    
    public void setResponseMetaDataFields(Set<String> metaData) { this.responseMetaDataFields = metaData; }

    public Locale getLocale() { return locale; }
    public void setLocale(Locale locale) { this.locale = locale; }
    
    public String getContentItem() { return contentItem; }
    public void setContentItem(String contentItem) { this.contentItem = contentItem; }
    
    public boolean isParsedSuccessfully() { return parsedSuccessfully; }
    public void setParsedSuccessfully(boolean parsedSuccessfully) { this.parsedSuccessfully = parsedSuccessfully; }
    
    

}
