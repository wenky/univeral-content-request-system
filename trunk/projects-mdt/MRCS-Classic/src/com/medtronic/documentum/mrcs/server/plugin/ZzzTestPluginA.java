package com.medtronic.documentum.mrcs.server.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

public class ZzzTestPluginA  implements IMrcsLifecyclePlugin 
{

	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject obj,Map config, Map context)
	{
		try {
			// log some info...
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - Entry of ZzzTestPluginA : " , null,null);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - Document id: "+obj.getObjectId().getId() , null,null);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - current state: "+obj.getCurrentStateName() , null,null);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - dump: "+obj.dump() , null,null);
	        
	        
		} catch (DfException dfe) {
	        /*-ERROR-*/DfLogger.error(this, " - error" , null,dfe);
			throw new RuntimeException("Error in ZzzTestPluginA",dfe);
		}
		
	}

}
