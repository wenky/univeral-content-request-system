package com.uhg.ewp.common.gotcha.util.vignette7;

import java.util.List;

import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.util.log.Lg;
import com.vignette.as.client.common.RequestParameters;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.StaticFile;

public class ContentInstanceUtils
{
    public static StaticFile getStaticFile(ContentInstance ci, String attrname) 
    {
        try {
            Object attrval = ci.getAttributeValue(attrname);
            String[] paths = { attrval.toString() };
            IPagingList pl = StaticFile.findByPlacementPath(paths, null);
            List sflist = null;
            if (pl != null) {
                sflist = pl.asList();
                StaticFile sfile = (StaticFile)sflist.get(0);
                return sfile;
            }
            return null;
        } catch (ValidationException ve) {
            throw new SourceRetrievalException("V7SRC-GETSTATIC-VE",Lg.err("Validation Exception getting static file via attribute "+attrname,ve),ve);
        } catch (ApplicationException ae) {
            throw new SourceRetrievalException("V7SRC-GETSTATIC-AE",Lg.err("Application Exception getting static file via attribute "+attrname,ae),ae);
        }

    }
    
    public static ContentInstance getContentInstanceFromChannel(Channel ch, String ciname)
    {
        try {

            RequestParameters reqParams = RequestParameters.getImmutableInstanceTopRelationOnly();
            List instances = ch.getContentInstances(reqParams).asList();
            for (int i=0; i < instances.size(); i++) {
                ContentInstance ci = (ContentInstance)instances.get(i);
                String curname = ci.getName();
                if (ciname.equals(curname)) {
                    // re-retrieve (otherwise relations attributes are unretrievable)
                    ci = (ContentInstance)ContentInstance.findByContentManagementId(ci.getContentManagementId());
                    return ci;
                }
            }
            return null;
        } catch (ValidationException ve) {
            throw new SourceRetrievalException("V7CHUTILS-GETCHANNELCI-VE",Lg.err("Validation Exception getting content instance "+ciname,ve),ve);
        } catch (ApplicationException ae) {
            throw new SourceRetrievalException("V7CHUTILS-GETCHANNELCI-AE",Lg.err("Application Exception getting content instance "+ciname,ae),ae);
        }
        
        
    }
    
}
