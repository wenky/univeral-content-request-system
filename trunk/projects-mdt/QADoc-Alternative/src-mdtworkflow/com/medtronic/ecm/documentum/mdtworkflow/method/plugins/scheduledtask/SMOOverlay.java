package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.scheduledtask;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.PdfOverlayProcessing;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtScheduledAction;

public class SMOOverlay implements IMdtScheduledAction
{

    public void execute(IDfSessionManager sessionmgr, String docbase,
            String mdtapp, IDfSysObject formobj, List attachments,
            IDfSysObject jobobject, Map methodparameters, Map context) 
    {
        IDfSession session = null;
        try {
            /*-WARN-*/Lg.wrn("top, acquire session");
            session = sessionmgr.getSession(docbase);
            Map config = (Map)context;
            String templatename = (String)config.get("TemplatePath");
            String formatname = (String)config.get("Format");
            
            /*-WARN-*/Lg.wrn("overlay on psidoc pdf from template %s to format %s",templatename,formatname);
            PdfOverlayProcessing.doSimpleOverlay(session,formobj, templatename,formatname);
            
            for (int i=0; i < attachments.size(); i++) {
                IDfSysObject attachdoc = (IDfSysObject)attachments.get(i);
                /*-WARN-*/Lg.wrn("overlay on attachment doc %s",attachdoc);
                PdfOverlayProcessing.doSimpleOverlay(session,attachdoc, templatename,formatname);
            }
            // proactive release
            sessionmgr.release(session); session = null;
        } catch (Exception dfe) {
            /*-ERROR-*/Lg.err("Error occurred in promotion of attachment",dfe);
            throw EEx.create("JobPromote","Error in method plugin processing",dfe);
        } finally {
            /*-dbg-*/Lg.wrn("releasing session");
            try {if (session!=null)sessionmgr.release(session);} catch(Exception e) {}
            /*-dbg-*/Lg.wrn("session released");
        }

        
    }

}
