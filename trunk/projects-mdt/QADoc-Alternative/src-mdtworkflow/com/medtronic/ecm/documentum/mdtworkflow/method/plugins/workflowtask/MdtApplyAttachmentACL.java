package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

public class MdtApplyAttachmentACL extends MdtProcessAttachments
{
    // ?transaction safe?
    public void processAttachment(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc, IDfSysObject attachment, Map context)
    {
        IDfSession session = null;
        try { 
            session = sessionmgr.getSession(docbase);
            /*-INFO-*/Lg.inf("Apply ACL on %s",attachment);
            String aclname = (String)context.get("ACL");
            /*-dbg-*/Lg.dbg("lookup acl %s",aclname);
            IDfTypedObject serverConfig = session.getServerConfig();
            String aclDomain = serverConfig.getString("operator_name");
            IDfACL pSet = session.getACL(aclDomain, aclname);
            /*-dbg-*/Lg.dbg("ACL retrieved successfully");
            attachment.setACL(pSet);
            /*-dbg-*/Lg.dbg("ACL applied successfully");
            attachment.save();
            /*-dbg-*/Lg.dbg("ACL change saved");            
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while rollback/destroying current version of attachment document %s",attachment,dfe);
            throw EEx.create("RollbackAttachment","error while rollback/destroying current version of attachment document %s",attachment,dfe);          
        } finally {
            
        }
        
    }   

}

