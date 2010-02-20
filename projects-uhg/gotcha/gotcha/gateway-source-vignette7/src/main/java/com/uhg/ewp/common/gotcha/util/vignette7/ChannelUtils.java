package com.uhg.ewp.common.gotcha.util.vignette7;

import org.apache.commons.lang.StringUtils;

import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.util.log.Lg;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.Site;

public class ChannelUtils
{

    public static Channel getChannelByGuid(String guid)
    {
        try { 
            Lg.trc("Channel direct lookup via guid %s",guid);
            Channel channel = (Channel)Channel.findByContentManagementId(new ManagedObjectVCMRef(guid));
            return channel;
        } catch (ApplicationException ae) {
            throw new SourceRetrievalException("V7CHUTILS-GETCIFROMBASE-AE",Lg.err("app error getting guid "+guid,ae),ae);
        } catch (ValidationException ve) {
            throw new SourceRetrievalException("V7CHUTILS-GETCIFROMBASE-VE",Lg.err("validation error getting guid "+guid,ve),ve);                
        }        
    }
    
    public static Channel getDirectDescendantSubchannel(Channel channel, String subchannelname)
    {
        try {
            Lg.trc("search direct descendant subchannels for %s",subchannelname);
            Channel[] subchs = channel.getAllSubchannels();
            for (Channel subch : subchs) {
                if (subchannelname.equals(subch.getName())) {
                    Lg.trc("found subchannel %s",subchannelname);
                    return subch;
                }
            }
            return null;
        } catch (ValidationException ve) {
            throw new SourceRetrievalException("V7CHUTILS-GETSUBCHANNEL-VE",Lg.err("Validation Exception on subchannelname "+subchannelname,ve),ve);
        } catch (ApplicationException ae) {
            throw new SourceRetrievalException("V7CHUTILS-GETSUBCHANNEL-AE",Lg.err("Application Exception on subchannelname "+subchannelname,ae),ae);
        }
        
    }

    public static Channel getHomeChannelForSite(String sitename) 
    {
        try { 
            Lg.trc("Get site named %s",sitename);
            Site basesite = Site.findByName(sitename);
            Lg.trc("Getting Home channel");
            Channel homechannel = basesite.getHomeChannel();
            Lg.trc("returning");
            return homechannel;
        } catch (ValidationException ve) {
            throw new SourceRetrievalException("V7CHUTILS-GETHOMECHANNEL-VE",Lg.err("Validation Exception",ve),ve);
        } catch (ApplicationException ae) {
            throw new SourceRetrievalException("V7CHUTILS-GETHOMECHANNEL-AE",Lg.err("Application Exception",ae),ae);
        }
    }


    public static Channel getRelativeSubchannel(Channel basechannel, String subchannelpath)
    {
        Channel channel = basechannel;
        
        if (StringUtils.isEmpty(subchannelpath)) {
            return basechannel;
        } else {
            if ("/".equals(subchannelpath)) {
                return basechannel;
            }
            
            Lg.trc("split subchannel path %s by '/'s",subchannelpath);
            if (subchannelpath.charAt(0)=='/') {
                subchannelpath = subchannelpath.substring(1);
            }

            String[] pathArr = subchannelpath.split("/");
        
            for (String subchannelname : pathArr)
            {
                channel = ChannelUtils.getDirectDescendantSubchannel(channel,subchannelname);
                if (channel == null)
                {
                    Lg.trc("Subchannel named %s not found for relative path %s",subchannelname,subchannelpath);
                    return null;
                }
            }
        }
            
        return channel;
    }    

    
    
}
