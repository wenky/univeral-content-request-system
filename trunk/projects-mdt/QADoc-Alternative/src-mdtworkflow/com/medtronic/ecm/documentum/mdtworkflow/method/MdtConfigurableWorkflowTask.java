package com.medtronic.ecm.documentum.mdtworkflow.method;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.mthdservlet.IDmMethod;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;

// configurable plugin executor for Workflow methods

//This method executes IMdtWorkflowAction plugins 


public class MdtConfigurableWorkflowTask implements IDmMethod
{
	// implement the default configurable method - execute listed actions in workflow task
	public void execute(Map parameters, OutputStream outputstream) throws Exception
	{
    	/*-INFO-*/Lg.inf("Top");
	    /*-dbg-*/Lg.wrn("dumping method invocation parameters");
    	/*-dbg-*/if (Lg.wrn())try {Iterator i = parameters.keySet().iterator(); while (i.hasNext())Lg.wrn("paramkey: %s",(String)i.next());} catch (Exception e) {}    	

    	String docbase = ((String[])parameters.get("docbase_name"))[0];
	    /*-dbg-*/Lg.wrn("  ~~docbase: %s",docbase);
    	String packageid = ((String[])parameters.get("packageId"))[0];
	    /*-dbg-*/Lg.wrn("  ~~packageId: %s",packageid);
    	String mode = ((String[])parameters.get("mode"))[0];
	    /*-dbg-*/Lg.wrn("  ~~MODE: %s",mode);
	    
	    /*-dbg-*/Lg.wrn("getting trusted session");
        IDfSessionManager sessionmgr = MethodUtils.doTrustedLogin(docbase);
	    IDfSession session = null;
	    
	    try { 
	   	    /*-dbg-*/Lg.wrn("acquire session");
	    	session = sessionmgr.getSession(docbase);
	    	
	    	// get workitem
		    /*-dbg-*/Lg.wrn("retreive workitem (aka the 'packageid')");
		    IDfWorkitem workitem = (IDfWorkitem)session.getObject(new DfId(packageid));
		    /*-dbg-*/Lg.wrn("...retrieved workitem: %s",workitem);

		    /*-dbg-*/Lg.wrn("retreive workflow and process objects");
	    	IDfWorkflow workflow = (IDfWorkflow)session.getObject(workitem.getWorkflowId());
	    	IDfProcess process = (IDfProcess)session.getObject(workflow.getProcessId());
		    String workflowname = process.getObjectName();
	    	String taskname = workitem.getActivity().getObjectName();
		    /*-dbg-*/Lg.wrn("...retrieved process %s and workflow %s",process,workflow);

		    /*-dbg-*/Lg.wrn("retreive primary package (the workflow control document)");
		    IDfDocument controldoc = (IDfDocument)WorkflowUtils.getPrimaryPackage(session, workflow.getObjectId().getId());
		    /*-dbg-*/Lg.wrn("...retrieved control doc: %s",controldoc);
		    		    
		    /*-dbg-*/Lg.wrn("get application");
		    String mdtapp = controldoc.getString("m_application");		    
		    /*-dbg-*/Lg.wrn("...m_application is %s",mdtapp);
		    
		    /*-dbg-*/Lg.wrn("getting task configuration");
            Map taskconfig = WorkflowUtils.getTaskConfig(sessionmgr,docbase,mdtapp,taskname,controldoc);
		    /*-dbg-*/Lg.wrn("task config retrieved");
		    
		    // acquire if not mode 0. ?what? - it's what the docbasic scripts do...
		    if ("0".equals(mode))
		    {
			    /*-dbg-*/Lg.wrn("mode is 0, acquiring workitem...");
		    	workitem.acquire();
			    /*-dbg-*/Lg.wrn("...acquired");
		    }

		    // get list of most recent attachments
            List attachmentlist = AttachmentUtils.getWorkflowAttachments(workitem);
            
		    /*-dbg-*/Lg.wrn("checking for custom processing");
		    if (taskconfig.containsKey("MethodActions"))
		    {
		        int i = 0;
    	    	try {
                    List methodplugins = (List)taskconfig.get("MethodActions");
                    for (i=0; i < methodplugins.size(); i++)
                    {                    
                        /*-dbg-*/Lg.wrn("execute %dth method action",i);
                        MdtPlugin plugin = (MdtPlugin)methodplugins.get(i);
                        /*-dbg-*/Lg.wrn("load custom action class %s",plugin.classname);
                        IMdtWorkflowAction customaction = (IMdtWorkflowAction)MdtPluginLoader.loadPlugin(plugin,sessionmgr);
                        /*-dbg-*/Lg.wrn(" --exec MDTAPP: %s CONTROLDOC: %s",mdtapp,controldoc);
                        customaction.execute(sessionmgr, docbase, mdtapp, controldoc, attachmentlist, workitem, parameters, plugin.context);                      
                        /*-dbg-*/Lg.wrn(" --action done");
                    }
				    
		    	} catch (Exception e) {
				    /*-WARN-*/Lg.wrn("Warning! Error occurred in methed action processing, checking for exception handler",e);
				    if (taskconfig.containsKey("MethodActionExceptionHandler"))
				    {
				        try { 
                            /*-dbg-*/Lg.wrn("execute %dth method action",i);
                            MdtPlugin plugin = (MdtPlugin)taskconfig.get("MethodActionExceptionHandler");
                            /*-dbg-*/Lg.wrn("load custom action class %s",plugin.classname);
                            IMdtWorkflowAction customaction = (IMdtWorkflowAction)MdtPluginLoader.loadPlugin(plugin,sessionmgr);
                            /*-dbg-*/Lg.wrn(" --exec MDTAPP: %s CONTROLDOC: %s",mdtapp,controldoc);
                            customaction.execute(sessionmgr, docbase, mdtapp, controldoc, attachmentlist, workitem, parameters, plugin.context);                      
                            /*-dbg-*/Lg.wrn(" --action done");
				        } catch (Exception e2) {
	                        /*-ERROR-*/Lg.err("Error occurred in attempted exception recovery",e2);
                            /*-ERROR-*/Lg.err("ORIGINAL ROOT CAUSE:",e);
	                        throw EEx.create("MdtCfgMethod-RecoveryError","Error Recovery attempt threw exception",e);				            
				        }
				        
				    } else {				    
	                    /*-ERROR-*/Lg.err("Unrecoverable Error occurred in method action execution",e);
			            throw EEx.create("MdtCfgMethod-MethActions","Unrecoverable Error occurred in method action execution",e);
				    }
		    	}
		    }
		    		    
		    /*-dbg-*/Lg.wrn("completing workitem...");	    
		    workitem.complete();
	    } finally {	    
	        /*-dbg-*/Lg.wrn("releasing session");
    	    try {sessionmgr.release(session);} catch(Exception e) {Lg.wrn("Unable to release session",e);}
	        /*-dbg-*/Lg.wrn("session released");
	    }
	    /*-dbg-*/Lg.wrn("method invocation complete");	    	    
	}
		
	public IDfWorkitem getWorkitem(IDfSessionManager sessionmgr, Map parameters) throws DfException
	{
        /*-dbg-*/Lg.wrn("get session" );		
		String docbase = ((String[])parameters.get("docbase_name"))[0];
		IDfSession session = null;
		IDfWorkitem wi = null;
		try {
			session = sessionmgr.getSession(docbase);
	        /*-dbg-*/Lg.wrn("get workitemid " );
			String workitemid = ((String[])parameters.get("packageId"))[0];
	        /*-dbg-*/Lg.wrn("get object from db" );
			wi = (IDfWorkitem)session.getObject(new DfId(workitemid));
		} finally {
	        /*-dbg-*/Lg.wrn("releasing session" );
	    	try {sessionmgr.release(session);} catch(Exception e) {Lg.wrn("Unable to release session",e);}	        
		}
        /*-dbg-*/Lg.wrn("done" );
		return wi;
	}

}

/*

workflow arguments (from dm_developer)
1.  "user" --> The username to run the method as
2. "docbase_name" --> The name of the docbase to run the method against
3. "packageId" --> The workitemId (yes, it is called packageId even though it is for a workitemId)
4. "ticket" --> The ticket to use to log onto the docbase
5. "mode" --> Just ignore this

*/