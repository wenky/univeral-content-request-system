package com.medtronic.documentum.mrcs.server.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class QueueEvent implements IMrcsWorkflowServerPlugin
{
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{
       	/*-CONFIG-*/String m="QueueEvent.execute - ";
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
	       	
	       	Date d = new Date();
	       	SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
	       	String datestring = sdf.format(d);
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating idftime for datestring "+datestring, null, null);
	       	IDfTime t = new DfTime(datestring, "mm/dd/yyyy");
	       	
	       	// getfirstattachment
	       	IDfSysObject obj = getFirstAttachment(task);
	       	
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"queuing event" , null, null);
	       	obj.queue(supervisor,eventname,1,true,t,message);
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"event queued" , null, null);
		} catch (DfException dfe) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in queue event" , null, dfe);
			throw new RuntimeException("Error in workflow plugin - queue event plugin",dfe);
		}

	}

	public IDfSysObject getFirstAttachment(IDfWorkitem wi) throws DfException
	{
        /*-CONFIG-*/String m="getFirstAttachment - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterate through packages" , null, null);
        IDfSession session = wi.getSession();
		IDfCollection packages = wi.getPackages("");
		IDfSysObject pkg = null;
		if (packages.next())
		{
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if there are packages in the r_component_id multivalue attr" , null, null);
			if (packages.getValueCount("r_component_id") > 0)
			{
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting the first component id" , null, null);
				IDfId id = packages.getRepeatingId("r_component_id",0);
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retrieving component "+id , null, null);
				pkg = (IDfSysObject)session.getObject(id);
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"component: "+pkg , null, null);
			}
		}
		return pkg;
	}

}
