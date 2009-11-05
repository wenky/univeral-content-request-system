package com.medtronic.documentum.mrcs.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.MrcsLifecycle;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

public abstract class MrcsLifecyclePluginLayer 
{
	// execute transactional actions
	public void executeActionPlugins(IDfSysObject obj, String username, String targetstate) throws DfException
	{
		/*-CONFIG-*/String m="executeActionPlugins-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top",null,null);
		// get IDfSession
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get session from IDfSysObject passed in",null,null);
		IDfSession session = obj.getSession();
		// locate mrcs config
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get document's mrcs attributes and lifecycle",null,null);
		String mrcsapp = obj.getString("mrcs_application");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- mrcs app: "+mrcsapp,null,null);
		String docconfig = obj.getString("mrcs_config");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- doc config: "+docconfig,null,null);
		String gfconfig = obj.getString("mrcs_folder_config");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- folder config: "+gfconfig,null,null);
		String lifecycle = obj.getPolicyName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- lifecycle system name: "+lifecycle,null,null);
        // get mrcs lifecycle state data
        StateTransitionConfigFactory stconfig = StateTransitionConfigFactory.getSTConfig();
        String mrcslc = stconfig.getMrcsLifecycleFromSystemLifecycleName(mrcsapp,docconfig,gfconfig,lifecycle);
        MrcsLifecycleState mrcstargetstate = stconfig.getLifecycleState(mrcsapp,mrcslc,targetstate);
		// get associated plugins
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting plugin list",null,null);
        List pluginlist = getLifecycleActionPluginList(mrcsapp,docconfig,gfconfig,lifecycle,targetstate);
        Map contextdata = new HashMap();
        // execute plugins
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterating plugin list",null,null);
	        for (int i=0; i < pluginlist.size(); i++)
	        {
	        	MrcsPlugin plugindef = (MrcsPlugin)pluginlist.get(i);
	        	IMrcsLifecyclePlugin lcplugin = (IMrcsLifecyclePlugin)Class.forName(plugindef.PluginClassName).newInstance();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing plugin "+plugindef.PluginClassName,null,null);
	        	lcplugin.execute(session.getSessionManager(), session.getDocbaseName(),mrcstargetstate,mrcsapp,obj,plugindef.PluginConfiguration,contextdata);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"plugin executed",null,null);
	        }
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this,m+"Error in plugin execution",null,e);
        	DfException dfe = new DfException(e); 
        	throw(dfe);
        }
	}
	
	public abstract List getLifecycleActionPluginList(String mrcsapp, String docconfig, String gfconfig, String lifecycle, String targetstate) throws DfException;	
}
