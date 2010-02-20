package com.uhg.ewp.common.gotcha.contentsource.impl.vignette7;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.log.Lg;
import com.uhg.ewp.common.gotcha.util.vignette7.AttributeUtils;
import com.uhg.ewp.common.gotcha.util.vignette7.ChannelUtils;
import com.vignette.as.client.common.RequestParameters;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ObjectType;

/**
 * 
 * retrieves an entire subchannel's contents as a compound response
 * 
 * requested item must be a channel only
 * 
 * contenttype -> attribute mappings may be necessary if default attribute doesn't work for all content items
 * 
 * typeAttributeProcessing is used to get direction for how to decode attribute values or value lists.
 *    -- map of key typename.fieldname to processing instruction, which is either a string keyword or a custom processor (TODO)
 * 
 * !does not retrieve static files!
 * 
 * @author cmuell7
 *
 */

public class VignetteSubchannelSource implements ContentSource
{    

    //----- PROPERTIES
    
    String siteName = null;
    
    String baseChannel = ""; 

    Map<String,String> typeSpecificContentAttributes = null;

    Set<String> guidDecodeAttributes = null; 

    String defaultAttributeName = null;
    
    String sourceName = "Vignette V7";
    
    Set<String> allowableContentTypesFilter = null;

    //---- END PROPERTIES

    


    Map<String,String> channelGuidCache = new HashMap<String,String>();
    Channel baseChannelMO = null;

    
    public ContentResponse getContent(ContentRequest req)
    {        
        BaseContentResponse response = new BaseContentResponse();

        Channel ch = getRequestedSubchannel(req.getContentItem());
        
        if (ch == null) 
        {
            response.setFound(false);
            return response;
        }
        
        // TODO set channel props for base request...
        
        List<ContentResponse> subitems = new ArrayList<ContentResponse>();
        
        try {
            RequestParameters reqParams = RequestParameters.getImmutableInstanceTopRelationOnly();
            Lg.trc("get filtered content instance list from channel");
            List instances = filterInstances(ch.getContentInstances(reqParams).asList(), req, ch);
            
            Lg.trc("retrieve content and metadata for content instances into content response");
            for (int i=0; i < instances.size(); i++) {
                BaseContentResponse subitem = new BaseContentResponse();
                
                ContentInstance ci = (ContentInstance)instances.get(i);
                //re-retrieve
                ci = (ContentInstance)ContentInstance.findByContentManagementId(ci.getContentManagementId());
                instances.set(i,ci);

                ObjectType type = ci.getObjectTypeRef().getObjectType();
                String typename = type.getName();
                
                String clobattrname = null;                            
                if (typeSpecificContentAttributes != null && typeSpecificContentAttributes.containsKey(typename)) {
                    clobattrname = typeSpecificContentAttributes.get(typename);
                } else {
                    clobattrname = defaultAttributeName;
                }
                
                Set<String> requestedMetaData = req.getResponseMetaDataFields();
                
                // get content and attribute values (Object[] is a multivalue response)
                Object[] search = AttributeUtils.getContentAndMetaDataAttributeValues(ci, clobattrname, requestedMetaData);                
                List contentvalues = (List)search[0];
                Map<String,ContentMetaData> attrvalues = (Map<String,ContentMetaData>)search[1];

                String clob = contentvalues != null && contentvalues.size() > 0 ? (String)contentvalues.get(0) : null;
                                
                subitem.setItemRef(ci.getName());
                subitem.setSourceId(ci.getContentManagementId().getId());
                subitem.setSourceName(getSourceName());
                subitem.setMetaData(attrvalues);
                
                if (clob != null) {
                    subitem.setFound(true);
                    subitem.setContent(IOUtils.toInputStream(clob));
                    Date lastmoddate = ci.getLastModTime();
                    if (lastmoddate != null) {
                        Long lastmod = lastmoddate.getTime();
                        subitem.setLastModified(lastmod);
                    }
                    
                    int size = clob.getBytes().length;
                    subitem.setSize(size);
                    
                    // TODO: mimetype determination
                }

                subitems.add(subitem);                
            }
            
            response.setFound(true);
            response.setCompound(true);
            response.setCompoundResponse(subitems);
            
            // now that all metadata and content is retrieved, provide a postprocess hook
            response = processResponse(req,response,ch,instances);
            
            return response;
            
        } catch (ValidationException ve) {
            throw new SourceRetrievalException("V7SubChSRC-ITERATECIS-VE",Lg.err("Validation Exception getting content instances for successfully located channel",ve),ve);
        } catch (ApplicationException ae) {
            throw new SourceRetrievalException("V7SubChSRC-ITERATECIS-AE",Lg.err("Application Exception getting content instances for successfully located channel",ae),ae);
        }
                 
    }
    

    // wrap ChannelUtils calls with an initialized base channel and a guid channel cache
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
    
    // filtration hook for custom analysis in extended sources: content types, metadata values, etc.
    protected List filterInstances(List instances, ContentRequest contentrequest, Channel channel)
    {
        if (allowableContentTypesFilter != null) {
            List filteredInstances = new ArrayList();
            for (Object cio : instances) {
                ContentInstance ci = (ContentInstance)cio;
                try { 
                    if (allowableContentTypesFilter.contains(ci.getObjectType().getName())) {
                        filteredInstances.add(ci);
                    }
                } catch (ApplicationException ae) {
                    String ciname = "<unknown>";
                    try{ ciname = ci.getName(); } catch (Exception e) {}
                    throw new SourceRetrievalException("V7SubChSRC-FILTERCIS-AE",Lg.err("Application Exception getting filtering content instances by type for contentInstance %s",ciname,ae),ae);
                }
            }
            return filteredInstances;
        }
        
        return instances;
    }

    // final processing hook for custom analysis in extended sources: content types, metadata values, etc.
    protected BaseContentResponse processResponse(ContentRequest contentrequest,BaseContentResponse contentresponse,Channel channel,List contentinstances)
    {
        // do guid decodes
        if (guidDecodeAttributes != null) {
            if (contentresponse.isMultipart()) {
                List<ContentResponse> multiparts = contentresponse.getMultipartResponse();
                for (ContentResponse item : multiparts)
                {
                    Map<String,ContentMetaData> itemmeta = item.getMetaData();
                    if (itemmeta != null) {
                        for (String key : itemmeta.keySet()) {
                            if (guidDecodeAttributes.contains(key)) {
                                AttributeUtils.decodeGuidMetaData((BaseContentMetaData)itemmeta.get(key));
                            }
                        }
                    }
                }
            }
        }
       
        
        return contentresponse;
    }


    // ---- property getter/setters
    public String getBaseChannel() { return baseChannel; }
    public void setBaseChannel(String baseChannelPath) { this.baseChannel = baseChannelPath; }
    public void setTypeSpecificContentAttributes(Map<String, String> typeattrmap) { this.typeSpecificContentAttributes = typeattrmap;}
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
    

    public Set<String> getGuidDecodeAttributes()
    {
        return guidDecodeAttributes;
    }


    public void setGuidDecodeAttributes(Set<String> guidDecodeAttributes)
    {
        this.guidDecodeAttributes = guidDecodeAttributes;
    }

    public Set<String> getAllowableContentTypesFilter()
    {
        return allowableContentTypesFilter;
    }


    public void setAllowableContentTypesFilter(Set<String> contentTypeFilter)
    {
        this.allowableContentTypesFilter = contentTypeFilter;
    }

}
