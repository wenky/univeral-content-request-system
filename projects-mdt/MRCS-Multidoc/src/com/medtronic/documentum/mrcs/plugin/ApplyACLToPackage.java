package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class ApplyACLToPackage implements IMrcsWorkflowServerPlugin {

	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{
       	/*-CONFIG-*/String m="ApplyACLToPackage.execute(WF) - ";
       	IDfCollection packages = null;
       	IDfCollection attachments = null;
		try { 
			// queue an informational event in workflow supervisor's inbox and send an email too
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get event name" , null, null);
	       	String eventname = (String)config.get("EventName");
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- event name: "+eventname , null, null);
	
	       	String supervisor = workflow.getSupervisorName();
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- supervisor: "+supervisor , null, null);
	       	
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get message" , null, null);
	       	String message = (String)config.get("Message");
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- message: "+message, null, null);
	       		       	
	       	// get ACL
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get ACL from plugin config" , null, null);
	       	String aclname = (String)config.get("ACL");
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- ACL: "+aclname , null, null);
	       	
	       	// getfirstattachment
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterate through packages" , null, null);
	        IDfSession session = task.getSession();
            String systemdomain = session.getServerConfig().getString("operator_name");
            IDfACL newACL = session.getACL(systemdomain, aclname);
			packages = task.getPackages("");
			IDfSysObject pkg = null;
			if (packages.next())
			{
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if there are packages in the r_component_id multivalue attr" , null, null);
				for (int c=0; c < packages.getValueCount("r_component_id"); c++)
				{
			        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting the first component id" , null, null);
					IDfId id = packages.getRepeatingId("r_component_id",c);
			        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retrieving component "+id , null, null);
					pkg = (IDfSysObject)session.getObject(id);
			        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"applying ACL" , null, null);
			        pkg.setACL(newACL);
			        pkg.save();
			        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"applyied" , null, null);
					
				}
			}
	       	
		} catch (Exception e) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in queue event" , null, e);
			throw new RuntimeException("Error in workflow plugin - queue event plugin",e);
		} finally {
			try { 
		    	if (packages != null)
		    		packages.close();
			} catch (DfException dfe) {
				// do nothing...
			}
		}

	}


}
