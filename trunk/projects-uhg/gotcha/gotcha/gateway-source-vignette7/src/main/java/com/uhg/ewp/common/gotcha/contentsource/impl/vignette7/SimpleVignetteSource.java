package com.uhg.ewp.common.gotcha.contentsource.impl.vignette7;


import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.log.Lg;
import com.uhg.ewp.common.gotcha.util.vignette7.AttributeUtils;
import com.uhg.ewp.common.gotcha.util.vignette7.ChannelUtils;
import com.uhg.ewp.common.gotcha.util.vignette7.ContentInstanceUtils;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ObjectType;

/**
 * SimpleVignetteSource assumes the requested content item maps to a specific ContentInstance, and the response merely 
 * has to return its content, either via the configured default attribute name, or from a configured type-to-attrname
 * map. 
 * 
 * No additional metadata, filtering, templating, or sorting of the content is done.
 * 
 * 
 * @author cmuell7
 *
 */

public class SimpleVignetteSource implements ContentSource
{    

    //----- PROPERTIES
    
    String siteName = null;
    
    String baseChannel = ""; 

    Map<String,String> types = null;
    
    String defaultAttributeName = null;
    
    String sourceName = "Vignette V7";

    //---- END PROPERTIES
    
    Map<String,String> channelGuidCache = new HashMap<String,String>();
    Channel baseChannelMO = null;

    
    public ContentResponse getContent(ContentRequest req)
    {        
        BaseContentResponse response = new BaseContentResponse();
        
        ContentInstance ci = getContentInstanceForItem(req.getContentItem());
        
        if (ci == null)
        {
            response.setFound(false);
            return response;
        }
        
        try { 
            // determine if content instance's content is stored in the db (CLOB) or as a static file (has file attribute)
            ObjectType type = ci.getObjectTypeRef().getObjectType();
            String typename = type.getName();
            
                        
            String clobattrname = null;
            if (types != null && types.containsKey(typename)) {
                clobattrname = types.get(typename);
            } else {
                clobattrname = defaultAttributeName;
            }
            String clob = AttributeUtils.getSimpleContent(ci, clobattrname);
            if (clob != null) {
                response.setFound(true);
                Date lastmoddate = ci.getLastModTime();
                if (lastmoddate != null) {
                    Long lastmod = lastmoddate.getTime();
                    response.setLastModified(lastmod);
                }
                response.setMimetype("text/html");
                
                byte[] content = clob.getBytes();
                int size = content.length;
                response.setSize(size);
                ByteArrayInputStream inputstream = new ByteArrayInputStream(content);
                
                response.setContent(inputstream);
                response.setItemRef(req.getContentItem());
                response.setSourceId(ci.getContentManagementId().getId());
                response.setSourceName(getSourceName());
                return response;
            } else {
                response.setFound(false);
                return response;
            }

        } catch (ApplicationException ae) {
            throw new SourceRetrievalException("V7SRC-GETCONTENT",Lg.err("Application Exception getting content for item "+req.getContentItem(),ae),ae);
        } 
    }
    
    ContentInstance getContentInstanceForItem(String contentItem)
    {
        String subchannelpath = null;
        String contentitemname = null;
        if (contentItem.indexOf('/') != -1) {
            subchannelpath = contentItem.substring(0,contentItem.lastIndexOf('/'));
            contentitemname = contentItem.substring(contentItem.lastIndexOf('/')+1);
        } else {
            subchannelpath = "";
            contentitemname = contentItem;
        }

        Channel ch = getRequestedSubchannel(subchannelpath);        
        ContentInstance ci = ContentInstanceUtils.getContentInstanceFromChannel(ch, contentitemname);
        
        return ci;        
    }
    
    protected Channel getRequestedSubchannel(String subchannels)
    {
        // initialize base channel
        if (baseChannelMO == null) {
            baseChannelMO = ChannelUtils.getRelativeSubchannel(ChannelUtils.getHomeChannelForSite(getSiteName()), getBaseChannel());
        }
        
        Channel channel;
        String cachekey = subchannels;
        if (channelGuidCache.containsKey(cachekey)){
            // presumably the direct Guid lookup is faster than navigate-and-iterate through the subchannel structure
            String guid = channelGuidCache.get(cachekey);
            channel = ChannelUtils.getChannelByGuid(guid);
        } else {
            if (StringUtils.isEmpty(subchannels)) {
                return baseChannelMO;
            } else {
                channel = ChannelUtils.getRelativeSubchannel(baseChannelMO, subchannels);
                channelGuidCache.put(cachekey, channel.getContentManagementId().getId());
            }
        }
            
        return channel;
    }    

    
    // ---- property getter/setters
    public void setBaseChannel(String baseChannelPath) { this.baseChannel = baseChannelPath; }
    public void setTypes(Map<String, String> typeattrmap) { this.types = typeattrmap;}
    public void setDefaultAttributeName(String defaultAttributeName)
    {
        this.defaultAttributeName = defaultAttributeName;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public String getBaseChannel()
    {
        return baseChannel;
    }
    

}
