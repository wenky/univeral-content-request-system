package com.uhg.ewp.common.gotcha.contentsource.impl.jbosscms;

import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanProxyCreationException;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.portal.cms.CMS;
import org.jboss.portal.cms.Command;
import org.jboss.portal.cms.model.Content;
import org.jboss.portal.cms.model.File;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceAccessException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.log.Lg;



// gets content from the JBoss CMS (a Jackrabbit CMS). Should be run from a webapp inside default/deploy/portal-server.sar/portal-cms.sar
// otherwise the command factory doesn't seem to work very well...

// reverse engineered from the OOTB CMSPortlet (org.jboss.portal.core.cms.ui.CMSPortlet in the portal-core-cms-lib.jar)
//   -- the CMS bean ref is acquired in CMSPortlet via: CMS CMSService = (CMS)getPortletContext().getAttribute("CMS");
//   -- ...how did I figure out the MBean? Is this portable across all nodes? example in org.jboss.portal.core.cms.servlet.CMSExportServlet
//         but is that accessible in all load balancing nodes?
//   -- MBean is configured in server/default/deploy/jboss-portal.sar/portal-cms.sar/META-INF/jboss-service.xml 


public class JBossCMSSource implements ContentSource
{
    //set
    String sourceName = "JBossCMS";
    
    //set
    String baseroot = "";

    //set
    String cmsServiceObjectName = "portal:service=CMS";

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public void setBaseroot(String baseroot) { this.baseroot = baseroot; }
    public void setCmsServiceObjectName(String cmsServiceObjectName){this.cmsServiceObjectName = cmsServiceObjectName;}
    
    // TODO: pooling?
    CMS getCMSServiceReference()
    {
        try {
            MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
            CMS CMSService = (CMS)MBeanProxy.get(CMS.class, new ObjectName(cmsServiceObjectName), mbeanServer);
            return CMSService;
        } catch (MBeanProxyCreationException createerror) {
            throw new SourceAccessException("JBOSSCMSSRC-MBEAN-PROXY",Lg.err("Could not create proxy/facade for CMS portal service name 'portal:service=CMS'",createerror),createerror);
        } catch (MalformedObjectNameException badname) {
            throw new SourceAccessException("JBOSSCMSSRC-MBEAN-BADNAME",Lg.err("CMS portal service name 'portal:service=CMS' is malformed",badname),badname);
        }        
    }

    // not used, but the ItemExistsCommand is too useful to get rid of, may need it in future.
    // may want to make this a precursor to the FileGetCommand, depends on performance...
    public boolean hasContent(String contentItem, HttpServletRequest request)
    {
        CMS CMSService = getCMSServiceReference();
        
        Command existsCMD = CMSService.getCommandFactory().createItemExistsCommand(contentItem);
        Boolean exists = (Boolean)CMSService.execute(existsCMD);
        return exists.booleanValue();        
    }
    
    public ContentResponse getContent(ContentRequest req)
    {
        BaseContentResponse response = new BaseContentResponse();
        
        CMS CMSService = getCMSServiceReference();
        
        String contentItem = req.getContentItem();
        
        String fullpath = baseroot + contentItem;
        
        response.setSourceName(getSourceName());
        response.setSourceId(fullpath);
        response.setItemRef(contentItem);
        
        
        Command getCMD = CMSService.getCommandFactory().createFileGetCommand(contentItem, new Locale(CMSService.getDefaultLocale()));
        File file = (File)CMSService.execute(getCMD);
        if (file != null) {
            Content content = file.getContent();
            response.setFound(true);
            //String version = file.getVersionUUID();
            Date date = file.getLastModified();
            if (date != null) {
                Long lastmodified = date.getTime();
                response.setLastModified(lastmodified);
            }
            
            long size = file.getSize();
            response.setSize(size);
            
            String mimeType = content.getMimeType();
            if (mimeType != null) {
                response.setMimetype(mimeType);
            }

            InputStream stream = content.getStream();
            response.setContent(stream);
            
            return response;
        }
        
        // return not found...
        response.setFound(false);
        return response;
        
    }

}
