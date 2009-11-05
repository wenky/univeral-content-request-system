package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.job;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.MailUtils;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.VelocityExecute;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtJobAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.util.DctmUtils;
import com.medtronic.ecm.documentum.util.VeloUtils;

public class SMOOverdueSendMail implements IMdtJobAction
{

    public void execute(IDfSessionManager smgr, String docbase,
            String mdtapp, String taskname, 
            Map methodparameters, Map pluginconfig) 
    {
        IDfSession session = null;
        try { 
            session = smgr.getSession(docbase);
            
            // could be params too...
            String dql = (String)pluginconfig.get("DQL"); 
            String smtpServer=(String)pluginconfig.get("SmtpServer"); 
            String from=(String)pluginconfig.get("From");
            String subject=(String)pluginconfig.get("Subject");
            String authuser=(String)pluginconfig.get("AuthUser");
            String authpass=(String)pluginconfig.get("AuthPass");
            
            List delinquents = DctmUtils.execSingleColumnQuery(session, dql);
            for (int i=0; i < delinquents.size(); i++) {
                String id = (String)delinquents.get(i);
                IDfSysObject badboy = (IDfSysObject)session.getObject(new DfId(id));
                Set addresses = new HashSet();
                
                // mail message
                // ONE: Get recipient set 
                // --group, user, or list of groups and users specified in plugin config
                IDfWorkflow wf = AttachmentUtils.getAttachmentWorkflow(badboy);
                if (pluginconfig.containsKey("QueueSupervisor")) { MailUtils.addSupervisorToAddressSet(wf,addresses); }
                if (pluginconfig.containsKey("QueueAssignedApprovers")) { MailUtils.addWorkflowApproversToAddressSet(wf,addresses); }                
                if (pluginconfig.containsKey("QueueGroup")) { MailUtils.addEntityOrListOfEntitiesToAddressSet(session,pluginconfig.get("QueueGroup"),addresses); }
                if (pluginconfig.containsKey("QueueAttributes")){MailUtils.addAttributesToAddressSet(badboy,addresses,(String)pluginconfig.get("QueueAttributes"));}
                
                // TWO: create context for velocity
                VelocityContext vctx = VelocityExecute.createContext(
                        "session",session,
                        "idfsysobject",badboy,
                        "methodparams",methodparameters,
                        "veloutils",new VeloUtils());
                
                // THREE: Message body
                // --velocity template in DCTM
                String templatepath = (String)pluginconfig.get("Template");
                String template = DctmUtils.loadFileContents(session, templatepath);
                String message = VelocityExecute.generateHTML(vctx, template);
                
                // FOUR: Send emails
                MailUtils.sendToAddresses(smtpServer, addresses, from, subject, message, authuser, authpass);                
            }
        
            // execute query to find overdue approval documents
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error occurred in checking for overdue approval documents",dfe);
        } finally {
            /*-dbg-*/Lg.wrn("releasing session");
            try {smgr.release(session);} catch(Exception e) {}
            /*-dbg-*/Lg.wrn("session released");
        }
            
    }
    

}
