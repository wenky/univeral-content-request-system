package com.uhg.ewp.common.gotcha.contentsource.impl.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceAccessException;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.log.Lg;

public class URLSource implements ContentSource
{
    // ---- PROPERTIES

    // set   
    String baseURL = "";

    //getset
    String sourceName = "URL";
    

    // ---- END PROPERTIES

    
    protected String buildURL(ContentRequest contentReq)
    {
        return baseURL + contentReq.getContentItem();
    }

    public ContentResponse getContent(ContentRequest contentReq)
    {
        
        String remoteurl = buildURL(contentReq);        
        URL url = null;
        try { 
            url = new URL(remoteurl);
        } catch (MalformedURLException male) {
            throw new SourceRetrievalException("URLSRC-BADURL",Lg.err("malformed url "+remoteurl),male);
        }
        
        BaseContentResponse cmsresp = getUrl(url);
        cmsresp.setItemRef(contentReq.getContentItem());
        cmsresp.setSourceName(getSourceName());
        cmsresp.setSourceId(remoteurl);
        
        return cmsresp;
    }
    
    protected BaseContentResponse getUrl(URL url) 
    {
        BaseContentResponse response = new BaseContentResponse();
        
        
        URLConnection connection = null;
        try { 
            connection = url.openConnection();
        } catch (IOException ioe) {
            // is an ioe here equal to notfound? I don't think so
            throw new SourceAccessException("URLSRC-OPENFAIL",Lg.err("IO error opening connection to url "+url),ioe);
        }
        
        InputStream input = null;
        try { 
            input = connection.getInputStream(); // TODO: how do we detect not found?
        } catch (IOException ioe) {
            throw new SourceRetrievalException("URLSRC-GETFAIL",Lg.err("IO error in content transmission from url "+url),ioe);
        }
        
        int length = connection.getContentLength();
        if (length > 0) {
            
            response.setFound(true);
            response.setSize(length);
            
            long lastmod = connection.getLastModified();
            if (lastmod <= 0L) {
                response.setLastModified(-1L);
            } else {
                response.setLastModified(lastmod);
            }
            
            // TODO mimetype
            
            response.setContent(input);
            
            return response;
        } else {
            response.setFound(false);
            return response;
        }
        
        // do we close the connection? The stream hasn't been read yet. Hopefully the stream knows how to close itself when it's done.
    }
    
    //gettersetters ----
    
    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }    

}
