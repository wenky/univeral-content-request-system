package com.medtronic.ecm.documentum.core.plugins.lifecycle;

import groovy.lang.Binding;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtLifecycleAction;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.GroovyExecute;

public class MdtGroovyLifecycleAction implements IMdtLifecycleAction {

    public void execute(String mdtapp, IDfSysObject idfsysobject,
            String username, String targetstate, Map pluginconfig) 
    {
        String scriptpath = (String)pluginconfig.get("ScriptPath");
        try {
            IDfSession session = idfsysobject.getSession(); 
            Binding binding = GroovyExecute.createBinding(
                    "session",session,
                    "mdtapp",mdtapp,
                    "idfsysobject",idfsysobject,
                    "targetstate",targetstate,
                    "username",username,
                    "pluginconfig",pluginconfig);
            GroovyExecute.runScript(session, scriptpath, binding);
            
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("error in groovy LC action script execution...",e);
            throw EEx.create("GroovyExec", "groovy LC action script execution...", e);
        } 

        
    }

}
