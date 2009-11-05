package com.medtronic.ecm.documentum.common;

import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfServiceException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

/**
 * TODO: ADD DESCRIPTION
 * 
 * @author $Author: dentrf1 $
 * 
 * @version $Revision: 1.7 $
 *
 */
public class MdtPluginLoader {

	/**
     * TODO: ADD DESCRIPTION
     *   
     * @param plugin <font color="#0000FF"><b>(MdtPlugin)</b></font> TODO: 
     * @param smgr <font color="#0000FF"><b>(IDfSessionManager)</b></font> TODO:
     * 
     * @return <font color="#0000FF"><b>Object</b></font> - TODO:
     */    
	public static Object loadPlugin(MdtPlugin plugin, IDfSessionManager smgr) throws DfException
	{
		/*-INFO-*/Lg.inf("load plugin class %s",plugin == null ? null : plugin.classname,plugin == null ? null : plugin.invocationtype);		
		if (plugin.invocationtype == null || "Pojo".equalsIgnoreCase(plugin.invocationtype))
		{
			try { 
				/*-dbg-*/Lg.dbg("pojo load");
				Object instance = Class.forName(plugin.classname).newInstance();
				/*-dbg-*/Lg.dbg("load success");
				return instance;
			} catch (ClassNotFoundException cnfe) {
				/*-ERROR-*/Lg.err("Class %s not found", plugin.classname,cnfe);
				throw EEx.create("LoadPlugin-POJO-CNFE", "Class %s not found", plugin.classname,cnfe);
			} catch (InstantiationException ie) {
				/*-ERROR-*/Lg.err("Instantiation error on class %s", plugin.classname,ie);
				throw EEx.create("LoadPlugin-POJO-InstE", "Instantiation error on class %s", plugin.classname,ie);				
			} catch (IllegalAccessException iae) {
				/*-ERROR-*/Lg.err("Illegal Access error on class %s", plugin.classname,iae);
				throw EEx.create("LoadPlugin-POJO-NoAccess", "Illegal Access error on class %s", plugin.classname,iae);
			}
		} else if ("SBO".equalsIgnoreCase(plugin.invocationtype)) {
			/*-dbg-*/Lg.dbg("sbo load - get dfclient");
			IDfClient dctmclient = DfClient.getLocalClient();
			try { 
				/*-dbg-*/Lg.dbg("create sbo");
				Object sboobj = dctmclient.newService(plugin.classname,smgr);
				/*-dbg-*/Lg.dbg("sbo load success");
				return sboobj;
			} catch (DfServiceException dse) {
				throw EEx.create("LoadPlugin-SBO-ServiceErr", "DfService exception on SBO class %s", plugin.classname,dse);
			}
		}
		/*-dbg-*/Lg.dbg("unknown plugin invocation/load method, returning null");		
		return null;
	}
	
	
		
	/**
     * I placed this here since plugins seem to be the main code types that need superuser promotion
     *   
     * @param session <font color="#0000FF"><b>(IDfSession)</b></font> TODO: 
     * @param application <font color="#0000FF"><b>(String)</b></font> TODO:
     * @throws DfException
     * @return <font color="#0000FF"><b>IDfLoginInfo</b></font> - TODO:
     */    
    public static IDfLoginInfo getApplicationSystemUser(IDfSession session, String application) throws DfException
    {
		/*-INFO-*/Lg.inf("get system user for app %s",application);
		String qual = MdtConfigService.CONFIG_SERVICE_SEARCH_QUALIFICATION+application+"'";
		/*-trc-*/Lg.trc("config obj qualification: %s",qual);
		IDfDocument serviceconfiguration = (IDfDocument)session.getObjectByQualification(qual);
		/*-trc-*/Lg.trc("config obj returned %s: ",serviceconfiguration);
		String user = serviceconfiguration.getString("m_zzz_uid");
		String pass = serviceconfiguration.getString("m_zzz_pcred");
		/*-trc-*/Lg.trc("make login info");
		IDfLoginInfo logininfo = new DfLoginInfo();
		logininfo.setUser(user);
		logininfo.setPassword(pass);
		/*-trc-*/Lg.trc("Returning");
		return logininfo;
    	
    }
    
    /**
     * TODO: ADD DESCRIPTION
     *   
     * @param session <font color="#0000FF"><b>(IDfSession)</b></font> TODO: 
     * @param mdtapp <font color="#0000FF"><b>(String)</b></font> TODO:
     * @throws DfException
     * @return <font color="#0000FF"><b>IDfSessionManager</b></font> - TODO:
     */    
    public static IDfSessionManager getSystemSessionManager(IDfSession session, String mdtapp) throws DfException
    {
    	IDfSessionManager sysmgr = session.getClient().newSessionManager();
    	IDfLoginInfo syslogin = getApplicationSystemUser(session,mdtapp);
    	sysmgr.setIdentity(session.getDocbaseName(),syslogin);
    	return sysmgr;
    }
    
    /**
     * I placed this here since plugins seem to be the main code types that need superuser promotion
     *   
     * @param pluginconfig <font color="#0000FF"><b>(Object)</b></font> TODO: 
     * @return <font color="#0000FF"><b>boolean</b></font> - TODO:
     */   
    public static boolean runAsSystem(Map pluginconfig)
    {
    	try { 
	    	if (pluginconfig instanceof Map)
	    	{
	    		Map cfgmap = pluginconfig;
	    		if (cfgmap.containsKey("SystemUser"))
	    		{
	    			return true;
	    		}
	    	}
    	} catch (Exception e) {
    		return false;
    	}
    	return false;
    }

}
