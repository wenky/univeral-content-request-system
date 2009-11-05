package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

public class SMOSetTargetApprovalDate extends MdtProcessAttachments
{
    public void processAttachment(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc, IDfSysObject attachment, Map context)
    {
        IDfSession session = null;
        try { 
            session = sessionmgr.getSession(docbase);
            /*-INFO-*/Lg.inf("Apply ACL on %s",attachment);
            String formdateattr = (String)context.get("FormDateAttrName");
            String attachmentdateattr = (String)context.get("AttachmentDateAttrName");
            /*-dbg-*/Lg.dbg("write %s to %s",formdateattr,attachmentdateattr);
            attachment.setTime(attachmentdateattr,controldoc.getTime(formdateattr));
            /*-dbg-*/Lg.dbg("Date set successfully");
            attachment.save();
            /*-dbg-*/Lg.dbg("Date change saved");            
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error setting target date on %s",attachment,dfe);
            throw EEx.create("SetTargetDate","error setting target date on %s",attachment,dfe);          
        } finally {
            
        }
        
    }   

}

