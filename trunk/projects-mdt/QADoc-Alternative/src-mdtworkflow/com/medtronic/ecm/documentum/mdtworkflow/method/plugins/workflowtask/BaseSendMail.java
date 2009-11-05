package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;

public class BaseSendMail implements IMdtWorkflowAction
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map plugincfg)
    {
        // simplest case: send email to a list of users/groups (can be DCTM user, DCTM group, or hardcoded address)
        String smtpServer=(String)plugincfg.get("SmtpServer"); 
        String from=(String)plugincfg.get("From");
        String subject=(String)plugincfg.get("Subject");
        String authuser=(String)plugincfg.get("AuthUser");
        String authpass=(String)plugincfg.get("AuthPass");
        IDfSession session = null;
        try {
            session = sessionmgr.getSession(docbase);
            Set addresses = new HashSet();
            
            // ONE: Recipients
            // TWO: Template Context
            // THREE: Generate Message
            // FOUR: Send Message to Recipients
            
            
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("Error occurred in simple mail send",e);
            throw EEx.create("MdtMsg-exec","Error occurred in simple mail send",e);            
        } finally {
            try {if (session!=null)sessionmgr.release(session);} catch(Exception e) {}
        }
        
        
    }
}
