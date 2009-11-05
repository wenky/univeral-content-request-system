package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

// simple testing utility class to throw an exception
// -- used to test LC and WF promotes and rollbacks

public class WaaaaThrowException implements IMrcsLifecyclePlugin {
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
        /*-CFG-*/String m="execute(new)-";        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"THROWING EXCEPTION",null,null);
        throw new RuntimeException ("Intentionally thrown exception by WaaaaThrowException plugin");
	}
}
