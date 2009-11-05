package com.medtronic.documentum.mrcs.server.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.server.MrcsPromotionServiceModule;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class QADocFormQueueEvent implements IMrcsWorkflowServerPlugin, IMrcsLifecyclePlugin
{
	
	/* lifecycle exec */
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
       	/*-CONFIG-*/String m="QueueEvent.execute(LC) - ";
       	IDfSession session = null;
		try { 
			// queue an informational event in workflow supervisor's inbox and send an email too
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get event name" , null, null);
	       	String eventname = (String)config.get("EventName");
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- event name: "+eventname , null, null);
		       	
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get message" , null, null);
	       	String message = (String)config.get("Message");
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- message: "+message, null, null);
	       	
	       	Date d = new Date();
	       	SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
	       	String datestring = sdf.format(d);
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating idftime for datestring "+datestring, null, null);
	       	IDfTime t = new DfTime(datestring, "mm/dd/yyyy");
	       	
	       	// getfirstattachment
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterate through packages" , null, null);
	        session = sMgr.getSession(docbase);

	       	// now iterate through the m_dist_list attribute for additional subscribers
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterating through distribution list", null, null);
	       	for (int cc=0; cc < mrcsdocument.getValueCount("m_dist_list"); cc++)
	       	{
		       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"user #"+cc, null, null);
	       		String user = mrcsdocument.getRepeatingString("m_dist_list",cc);
	       		try {
			       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"sending message to user: "+user, null, null);
			       	mrcsdocument.queue(user,eventname,1,true,t,message);			       			
	       		} catch (Exception e) {
	       			// do nothing
			       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"bad user in distlist: "+user, null, null);
	       		}
	       	}
	       	
		} catch (DfException dfe) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in queue event" , null, dfe);
			throw new RuntimeException("Error in workflow plugin - queue event plugin",dfe);
		} finally {
			sMgr.release(session);
		}
	}

	
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{
       	/*-CONFIG-*/String m="QueueEvent.execute(WF) - ";
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
	       	
	       	Date d = new Date();
	       	SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
	       	String datestring = sdf.format(d);
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating idftime for datestring "+datestring, null, null);
	       	IDfTime t = new DfTime(datestring, "mm/dd/yyyy");
	       	
	       	// getfirstattachment
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterate through packages" , null, null);
	        IDfSession session = task.getSession();

	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting ATTACHMENT collection for workitem", null, null);
	    	attachments =  task.getAttachments();
			while(attachments.next()) 
			{
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--NEXT ATTACHMENT--", null, null);
			    for (int i = 0; i < attachments.getAttrCount(); i++) 
			    {
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attr:    "+attachments.getAttr(i).getName(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"type:    "+attachments.getAttr(i).getDataType(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"repeats: "+attachments.getAttr(i).isRepeating(), null, null);		            
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"val:     "+attachments.getValueAt(i).asString(), null, null);
			    }
			    String compid = attachments.getString("r_component_id");
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--ATTACHMENT id: "+compid, null, null);
	    		if (compid != null) {
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting ATTACHMENT from docbase", null, null);
	    			IDfDocument attacheddoc = (IDfDocument)session.getObject(new DfId(compid));
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--pkgdoc retrieved: "+attacheddoc.getObjectName() + " - "+attacheddoc.getObjectId().getId(), null, null);
				    
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting most recent version of attachment", null, null);
	    			IDfDocument pkgdoc = (IDfDocument)session.getObjectByQualification("dm_document where i_chronicle_id = '"+attacheddoc.getChronicleId().getId()+"'");
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--most recent retrieved: "+pkgdoc.getObjectName() + " - "+pkgdoc.getObjectId().getId(), null, null);
				    
	    	        try { 
				       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"queuing event for supervisor" , null, null);
				       	pkgdoc.queue(supervisor,eventname,1,true,t,message);
				       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"event queued" , null, null);
				       	// now iterate through the m_dist_list attribute for additional subscribers
				       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterating through distribution list", null, null);
				       	for (int cc=0; cc < pkgdoc.getValueCount("m_dist_list"); cc++)
				       	{
					       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"user #"+cc, null, null);
				       		String user = pkgdoc.getRepeatingString("m_dist_list",cc);
				       		try {
						       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"sending message to user: "+user, null, null);
						       	pkgdoc.queue(user,eventname,1,true,t,message);			       			
				       		} catch (Exception e) {
				       			// do nothing
						       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"bad user in distlist: "+user, null, null);
				       		}
				       	}
	    	        } catch (Exception e) {
	    	    	    /*-ERROR-*/DfLogger.error(this, m+"error while loading or executing promotion service module",null,e);
	    	            throw e;        	
	    	        }
	    		}
	    	}
	       	
		} catch (Exception e) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in queue event" , null, e);
			throw new RuntimeException("Error in workflow plugin - queue event plugin",e);
		} finally {
			try { 
		    	if (attachments != null)
		    		attachments.close();
		    	if (packages != null)
		    		packages.close();
			} catch (DfException dfe) {
				// do nothing...
			}
		}

	}

}
