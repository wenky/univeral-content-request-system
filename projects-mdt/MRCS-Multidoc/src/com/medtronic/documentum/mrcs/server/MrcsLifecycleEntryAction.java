package com.medtronic.documentum.mrcs.server;

import java.util.List;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.lifecycle.IDfLifecycleUserAction;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

// this will execute inside the state change transaction

public class MrcsLifecycleEntryAction extends MrcsLifecyclePluginLayer implements IDfLifecycleUserAction {

	public void userAction(IDfSysObject obj, String username, String targetstate) throws DfException 
	{
		executeActionPlugins(obj,username,targetstate);
	}
	
	// the list retrieved will depend on if we are looking up the entry, user, or post action's plugins
	// - 
	public List getLifecycleActionPluginList(String mrcsapp, String docconfig, String gfconfig, String lifecycle, String targetstate) throws DfException
	{
		StateTransitionConfigFactory config =  StateTransitionConfigFactory.getSTConfig();
		List pluginlist = config.getLifecycleEntryPluginList(mrcsapp, docconfig, gfconfig, lifecycle, targetstate);
		return pluginlist;
	}

}
