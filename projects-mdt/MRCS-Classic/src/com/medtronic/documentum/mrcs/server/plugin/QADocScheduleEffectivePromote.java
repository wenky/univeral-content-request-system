package com.medtronic.documentum.mrcs.server.plugin;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class QADocScheduleEffectivePromote implements IMrcsWorkflowServerPlugin, IMrcsLifecyclePlugin
{
	
	/* lifecycle exec */
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
    	/*-CONFIG-*/String m="execute - ";
       	IDfSession session = null;
       	try {
       		session = sMgr.getSession(docbase);
       		
       		startScheduledPromote(session,(IDfDocument)mrcsdocument);
		} catch (Exception dfe) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in generate rendtion on server event" , null, dfe);
			throw new RuntimeException("Error in Lifecycle plugin - QADOC schedule effectivity on server event plugin",dfe);
       	} finally { sMgr.release(session); }
		
	}
	
	/* workflow exec */
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{
    	/*-CONFIG-*/String m="execute - ";
       	IDfSession session = null;
       	try {
       		session = sMgr.getSession(docbase);
			session = task.getSession();
			// get attachment...
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"GET ATTACHED DOCUMENT FROM TASK ", null, null);
			IDfCollection packages = task.getPackages("");
		    IDfDocument doc = null;
		    try { 
				if (packages.next())
				{
			        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if there are packages in the r_component_id multivalue attr" , null, null);
					if (packages.getValueCount("r_component_id") > 0)
					{
				        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterating on component list" , null, null);
				        for (int c = 0; c < packages.getValueCount("r_component_id"); c++) 
				        {
							IDfId id = packages.getRepeatingId("r_component_id",c);
					        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retrieving component "+id , null, null);
						    doc = (IDfDocument)session.getObject(id);
						    if ("Quality Document".equals(doc.getString("mrcs_config"))) {
						    	startScheduledPromote(session,doc);
						    }
				        }
					}
				}
		    } finally { packages.close(); }
		    
		    // okey-dokey: now check for additional attachments to the workflow...
		    IDfCollection attachments =  task.getAttachments();
		    try {
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting ATTACHMENT collection for workitem", null, null);
				while(attachments.next()) 
				{
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--NEXT ATTACHMENT--", null, null);
				    String compid = attachments.getString("r_component_id");
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--ATTACHMENT id: "+compid, null, null);
		    		if (compid != null) {
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting ATTACHMENT from docbase", null, null);
		    			IDfDocument attacheddoc = (IDfDocument)session.getObject(new DfId(compid));
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--pkgdoc retrieved: "+attacheddoc.getObjectName() + " - "+attacheddoc.getObjectId().getId(), null, null);
					    
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting most recent version of attachment", null, null);
		    			IDfDocument pkgdoc = (IDfDocument)session.getObjectByQualification("dm_document where i_chronicle_id = '"+attacheddoc.getChronicleId().getId()+"'");
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--most recent retrieved: "+pkgdoc.getObjectName() + " - "+pkgdoc.getObjectId().getId(), null, null);
					    
					    // do scheduling
					    if ("Quality Document".equals(pkgdoc.getString("mrcs_config"))) {
					    	startScheduledPromote(session,pkgdoc);
					    }
		    		}
				}
		    } finally {attachments.close(); }
		    
       		
		} catch (Exception dfe) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in generate rendtion on server event" , null, dfe);
			throw new RuntimeException("Error in workflow plugin - QADOC schedule effectivity on server event plugin",dfe);
       	} finally { sMgr.release(session); }
		
	}

	// adapted from com.medtronic.documentum.mrcs.SchedulePromote
	
    public void startScheduledPromote(IDfSession session, IDfDocument docObject)
    {
    	/*-CONFIG-*/String m="startScheduledPromote - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"QADocScheduleEffectivePromote.startScheduledPromote called.",null,null);

        try {
            IDfTime dt = docObject.getTime("m_effective_date"); 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"MRCS: SchedulePromote promote time " + dt.toString(),null,null);
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            String mrcsapp = docObject.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"mrcs application of doc to be promoted: "+mrcsapp,null,null);
            String nextState = docObject.getNextStateName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"state to promote to: "+nextState,null,null);
            
            if (config.isLegacyLCWF(mrcsapp)) 
            {            
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"LEGACY MRCS 4.1.2 - trying scheduled promote at time " + dt.toString() + " for state " + nextState,null,null);
	            docObject.schedulePromote(nextState, dt, false);
            } else { 
            
	            // new version - create job...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"scheduled promote - MRCS 4.2+ ",null,null);
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");	            
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"date of promote - "+dt.toString(),null,null);
	            String jobname = "QADOCScheduleEffectivity-"+docObject.getObjectId().getId()+"-"+dt.toString();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"name of promotion job - "+jobname,null,null);
	            String methodargs = "-mrcsscheduledpromote yes -dboname "+session.getDocbaseOwnerName()+" -docbase_name "+session.getDocbaseName()+" -objectid "+docObject.getObjectId().getId();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    methodargs: "+methodargs,null,null);
	            	            
	            // create the object - need to be system user...
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"switching to system user for job creation",null,null);
	            String user = config.getSystemUsername(mrcsapp);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"system user for current mrcs app: "+user,null,null);
	            String pass = config.getSystemPassword(mrcsapp);
	            String docbase = config.getApplicationDocbase(mrcsapp);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"DOCBASE for mrcs app: "+docbase,null,null);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"reuse local client from session..."+docbase,null,null);
	            IDfClient curclient = session.getClient();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"prepare login ticket/info",null,null);
	            DfLoginInfo login = new DfLoginInfo();
	            login.setUser(user); 
	            login.setPassword(pass);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"creating new session manager",null,null);
	            IDfSessionManager sysmgr = curclient.newSessionManager();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"setting login identity",null,null);
	            sysmgr.setIdentity(docbase,login);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"finally, get system session from system session manager...",null,null);
	            IDfSession syssession = sysmgr.getSession(docbase);
	            
	            // create job object
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"CREATING new DM_JOB for scheduled promote method call",null,null);
	            IDfPersistentObject newjob = (IDfPersistentObject)syssession.newObject("dm_job");
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    object_name: "+jobname,null,null);
	            newjob.setString("object_name",jobname);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    method_name: MrcsPromoteMethod",null,null);
	            newjob.setString("method_name","MrcsPromoteMethod");
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    method_arguments: "+methodargs,null,null);
	            newjob.setRepeatingString("method_arguments",0,methodargs);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    start_date: "+dt.toString(),null,null);
	            newjob.setTime("start_date",dt);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    a_next_invocation: "+dt.toString(),null,null);
	            newjob.setTime("a_next_invocation",dt);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    preparing execdate + one year for expiration date...",null,null);
	            Date oneyearlater = dt.getDate();
	            oneyearlater.setYear(oneyearlater.getYear()+1);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    expiration_date: "+sdf.format(oneyearlater),null,null);
	            newjob.setTime("expiration_date",new DfTime(oneyearlater));
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    set max_iterations, run_interval, and run_mode to 1",null,null);
	            newjob.setInt("max_iterations",1);
	            newjob.setInt("run_interval",1);
	            newjob.setInt("run_mode",1);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    inactivate_after_failure = true",null,null);
	            newjob.setBoolean("inactivate_after_failure",true);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    run_now = false",null,null);
	            newjob.setBoolean("run_now",false);
	            newjob.setInt("method_trace_level",10);
	            
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"SAVE of new job...",null,null);
	            newjob.save();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"...job saved, releasing system session",null,null);
	            
	            sysmgr.release(syssession);
            }
        }
        catch (DfException dfe){
            /*-ERROR-*/DfLogger.error(this,m+"-------startScheduledPromote Exception---------",null,dfe);
        }

    }
	

}

