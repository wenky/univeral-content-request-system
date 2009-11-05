package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;

public abstract class MdtProcessAttachments implements IMdtWorkflowAction {

    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map pluginconfig)
    {
        try {
            boolean bypassrecent = false;
            if (pluginconfig != null && pluginconfig.containsKey("BypassRecent"));            
            for (int i=0 ; i < attachments.size(); i++) 
            {                
                /*-dbg-*/Lg.wrn("--NEXT ATTACHMENT--");
                IDfSysObject attacheddoc = (IDfSysObject)attachments.get(i);                
                if (!bypassrecent) {
                    attacheddoc = AttachmentUtils.getMostRecent(attacheddoc);                
                    formobj = AttachmentUtils.getMostRecent(formobj);                
                }
                /*-dbg-*/Lg.wrn("process attachment");
                processAttachment(sessionmgr, docbase, mdtapp, workitem, formobj, attacheddoc, pluginconfig);                      
                /*-dbg-*/Lg.wrn("attachment processors done");
            }
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("error in processing workitem attachments...",e);
            throw EEx.create("procAtchmnts", "error in processing workitem attachments...", e);
        } 
        
    }
    
    public abstract void processAttachment(IDfSessionManager smgr, String docbase, String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc, IDfSysObject attachment, Map context);
    


}
