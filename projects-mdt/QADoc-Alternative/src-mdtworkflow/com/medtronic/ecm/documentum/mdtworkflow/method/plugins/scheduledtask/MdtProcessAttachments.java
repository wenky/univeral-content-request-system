package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.scheduledtask;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtScheduledAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;

public abstract class MdtProcessAttachments implements IMdtScheduledAction {

    public void execute(IDfSessionManager sessionmgr, String docbase,
            String mdtapp, IDfSysObject formobj, List attachments,
            IDfSysObject jobobject, Map methodparameters, Map context)
    {
        try {
            boolean getrecent = (context != null ? context.containsKey("GetMostRecent") : false);            
            for (int i=0 ; i < attachments.size(); i++) 
            {                
                /*-dbg-*/Lg.wrn("--NEXT ATTACHMENT--");
                IDfSysObject attacheddoc = (IDfSysObject)attachments.get(i);                
                if (getrecent) attacheddoc = AttachmentUtils.getMostRecent(attacheddoc);                
                /*-dbg-*/Lg.wrn("process attachment");
                processAttachment(sessionmgr, docbase, mdtapp, jobobject, formobj, attacheddoc, methodparameters, context);                      
                /*-dbg-*/Lg.wrn("attachment processors done");
            }
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("error in processing workitem attachments...",e);
            throw EEx.create("procAtchmnts", "error in processing workitem attachments...", e);
        } 
        
    }
    
    public abstract void processAttachment(IDfSessionManager smgr, String docbase, String mdtapp, IDfSysObject jobobject, IDfSysObject formobj, IDfSysObject attachment, Map jobparameters, Map context);
    


}
