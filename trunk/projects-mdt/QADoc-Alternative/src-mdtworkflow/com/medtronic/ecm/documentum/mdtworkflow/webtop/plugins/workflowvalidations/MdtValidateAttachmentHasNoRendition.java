package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowAttachmentValidation;

public class MdtValidateAttachmentHasNoRendition implements IMdtWorkflowAttachmentValidation 
{

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, IDfSysObject attachment, List errors, Map context) 
    {
        // get format from config
        /*-dbg-*/Lg.dbg("plugin top");
        Map cfg = (Map)context;
        String format;
        /*-dbg-*/Lg.dbg("get format");
        if (cfg !=null && cfg.containsKey("Format"))
        {
            format = (String)cfg.get("Format");
            /*-dbg-*/Lg.dbg("format: %s",format);
        } else {
            /*-dbg-*/Lg.dbg("defaulting to pdf");
            format = "pdf";
        }
        
        /*-dbg-*/Lg.dbg("check for rendition on %s",attachment);
        try { 
            if (WorkflowUtils.checkRenditions(attachment,format)) {
                /*-dbg-*/Lg.dbg("rendition detected on attachment %s, return validation failure",attachment);
                if (context!=null && ((Map)context).containsKey("HasRenditionError")) {
                    errors.add(MdtErrorService.renderErrorMessage("HasRenditionError",(Map)context,mgr,docbase,formobj,attachment,new ErrKey("format",format)));
                } else {
                    errors.add("Document "+attachment.getObjectName()+" has a preexisting rendition of format "+format+" - modify the document to assure it is unrendered");
                }
                return false;
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking for rendition of format %s on %s",format,attachment,dfe);
            throw EEx.create("WFNoRendtion-DFE","Error checking for rendition of format %s on %s",format,attachment,dfe);
        }
        
        /*-dbg-*/Lg.dbg("no rendition detected, returning true");
        return true;
    }
    
    

}
