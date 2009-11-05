package com.medtronic.documentum.mrcs.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPackage;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.services.workflow.common.IWorkflowTaskAttachment;
import com.documentum.services.workflow.inbox.ITask;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowActionPlugin;


// this class schedules a job in Documentum, can be used to schedule a promote (i.e.Japan)

public class MrcsScheduleJob implements IMrcsLifecyclePlugin, IMrcsWorkflowActionPlugin
{

	// workflow plugin entry point
	public void execute(IDfSessionManager sMgr, String docbase, IWorkflowTask task, IDfWorkflow workflow, String mrcsapp, Map config, Map context)
	{
		/*-CONFIG-*/String m="execute (workflow plugin) - ";
		try { 
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"getting session",null,null);
			IDfSession session = sMgr.getSession(docbase);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"getting attached document",null,null);
			IDfSysObject attachment = getSignableDocument(session,task);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"calling job scheduling",null,null);
	        scheduleJob(mrcsapp,attachment,config,session);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"releasing session",null,null);
			sMgr.release(session);
		} catch (Exception e) {
	        /*-ERROR-*/DfLogger.error(this,m+"error in workflow plugin's scheduling of job",null,e);
			throw new RuntimeException("Error in MrcsScheduleJob called from workflow action",e);
		}
		
	}
	
	// lifecycle plugin entry point
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
		/*-CONFIG-*/String m="execute (lifecycle plugin) - ";
		try { 
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"getting session",null,null);
			IDfSession session = sMgr.getSession(docbase);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"calling job scheduling",null,null);
			scheduleJob(mrcsapp,mrcsdocument,config,session);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"releasing session",null,null);
			sMgr.release(session);
		} catch (Exception e) {
	        /*-ERROR-*/DfLogger.error(this,m+"error in lifecycle plugin's scheduling of job",null,e);
			throw new RuntimeException("Error in MrcsScheduleJob called from workflow action",e);
		}
		
	}
		
	public void scheduleJob (String mrcsapp, IDfSysObject docObject, Map pluginconfig, IDfSession session) throws Exception
	{
		/*-CONFIG-*/String m="scheduleJob - ";
		
		// get method name to execute from config
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"getting configuration for scheduling of job+ ",null,null);
		String methodname = (String)pluginconfig.get("Method"); //JavaServerMethod to execute for the job
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"METHOD NAME - "+methodname,null,null);
		String delay = (String)pluginconfig.get("Delay");       //delay from current time
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"DELAY - "+delay,null,null);
		String delayunits = (String)pluginconfig.get("Unit");   //minute, hour, day
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"DELAY UNITS - "+delayunits,null,null);
		long unit = Long.parseLong(delay);
		
		// other configurable options: jobname, trace level, 
		
		// calculate execution date
		long currenttime = new Date().getTime();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"calculating execution time from current time - "+currenttime,null,null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		if ("minute".equalsIgnoreCase(delayunits)) {
			currenttime += unit * 60000L; // 60 seconds per minute * 1000 milliseconds per second
		} else if ("hour".equals(delayunits)) {
			currenttime += unit * 360000L; // 60000 milliseconds per minute * 60 minutes
		} else if ("day".equals(delayunits)) {
			currenttime += unit * 24L * 360000L; // 60000 milliseconds per minute * 60 minutes per hour * 24 hours per day
		}
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"execution time (milliseconds) - "+currenttime,null,null);
		Date dt = new Date(currenttime);
		 		
		
        // new version - create job...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"scheduled job - MRCS 4.2+ ",null,null);
        String execdate = sdf.format(dt); 
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"date of JOB - "+execdate,null,null);
        String jobname = methodname+"-"+docObject.getObjectId().getId()+"-"+execdate;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"name of job - "+jobname,null,null);
        String methodargs = "-docbase "+session.getDocbaseName()+" -objectid "+docObject.getObjectId().getId();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    methodargs: "+methodargs,null,null);
        
        //String createjob = "CREATE dm_job OBJECT " +
        //    "object_name = '"+jobname+"', " + // job name? document name + scheduled promotion time ? 
        //    "method_name = 'MrcsPromoteMethod', " +
        //    "method_arguments = '" +methodargs+"', " +
        //    "pass_standard_arguments = false, " + //default is false anyway...
        //    "start_date = "+execdate+", " +       // scheduled date of promotion
        //    "expiration_date = "+execdate+", " +  // same as start date
        //    "a_next_invocation = "+execdate+", " +  // same as start date
        //    "max_iterations = 1, run_interval = 1, run_mode = 1, inactivate_after_failure = true, run_now = false";
        
        // create the object - need to be system user...
        StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
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
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    method_name: "+methodname,null,null);
        newjob.setString("method_name",methodname);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    method_arguments: "+methodargs,null,null);
        newjob.setRepeatingString("method_arguments",0,methodargs);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    start_date: "+execdate,null,null);
        newjob.setTime("start_date",new DfTime(dt));
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    a_next_invocation: "+execdate,null,null);
        newjob.setTime("a_next_invocation",new DfTime(dt));
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    preparing execdate + one year for expiration date...",null,null);
        Date oneyearlater = dt;
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
	
	public static IDfSysObject getSignableDocument(IDfSession idfsession, ITask itask)throws DfException{
        /*-CONFIG-*/String m="getSignableDocument - ";
		com.documentum.fc.client.IDfDocument documentObj = null;
        //IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
        //IDfSession idfsession = idfsessionmanager.getSession(SessionManagerHttpBinding.getCurrentDocbase());

        //CEM: this attachment retrival strategy has a strange bug: it only works athe first time you open the task, so if something fails
        //     and you try to reperform the task, it will return the wrong document version (the document at the beginning of the wf start,'
        //     not the current one), so if renditions are in the current version, it won't find them since it's looking at the pre-promoted
        //     document version.
		IWorkflowTask  iworkflowtask = (IWorkflowTask )itask;
		IDfList lDocs= iworkflowtask.getAttachments();
		IWorkflowTaskAttachment wrkAttachment = (IWorkflowTaskAttachment)lDocs.get(0);
		IDfId docID = wrkAttachment.getDocumentId(0);
        
        //CEM: this is an alternative way to get the package/attachment that seems to be much more reliable. 
        DfQuery packagequery = new DfQuery();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"getting workflow id",null,null);
        IDfId workflowid = itask.getWorkflowId();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"workflow id: "+(workflowid==null?null:workflowid.getId()),null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"looking up workflow's package aka the attachment",null,null);
        // max(r_object_id) is a crude/effective way to select the most recent dmi_package for this workflow...
        String dqlpackage = "select max(r_object_id) from dmi_package where r_workflow_id ='"+workflowid.getId()+"'";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"package lookup dql: "+dqlpackage,null,null);
        packagequery.setDQL(dqlpackage);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"executing dql",null,null);
        IDfCollection packages = packagequery.execute(idfsession, 0);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"getting first package",null,null);
        packages.next();
        String packageid = packages.getString(packages.getAttr(0).getName());
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"packageid: "+packageid,null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"retrieving IDfPackage object",null,null);
        IDfPackage pkg = (IDfPackage)idfsession.getObject(new DfId(packageid));
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"getting package's first componentid",null,null);
        IDfId docid = pkg.getComponentId(0);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class,m+"package docid: "+(docid==null?null:docid.getId()),null,null);
        packages.close();

		documentObj = (IDfDocument)idfsession.getObject(docid);
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsScheduleJob.class))DfLogger.debug(MrcsScheduleJob.class, m+"documentObj1 : "+documentObj, null, null);

		return documentObj;
	}
	

}
