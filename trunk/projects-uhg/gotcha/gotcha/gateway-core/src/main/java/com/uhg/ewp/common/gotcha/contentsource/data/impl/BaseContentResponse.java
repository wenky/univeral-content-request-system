package com.uhg.ewp.common.gotcha.contentsource.data.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;

public class BaseContentResponse implements ContentResponse
{
    
    // --- PROPERTIES ----    
    /* getset */
    boolean found = false;    
    /* getset */
    String sourceId = null;    
    /* getset */
    String sourceName = null;    
    /* getset */
    String itemRef = null;
    /* getset */
    long size = -1L;
    /* getset */
    InputStream content = null;
    /* getset */
    long lastModified = -1L;
    /* getset */
    String mimetype = null;
    /* getset */
    Map<String,ContentMetaData> metaData = null;
    /* getset */
    boolean multipart = false;    
    /* getset */
    List<ContentResponse> multipartResponse = null;    
    
    
    

    public boolean isMultipart()
    {
        return multipart;
    }
    public void setCompound(boolean compound)
    {
        this.multipart = compound;
    }
    public List<ContentResponse> getMultipartResponse()
    {
        return multipartResponse;
    }
    public void setCompoundResponse(List<ContentResponse> compoundResponse)
    {
        this.multipartResponse = compoundResponse;
    }
    // getter setters ...
    public String getSourceName()
    {
        return sourceName;
    }
    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }
    public boolean isFound()
    {
        return found;
    }
    public void setFound(boolean found)
    {
        this.found = found;
    }
    public String getSourceId()
    {
        return sourceId;
    }
    public void setSourceId(String sourceId)
    {
        this.sourceId = sourceId;
    }
    public String getItemRef()
    {
        return itemRef;
    }
    public void setItemRef(String itemRef)
    {
        this.itemRef = itemRef;
    }
    public long getSize()
    {
        return size;
    }
    public void setSize(long size)
    {
        this.size = size;
    }
    public InputStream getContent()
    {
        return content;
    }
    public void setContent(InputStream content)
    {
        this.content = content;
    }
    public long getLastModified()
    {
        return lastModified;
    }
    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }
    public String getMimetype()
    {
        return mimetype;
    }
    public void setMimetype(String mimetype)
    {
        this.mimetype = mimetype;
    }
    public Map<String,ContentMetaData> getMetaData()
    {
        return metaData;
    }
    public void setMetaData(Map<String,ContentMetaData> metaData)
    {
        this.metaData = metaData;
    }    
    
}