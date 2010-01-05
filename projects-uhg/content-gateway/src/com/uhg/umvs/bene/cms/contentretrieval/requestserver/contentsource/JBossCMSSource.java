package com.uhg.umvs.bene.cms.contentretrieval.requestserver.contentsource;

import java.io.BufferedInputStream;
import java.io.InputStream;
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

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.ContentResponse;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;

// gets content from the JBoss CMS (a Jackrabbit CMS). Should be run from a webapp inside default/deploy/portal-server.sar/portal-cms.sar
// otherwise the command factory doesn't seem to work very well...

// reverse engineered from the OOTB CMSPortlet (org.jboss.portal.core.cms.ui.CMSPortlet in the portal-core-cms-lib.jar)
//   -- the CMS bean ref is acquired in CMSPortlet via: CMS CMSService = (CMS)getPortletContext().getAttribute("CMS");
//   -- ...how did I figure out the MBean? Is this portable across all nodes? example in org.jboss.portal.core.cms.servlet.CMSExportServlet
//         but is that accessible in all load balancing nodes?
//   -- MBean is configured in server/default/deploy/jboss-portal.sar/portal-cms.sar/META-INF/jboss-service.xml 


public class JBossCMSSource implements ContentSource
{
    String cmsServiceObjectName = "portal:service=CMS";
    public void setCmsServiceObjectName(String cmsServiceObjectName){this.cmsServiceObjectName = cmsServiceObjectName;}
    
    CMS getCMSServiceReference()
    {
        try {
            MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
            CMS CMSService = (CMS)MBeanProxy.get(CMS.class, new ObjectName(cmsServiceObjectName), mbeanServer);
            return CMSService;
        } catch (MBeanProxyCreationException createerror) {
            throw new RuntimeException("JBossCMSSource:: could not create proxy/facade for CMS portal service name 'portal:service=CMS'",createerror);
        } catch (MalformedObjectNameException badname) {
            throw new RuntimeException("JBossCMSSource:: CMS portal service name 'portal:service=CMS' is malformed",badname);
        }        
    }

    public boolean hasContent(String contentItem, HttpServletRequest request)
    {
        CMS CMSService = getCMSServiceReference();
        
        Command existsCMD = CMSService.getCommandFactory().createItemExistsCommand(contentItem);
        Boolean exists = (Boolean)CMSService.execute(existsCMD);
        return exists.booleanValue();        
    }
    
    public ContentResponse getContent(String contentItem, HttpServletRequest request)
    {
        ContentResponse response = new ContentResponse();
        
        CMS CMSService = getCMSServiceReference();
        
        Command getCMD = CMSService.getCommandFactory().createFileGetCommand(contentItem, new Locale(CMSService.getDefaultLocale()));
        File file = (File)CMSService.execute(getCMD);
        Content content = file.getContent();
        String mimeType = content.getMimeType();
        if (mimeType != null) {
            response.setMimetype(mimeType);
        }
                    
        InputStream contentstream = new BufferedInputStream(content.getStream());
        response.setContent(contentstream);
        return response;
        
    }

}
