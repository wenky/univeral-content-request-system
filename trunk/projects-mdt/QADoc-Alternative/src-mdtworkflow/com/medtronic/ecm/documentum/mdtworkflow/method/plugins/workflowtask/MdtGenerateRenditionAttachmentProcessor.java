package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.util.DctmUtils;

public class MdtGenerateRenditionAttachmentProcessor extends MdtProcessAttachments
{
    
    public void processAttachment(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc, IDfSysObject attachment, Map cfgmap)
    {    
        /*-INFO-*/Lg.inf("MdtGenerateRendition - top");
        /*-dbg-*/Lg.dbg("get configured rendition format (default is pdf)");
        try { 
            String format = null;
            if (cfgmap != null ) {
                format = (String)cfgmap.get("RenditionFormat");
            }
            if (format == null || "".equals(format))
            {
                format = "pdf";
            }
    
            /*-dbg-*/Lg.dbg("check if doc already has rendition of format: %s",format);            
            if (!DctmUtils.hasRendition(attachment.getSession(),attachment.getObjectId().getId(),format)) 
            {
                /*-dbg-*/Lg.dbg("need to generate a rendition");            
                DctmUtils.makeRenditionRequest(attachment,format);
                /*-dbg-*/Lg.dbg("saving attachment");
                attachment.save();
            }
            /*-dbg-*/Lg.dbg("done");
        } catch (DfException e) {
            /*-ERROR-*/Lg.err("Error encountered requesting rendition for attachment %s",attachment,e);
            throw EEx.create("RenderAttachment","Error encountered requesting rendition for attachment %s",attachment,e);
        }
    }
    

}
