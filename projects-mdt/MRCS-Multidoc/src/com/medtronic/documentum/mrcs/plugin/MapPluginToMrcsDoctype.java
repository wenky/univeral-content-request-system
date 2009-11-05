package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;


// this is a 4.2-only plugin that executes IMrcsLifecyclePlugins based on the mrcs doctype of the passed in document.
// this is useful for making flexible lifecycles that can work on documents in different grouping folder behaviors/contexts...
public class MapPluginToMrcsDoctype implements IMrcsLifecyclePlugin 
{

	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject obj,Map config, Map context)
	{
        /*-CFG-*/String m="execute-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"find out what the mrcs doctype is",null,null);
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check for mrcs sysobject (has mrcs_config)",null,null);
	        if (obj.hasAttr("mrcs_config"))
	        {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"is mrcs doc, get MRCS doctype",null,null);
	        	String mrcsdoctype = obj.getString("mrcs_config");
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting plugin mapping from plugin config for MRCS doctype "+mrcsdoctype,null,null);
	        	if (config.containsKey(mrcsdoctype))
	        	{
	        		MrcsPlugin plugin = (MrcsPlugin)config.get(mrcsdoctype);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing class "+plugin.PluginClassName,null,null);
	        		IMrcsLifecyclePlugin codetorun = (IMrcsLifecyclePlugin)Class.forName(plugin.PluginClassName).newInstance();
	        		codetorun.execute(sMgr,docbase,targetstate,mrcsapp,obj,plugin.PluginConfiguration,context);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executed, returning",null,null);
	        	} else throw new Exception("Unmapped doctype "+mrcsdoctype+" encountered in MapPluginToMrcsDoctype");
	        }
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"Exception thrown during CopyToSystemFolder plugin execution",e);
            throw new RuntimeException("Error in MapPluginToMrcsDoctype lifecycle plugin",e);
        }
		
	}

}
