package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.scheduledtask;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.MailUtils;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.VelocityExecute;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtScheduledAction;
import com.medtronic.ecm.documentum.util.DctmUtils;
import com.medtronic.ecm.documentum.util.VeloUtils;

public class QADocScheduledSendMail implements IMdtScheduledAction {

    public void execute(IDfSessionManager sessionmgr, String docbase,
            String mdtapp, IDfSysObject formobj, List attachments,
            IDfSysObject jobobject, Map methodparameters, Map plugincfg)
    {
        String smtpServer=(String)plugincfg.get("SmtpServer"); 
        String from=(String)plugincfg.get("From");
        String subject=(String)plugincfg.get("Subject");
        String authuser=(String)plugincfg.get("AuthUser");
        String authpass=(String)plugincfg.get("AuthPass");
        IDfSession session = null;
        try {
            session = sessionmgr.getSession(docbase);
            Set addresses = new HashSet();
            
            // ONE: Get recipient set 
            // --group, user, or list of groups and users specified in plugin config
            if (plugincfg.containsKey("QueueGroup")) { MailUtils.addEntityOrListOfEntitiesToAddressSet(session,plugincfg.get("QueueGroup"),addresses); }
            if (plugincfg.containsKey("QueueFormAttributes")){MailUtils.addAttributesToAddressSet(formobj,addresses,(String)plugincfg.get("QueueFormAttributes"));}
            if (plugincfg.containsKey("QueueAttachmentAttributes")) { MailUtils.addAttachmentAttributesToAddressSet(attachments,addresses,(String)plugincfg.get("QueueAttachmentAttributes")); }
            
            // TWO: create context for velocity
            VelocityContext vctx = VelocityExecute.createContext(
                    "session",session,
                    "form",formobj,
                    "attachments",attachments,
                    "job",jobobject,
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
