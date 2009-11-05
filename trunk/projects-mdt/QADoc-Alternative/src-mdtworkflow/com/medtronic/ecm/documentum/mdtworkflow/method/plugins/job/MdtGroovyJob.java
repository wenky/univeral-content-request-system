package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.job;

import groovy.lang.Binding;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.GroovyExecute;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtJobAction;

public class MdtGroovyJob implements IMdtJobAction
{

    public void execute(IDfSessionManager smgr, String docbase,
            String mdtapp, String taskname, 
            Map methodparameters, Map pluginconfig) 
    {
        String scriptpath = (String)pluginconfig.get("ScriptPath");
        IDfSession session = null;
        try {
            session = smgr.getSession(docbase);
            Binding binding = GroovyExecute.createBinding(
                    "session",session,
                    "mdtapp",mdtapp,
                    "methodparams",methodparameters,
                    "pluginconfig",pluginconfig);
            GroovyExecute.runScript(session, scriptpath, binding);
            
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error occurred in checking for overdue approval documents",dfe);
            throw EEx.create("MdtMsg-exec","Error occurred in checking for overdue approval documents",dfe);                        
        } finally {
            try {smgr.release(session);} catch(Exception e) {}
        }
    }

}
