package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.MailUtils;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.VelocityExecute;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.documentum.util.DctmUtils;
import com.medtronic.ecm.documentum.util.VeloUtils;

public class QADocRejectSendMail implements IMdtWorkflowAction
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
            
            // formobj is likely destroyed/rolled back - reretrieve
            formobj = WorkflowUtils.getPrimaryPackage(session, workitem.getWorkflowId().getId());
            
            // ONE: Recipients
            MailUtils.addSupervisorToAddressSet(workitem,addresses);
            if (plugincfg.containsKey("QueueSupervisor")) { MailUtils.addSupervisorToAddressSet(workitem,addresses); }
            if (plugincfg.containsKey("QueueAssignedApprovers")) { MailUtils.addWorkflowApproversToAddressSet(workitem,addresses); }
            if (plugincfg.containsKey("QueueGroup")) { MailUtils.addEntityOrListOfEntitiesToAddressSet(session,plugincfg.get("QueueGroup"),addresses); }            
            if (plugincfg.containsKey("QueueFormAttributes")){MailUtils.addAttributesToAddressSet(formobj,addresses,(String)plugincfg.get("QueueFormAttributes"));}
            if (plugincfg.containsKey("QueueAttachmentAttributes")){MailUtils.addAttachmentAttributesToAddressSet(attachments,addresses,(String)plugincfg.get("QueueAttachmentAttributes"));}
            
            // TWO: create context for velocity
            VelocityContext vctx = VelocityExecute.createContext(
                    "session",session,
                    "form",formobj,
                    "attachments",attachments,
                    "workitem",workitem,
                    "methodparams",methodparameters,
                    "veloutils",new VeloUtils());            
            
            // THREE: Message body
            // --velocity template in DCTM
            String templatepath = (String)plugincfg.get("Template");
            String template = DctmUtils.loadFileContents(session, templatepath);
            String message = VelocityExecute.generateHTML(vctx, template);
            
            // FOUR: Send emails
            MailUtils.sendToAddresses(smtpServer, addresses, from, subject, message, authuser, authpass);
            
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("Error occurred in simple mail send",e);
        } finally {
            try {if (session!=null)sessionmgr.release(session);} catch(Exception e) {}
        }
    }
}
