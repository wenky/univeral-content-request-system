package com.uhg.ewp.common.gotcha.contentsource.impl.webdavsardine;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.log.Lg;

public class WebDAVSource implements ContentSource
{
    // ---- PROPERTIES

    // set   
    String baseURL = "";
    
    // getset
    String sourceName = "WebDAV";
    
    // ---- END PROPERTIES

    protected String buildURL(ContentRequest contentReq)
    {
        return baseURL + contentReq.getContentItem();
    }
    

    public ContentResponse getContent(ContentRequest contentItemReq)
    {
        BaseContentResponse response = new BaseContentResponse();

        String url = buildURL(contentItemReq);
        
        try {
            
            // TODO - config for authenticated initialization
            Sardine sardine = SardineFactory.begin();

            // uses HEAD...hmm....
            if (!sardine.exists(url)) {
                response.setFound(false);
                return response;
            }
            
            List<DavResource> resources = sardine.getResources(url);
            
            if (resources == null || resources.size() == 0)
            {
                response.setFound(false);
                return response;
            } else if (resources.size() > 1) {
                throw new SourceRetrievalException("WEBDAVSRC-DUPES",Lg.err("Too many resources located for url "+url));                
            }
            
            DavResource resource = resources.get(0);
            
            Long len = resource.getContentLength();
            if (len != null) {
                response.setSize(len);
            } else {
                response.setSize(-1);
            }

            Date lastmod = resource.getModified();
            if (lastmod != null) {
                response.setLastModified(lastmod.getTime());
            } else {
                response.setLastModified(-1);
            }
            
            String mimetype = resource.getContentType();
            response.setMimetype(mimetype);
            
            InputStream inputstream = sardine.getInputStream(url);
            response.setContent(inputstream);
            
            response.setFound(true);
            response.setSourceId(url);
            response.setSourceName(getSourceName());
            response.setItemRef(contentItemReq.getContentItem());
            
            return response;
            
        } catch (SardineException se) {
            throw new SourceRetrievalException("WEBDAVSRC-FAIL",Lg.err("Error retrieving resource from url "+url,se),se);
        }
    }
    
    //gettersetters ----
    
    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

}
