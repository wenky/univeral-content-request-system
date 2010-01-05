package com.uhg.umvs.bene.cms.contentretrieval.portlet;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class ContentAccessPortlet extends GenericPortlet
{

    @Override
    protected void doView(RenderRequest req, RenderResponse resp) throws PortletException,
            IOException
    {
        // get ref to portlet config
        
        // any server configuration will be stored in portlet.xml config 
        // content item is specified in corresponding portlet-instance.xml
        resp.setContentType("text/html");
        
    }
    
}
