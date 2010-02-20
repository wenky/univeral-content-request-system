package com.uhg.ewp.common.gotcha.util.vignette7.introspect;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.vignette.as.client.common.RequestParameters;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.Site;
import com.vignette.as.client.javabean.StaticFile;


public class ContentList
{
    public String getContentList(HttpServletRequest req) throws Exception
    {
        String appbase = req.getContextPath(); // == /CRS for example
        StringBuffer resp = new StringBuffer();
        String basesite = req.getParameter("basesite");
        if (StringUtils.isEmpty(basesite)) {
            // get base Sites 
            List sitelist = Site.findAll().asList();
            for (int i=0; i < sitelist.size(); i++)
            {
                Site site = (Site)sitelist.get(i);
                resp.append("<a href='"+appbase+"/ContentList.jsp?basesite="+site.getName()+"'>Site :"+site.getName()+"</a><BR>");
            }
        } else {
            Site site = Site.findByName(basesite);
            Channel homechannel = site.getHomeChannel();
            resp.append("Site: "+site.getName()+"<BR>");
            resp.append("HomeChannel name: "+homechannel.getName()+"<BR>");
            
            // descend subchannel path
            resp.append("Current Path: ");
            List<String> paths = new ArrayList<String>();
            int pathnum = 0;
            String pathargs = "";
            String pathstr = "/"+basesite;
            while (req.getParameter("p"+pathnum)!= null) {
                paths.add(req.getParameter("p"+pathnum));
                pathargs += "&p"+pathnum+"="+req.getParameter("p"+pathnum);
                resp.append("/"+req.getParameter("p"+pathnum));
                pathstr += "/"+req.getParameter("p"+pathnum);
                pathnum++;
            }

            // navigate to subchannel
            Channel ch = homechannel;
            for (String chname : paths) {
               Channel[] subchs = ch.getAllSubchannels();
               for (Channel subch : subchs) {
                   if (chname.equals(subch.getName())) {
                       ch = subch;
                   }
               }
            }
            
            // list subchannels for current selected channel
            resp.append("<BR><hr>SubChannels<BR><hr>");
            Channel[] subchannels = ch.getAllSubchannels();
            // get list subchannel contents
            for (Channel subchannel : subchannels) {
                resp.append("<a href='"+appbase+"/ContentList.jsp?basesite="+site.getName()+pathargs+"&p"+pathnum+"="+subchannel.getName()+"'>SubChannel :"+subchannel.getName()+"</a><BR>");
            }
            
            // list content instances
            resp.append("<BR><hr>Content Instances<BR><hr>");
            RequestParameters reqParams = RequestParameters.getImmutableInstanceTopRelationOnly();
            List instances = ch.getContentInstances(reqParams).asList();
            {
                for (int i=0; i < instances.size(); i++) {
                    ContentInstance ci = (ContentInstance)instances.get(i);
                    resp.append("<a href='"+appbase+"/ContentDetail.jsp?guid="+ci.getContentManagementId().getId()+"&path="+pathstr+"'>"+ci.getName()+"</a><BR>");
                }
            }
            
            // list static files
            resp.append("<BR><hr>Static Files<BR><hr>");
            RequestParameters reqParamsSF = RequestParameters.getImmutableInstanceTopRelationOnly();
            List staticfiles = ch.getStaticFileManagedObjects(reqParamsSF).asList();
            {
                for (int i=0; i < staticfiles.size(); i++) {
                    StaticFile sf = (StaticFile)staticfiles.get(i);
                    resp.append("<a href='"+appbase+"/StaticFileDetail.jsp?guid="+sf.getContentManagementId().getId()+"&path="+pathstr+"'>"+sf.getName()+"</a><BR>");
                }
            }
            
        }
        
        
        return resp.toString();
    }

}
