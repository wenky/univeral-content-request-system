package com.medtronic.documentum.mrcs.server;

import java.util.List;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.lifecycle.IDfLifecycleUserPostProcessing;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

// this will execute outside of the transaction

public class MrcsLifecyclePostAction extends MrcsLifecyclePluginLayer implements IDfLifecycleUserPostProcessing {

	
	public void userPostProcessing(IDfSysObject obj, String username, String targetstate) throws DfException 
	{
		/*-CONFIG-*/String m="userPostProcessing-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Post Action hook invoked, executing Mrcs server plugin layer plugins",null,null);
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
		List pluginlist = config.getLifecyclePostPluginList(mrcsapp, docconfig, gfconfig, lifecycle, targetstate);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Number of plugins: "+pluginlist.size(),null,null);
		return pluginlist;
	}

}
