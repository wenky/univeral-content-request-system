package com.medtronic.ecm.documentum.core.lifecycle.impl;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.core.lifecycle.IMdtConfigurableLifecycleAction;
import com.medtronic.ecm.documentum.core.plugins.IMdtLifecycleAction;

/**
 * TODO: Add description
 *
 * @author $Author: dms01 $
 * @version $Revision: 1.1 $
 *
 */
public class MdtConfigurableLifecycleAction extends DfService implements IMdtConfigurableLifecycleAction
{ // may need to extend/impl IDfService as well

	/**
    *
    * TODO: ADD DESCRIPTION
    *
    * @param idfsysobject <font color="#0000FF"><b>(IDfSysObject)</b></font> TODO:
    * @param username <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param targetstate <font color="#0000FF"><b>(String)</b></font> TODO:
    * @throws DfException
    *
    * @since 1.0
    *
    */
	public void userAction(IDfSysObject idfsysobject, String username, String targetstate) throws DfException
	{
        /*-INFO-*/Lg.inf("execute custom user lifecycle actions");
        /*-dbg-*/Lg.dbg("get mdt app for obj: %s",idfsysobject);
		String mdtapp = idfsysobject.getString("m_application");
        /*-dbg-*/Lg.dbg("get cfgservice");
		MdtConfigService cfgsvc = MdtConfigService.getConfigService(idfsysobject.getSessionManager(), idfsysobject.getSession().getDocbaseName());
        /*-dbg-*/Lg.dbg("get config from cfgsvc for mdtapp: %s",mdtapp);
		Map cfgdata = (Map)cfgsvc.getAppConfig(mdtapp);
        /*-dbg-*/Lg.dbg("get lc name");
		String lcname = idfsysobject.getPolicyName();
		Map state = null;
		try {
	        /*-dbg-*/Lg.dbg("get state config for lifecycle %s state %s",lcname, targetstate);
			state = getCurrentStateConfig(cfgdata,lcname,targetstate);
		} catch (NullPointerException npe) {
	        /*-ERROR-*/Lg.err("config not specified or error in config",npe);
			throw EEx.create("LC-userAction","config not specified or error in config",npe);
		}

        /*-dbg-*/Lg.dbg("get action plugins");
		List actionplugins = (List)state.get("ActionPlugins"); //get List of MdtPlugins
		for (int j=0; j < actionplugins.size(); j++)
		{
			try {
		        /*-dbg-*/Lg.dbg("run plugin #%d",j);
				MdtPlugin plugin = (MdtPlugin)actionplugins.get(j);
				/*-dbg-*/ if (Lg.dbg()) try {Lg.dbg("Loading Plugin: %s", plugin.classname);} catch (Exception e) {}
				IMdtLifecycleAction task = (IMdtLifecycleAction)MdtPluginLoader.loadPlugin(plugin, idfsysobject.getSessionManager());
		        /*-dbg-*/Lg.dbg("execute");
				task.execute(mdtapp,idfsysobject,username,targetstate,plugin.context);
		        /*-dbg-*/Lg.dbg("...done");	        
			} catch (DfException dfe) {
				// TODO: handle exception
		        /*-ERROR-*/Lg.err("Lifecycle Action plugin failed",dfe);
				throw EEx.create("LC-Action","Lifecycle Action plugin failed",dfe);				
			}		
		}
	}


	/**
    *
    * TODO: ADD DESCRIPTION
    *
    * @param idfsysobject <font color="#0000FF"><b>(IDfSysObject)</b></font> TODO:
    * @param username <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param targetstate <font color="#0000FF"><b>(String)</b></font> TODO:
    * @throws DfException
    *
    * @since 1.0
    *
    */
	public void userPostProcessing(IDfSysObject idfsysobject, String username, String targetstate) throws DfException
	{
        /*-INFO-*/Lg.inf("execute custom user lifecycle actions");
        /*-dbg-*/Lg.dbg("get mdt app for obj: %s",idfsysobject);
		String mdtapp = idfsysobject.getString("m_application");
        /*-dbg-*/Lg.dbg("get cfgservice");
		MdtConfigService cfgsvc = MdtConfigService.getConfigService(idfsysobject.getSessionManager(), idfsysobject.getSession().getDocbaseName());
        /*-dbg-*/Lg.dbg("get config from cfgsvc for mdtapp: %s",mdtapp);
		Map cfgdata = (Map)cfgsvc.getAppConfig(mdtapp);
        /*-dbg-*/Lg.dbg("get lc name");
		String lcname = idfsysobject.getPolicyName();
		Map state = null;
		try {
	        /*-dbg-*/Lg.dbg("get state config for lifecycle %s state %s",lcname, targetstate);
			state = getCurrentStateConfig(cfgdata,lcname,targetstate);
		} catch (NullPointerException npe) {
	        /*-ERROR-*/Lg.err("config not specified or error in config",npe);
			throw EEx.create("LC-postprocess","config not specified or error in config",npe);
		}

        /*-dbg-*/Lg.dbg("get PostProcessing plugins");
		List actionplugins = (List)state.get("PostProcessingPlugins"); //get List of MdtPlugins
		for (int j=0; j < actionplugins.size(); j++)
		{
	        /*-dbg-*/Lg.dbg("run plugin #%d",j);
			MdtPlugin plugin = (MdtPlugin)actionplugins.get(j);
			/*-dbg-*/ if (Lg.dbg()) try {Lg.dbg("Loading Plugin: %s", plugin.classname);} catch (Exception e) {}
			IMdtLifecycleAction task = (IMdtLifecycleAction)MdtPluginLoader.loadPlugin(plugin, idfsysobject.getSessionManager());
	        /*-dbg-*/Lg.dbg("execute");
			task.execute(mdtapp,idfsysobject,username,targetstate,plugin.context);
	        /*-dbg-*/Lg.dbg("...done");
		}

	}


	/**
    *
    * TODO: ADD DESCRIPTION
    *
    * @param cfgdata <font color="#0000FF"><b>(Map)</b></font> TODO:
    * @param lcname <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param targetstate <font color="#0000FF"><b>(String)</b></font> TODO:
    * @return <font color="#0000FF"><b>Map</b></font> - TODO:
    *
    * @since 1.0
    *
    */
	public Map getCurrentStateConfig(Map cfgdata, String lcname, String targetstate)
	{
		// app["Lifecycles"][lcname]["States"][targetstate]
		Map lifecycles = (Map)cfgdata.get("Lifecycles");
		Map lifecycle = (Map)lifecycles.get(lcname);
		Map states = (Map)lifecycle.get("States");
		Map state = (Map)states.get(targetstate);
		return state;

	}

}
