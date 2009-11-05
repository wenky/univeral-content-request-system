package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import groovy.lang.Binding;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.GroovyExecute;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;

public class MdtGroovyWorkflowTask implements IMdtWorkflowAction
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map context)
    {
        IDfSession session = null;
        String scriptpath = (String)context.get("ScriptPath");
        try {
            session = sessionmgr.getSession(docbase);
            Binding binding = GroovyExecute.createBinding(
                    "session",session,
                    "mdtapp",mdtapp,
                    "form",formobj,
                    "attachments",attachments,
                    "workitem",workitem,
                    "methodparams",methodparameters,
                    "pluginconfig",context);
            GroovyExecute.runScript(session, scriptpath, binding);
            
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("error in groovy script execution...",e);
            throw EEx.create("GroovyExec", "groovy script execution...", e);
        } finally {
            try {if (session!=null)sessionmgr.release(session);} catch(Exception e) {}
        }
        
    }

}
