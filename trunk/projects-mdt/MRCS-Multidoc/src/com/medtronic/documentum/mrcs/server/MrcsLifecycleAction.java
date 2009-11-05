package com.medtronic.documentum.mrcs.server;

import java.util.List;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.lifecycle.IDfLifecycleUserAction;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

//this will execute inside the state change transaction

public class MrcsLifecycleAction extends MrcsLifecyclePluginLayer implements IDfLifecycleUserAction {

	public void userAction(IDfSysObject obj, String username, String targetstate) throws DfException 
	{
		/*-CONFIG-*/String m="userAction-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"UserAction hook invoked, executing Mrcs server plugin layer plugins",null,null);
		executeActionPlugins(obj,username,targetstate);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"plugin execution complete, returning",null,null);
	}
	
	// the list retrieved will depend on if we are looking up the entry, user, or post action's plugins
	// - 
	public List getLifecycleActionPluginList(String mrcsapp, String docconfig, String gfconfig, String lifecycle, String targetstate) throws DfException
	{
		/*-CONFIG-*/String m="getLifecycleActionPluginList-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Loading StateTransitionConfig",null,null);
		StateTransitionConfigFactory config =  StateTransitionConfigFactory.getSTConfig();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Getting lifecycle server plugin list for user actions",null,null);
		List pluginlist = config.getLifecycleActionPluginList(mrcsapp, docconfig, gfconfig, lifecycle, targetstate);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Number of plugins: "+pluginlist.size(),null,null);
		return pluginlist;
	}

}
