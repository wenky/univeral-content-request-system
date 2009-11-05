
// This is a base class for creating Java Methods that are more contextually aware.
// What I mean by that is DCTM doesn't provide a means for parameterizing method calls from
// workflows on a per-task basis. The method object can have arguments hardcoded in its docbase
// properties, but that doesn't provide context relative to a specific task in a specific workflow
// that is invoking the method.

// So for example, we have a method that populates or validates a particular type of property. The
// property name can't be hardcoded in the method's code since it may change or be used in different
// workflows. The property name can't be hardcoded in the method object's properties, since that's 
// just a different kind of hardcoding. You want to be able to specify which property to populate or
// validate at runtime - based on the task and workflow the task is executing in.

// Really, it's about making reusable method logic, and not being stuck in an endless loop of one-off
// coding, like what happened to me in MSD-CAS.  

// This class provides:
// - A utility calls to get sessions as the user, dbo (method must be trusted), and a given mrcsapp's sysuser
// - various utility calls to get method configuration from MRCS config based on context 

package com.medtronic.documentum.mrcs.method;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.mthdservlet.IDmMethod;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsWorkflowTask;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class QADocFormConfigurableMethod implements IDmMethod {

	// implement the default configurable method - execute listed actions in workflow task 
	public void execute(Map parameters, OutputStream outputstream) throws Exception 
	{
       	/*-CONFIG-*/String m="QADocFormConfigurableMethod.execute - ";

       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting docbase from JSM parameters" , null, null);
    	String[] paramvals = (String[])parameters.get("docbase_name");
    	String docbase = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~docbase: "+docbase , null, null);
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting packageId from JSM parameters" , null, null);
    	paramvals = (String[])parameters.get("packageId"); // OOTB docbasic promote method thinks this is workitemid...
    	String packageid = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~packageId: "+packageid, null, null);
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting 'mode' from JSM parameters" , null, null);
    	paramvals = (String[])parameters.get("mode");
    	String mode = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~MODE: "+mode , null, null);
	    
   	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcs system session", null, null);
	    IDfSessionManager sessionmgr = getMrcsSystemUserSessionFromFirstAttachment(parameters);
	    IDfSession session = sessionmgr.getSession(docbase);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session DMCL id: "+session.getDMCLSessionId(), null, null);
    	
    	// get workitem
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retreive workitem (the 'packageid')", null, null);
	    IDfWorkitem workitem = (IDfWorkitem)session.getObject(new DfId(packageid));
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"...retrieved workitem "+packageid+"? "+(workitem !=null), null, null);

	    // acquire if not mode 0. ?what? - it's what the docbasic thingy does
	    if ("0".equals(mode))
	    {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mode is 0, acquiring workitem...", null, null);
	    	workitem.acquire();
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"...acquired", null, null);
	    }
	    try {
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get attachment", null, null);	    
	    	IDfSysObject attachment = getFirstAttachment(sessionmgr,parameters);
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mrcsapplication of attachment", null, null);	    	    	
	    	String mrcsapp = attachment.getString("mrcs_application");
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" -- is: "+mrcsapp, null, null);	    
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get workflow object "+workitem.getWorkflowId(), null, null);	    	    	
	    	IDfWorkflow workflow = (IDfWorkflow)session.getObject(workitem.getWorkflowId());
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get process object "+workflow.getProcessId(), null, null);	    	    	
	    	IDfProcess process = (IDfProcess)session.getObject(workflow.getProcessId());
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"workflow process name and task name", null, null);	    	    	
	    	String workflowname = process.getObjectName();
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- workflow process name: "+workflowname, null, null);	    	    		    	
	    	String taskname = workitem.getActivity().getObjectName();
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- workitem activity name: "+taskname, null, null);	    	    		    	
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locate configuration of task in mrcs config", null, null);	    	    		    	
	    	StateTransitionConfigFactory stconfig = StateTransitionConfigFactory.getSTConfig();

		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"look up MRCS task definition", null, null);	    	    		    	
	    	MrcsWorkflowTask mrcstask = stconfig.getMrcsWorkflowTask(mrcsapp,workflowname,taskname);
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting server plugins list (ServerPlugins key in the MethodConfiguration)", null, null);	    	    		    	
	    	List actionlist = (List)mrcstask.MethodConfiguration.get("ServerPlugins");
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"EXEC plugins", null, null);	    	    		    	
	    	if (actionlist != null) 
	    	{
	    		Map context = new HashMap();
	    		for (int i=0; i < actionlist.size(); i++)
	    		{
	    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"running plugin #"+i, null, null);	    	    		    	
	    			MrcsPlugin plugin = (MrcsPlugin)actionlist.get(i);
	    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- instantiating - "+plugin.PluginClassName, null, null);	    	    		    	
	    			IMrcsWorkflowServerPlugin doit = (IMrcsWorkflowServerPlugin)Class.forName(plugin.PluginClassName).newInstance();
	    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- doit!", null, null);	    	    		    	
	    			doit.execute(sessionmgr,docbase,workitem,workflow,process,mrcsapp,plugin.PluginConfiguration, context);
	    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- done!", null, null);	    	    		    	
	    		}
	    	}
	    	

	    } catch (Exception e) {
    	    /*-ERROR-*/DfLogger.error(this, m+"error in promoting workitem packages...",null,e);
    	    sessionmgr.release(session);
    	    throw e;
	    }
	    
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"completing workitem...", null, null);	    
	    workitem.complete();
	    
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing session", null, null);
    	sessionmgr.release(session);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session released", null, null);


	}

	// this is ripped from the dctm sample method
	public IDfSessionManager getUserSession(Map parameters) throws Exception
	{
        /*-CONFIG-*/String m="getUserSession - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting data from parameters map" , null, null);
		String user = ((String[])parameters.get("user"))[0];
		String ticket = ((String[])parameters.get("ticket"))[0];
		String docbase = ((String[])parameters.get("docbase_name"))[0];
		
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting client objects" , null, null);
        IDfClient client = DfClient.getLocalClient();

        IDfSessionManager sMgr = null;
        DfLoginInfo loginInfo = new DfLoginInfo();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting identity to user "+user , null, null);
        loginInfo.setUser(user);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting identity to ticket "+ticket , null, null);
        loginInfo.setPassword(ticket);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting session", null, null);
        //session = client.newSession(docbase, loginInfo);
        sMgr = client.newSessionManager();
        sMgr.setIdentity(docbase,loginInfo);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"returning", null, null);
        return sMgr;
	}

	public IDfSessionManager getDBOSessionRunningAsServer(Map parameters) throws Exception
	{
        /*-CONFIG-*/String m="getDBOSessionRunningAsServer - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting user session so we can query for the dbo's name" , null, null);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating DCTM client object" , null, null);
        IDfClient client = DfClient.getLocalClient();
        
        String docbase = ((String[])parameters.get("docbase_name"))[0];

        IDfSessionManager sMgr = null;
        DfLoginInfo loginInfo = new DfLoginInfo();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting identity to dbo user dm_dbo" , null, null);
        loginInfo.setUser("dm_dbo");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting DBO session", null, null);
        sMgr = client.newSessionManager();
        sMgr.setIdentity(docbase,loginInfo);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"returning", null, null);
        return sMgr;
	}
	
	
	public IDfSessionManager getDBOSession(Map parameters) throws Exception
	{
        /*-CONFIG-*/String m="getDBOSession - ";
        String docbase = ((String[])parameters.get("docbase_name"))[0];
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting user session so we can query for the dbo's name" , null, null);
		IDfSessionManager usersessionmgr = getUserSession(parameters);
		IDfSession usersession = usersessionmgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"quick dbo name: "+usersession.getDocbaseOwnerName() , null, null);
        String dbo = usersession.getDocbaseOwnerName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"create query objects" , null, null);
		String dboquery = "select r_install_owner from dm_server_config";		
        IDfCollection col = null; 
        IDfQuery q = new DfQuery(); 
        q.setDQL(dboquery);        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing "+dboquery , null, null);
        col = q.execute(usersession, IDfQuery.DF_READ_QUERY);
        if (col.next()) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"query successful, reading dbo's name" , null, null);
            dbo = col.getString("r_install_owner");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"DBO name via query: "+dbo , null, null);
            col.close();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing user session" , null, null);
            usersessionmgr.release(usersession);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"user session released" , null, null);
        } else {
        	col.close();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing user session" , null, null);
            usersessionmgr.release(usersession);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"user session released" , null, null);
        	throw new RuntimeException ("r_install_owner not retrievable from docbase");
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating DCTM client object" , null, null);
        IDfClient client = DfClient.getLocalClient();
        
        IDfSessionManager sMgr = null;
        DfLoginInfo loginInfo = new DfLoginInfo();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting identity to dbo user "+dbo , null, null);
        loginInfo.setUser(dbo);
        //loginInfo.setPassword("");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting DBO session", null, null);
        sMgr = client.newSessionManager();
        sMgr.setIdentity(docbase,loginInfo);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"returning", null, null);
        return sMgr;
	}
		
	public IDfSessionManager getDBOSession() throws Exception
	{
		// this uses the user to get Mrcs Config to look up the DBO!
        /*-CONFIG-*/String m="getDBOSession - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"ST Config lookup..." , null, null);
        StateTransitionConfigFactory sts = StateTransitionConfigFactory.getSTConfig();
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting config session so we can query for the dbo's name" , null, null);
        IDfSessionManager usersessionmgr = sts.cfgSession();
        IDfSession usersession = usersessionmgr.getSession(sts.cfgDocbase());
        String docbase = usersession.getDocbaseName();        
        String dbo = usersession.getDocbaseOwnerName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"quick dbo name: "+dbo , null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"create query objects for lookup" , null, null);
		String dboquery = "select r_install_owner from dm_server_config";		
        IDfCollection col = null; 
        IDfQuery q = new DfQuery(); 
        q.setDQL(dboquery);        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing "+dboquery , null, null);
        col = q.execute(usersession, IDfQuery.DF_READ_QUERY);
        if (col.next()) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"query successful, reading dbo's name" , null, null);
            dbo = col.getString("r_install_owner");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"DBO name via query: "+dbo , null, null);
            col.close();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing user session" , null, null);
            usersessionmgr.release(usersession);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"user session released" , null, null);
        } else {
        	col.close();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing user session" , null, null);
            usersessionmgr.release(usersession);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"user session released" , null, null);
        	throw new RuntimeException ("r_install_owner not retrievable from docbase");
        }
		
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating DCTM client object" , null, null);
        IDfClient client = DfClient.getLocalClient();        
        IDfSessionManager sMgr = null;
        DfLoginInfo loginInfo = new DfLoginInfo();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting identity to dbo user "+dbo , null, null);
        loginInfo.setUser(dbo);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting DBO session", null, null);
        sMgr = client.newSessionManager();
        sMgr.setIdentity(docbase,loginInfo);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"returning", null, null);
        return sMgr;
		
	}
	
	public IDfSessionManager getMrcsSystemUserSession(Map parameters, String mrcsapp) throws Exception
	{
        /*-CONFIG-*/String m="getMrcsSystemUserSession - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"accessing config factory..." , null, null);
        StateTransitionConfigFactory sts = StateTransitionConfigFactory.getSTConfig();
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up app's system user for mrcsapp "+mrcsapp , null, null);
        String sysuser = sts.getSystemUsername(mrcsapp);
        String syspass = sts.getSystemPassword(mrcsapp);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating DCTM client object" , null, null);
        IDfClient client = DfClient.getLocalClient();
        
        String docbase = ((String[])parameters.get("docbase_name"))[0];

        IDfSessionManager sMgr = null;
        DfLoginInfo loginInfo = new DfLoginInfo();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting identity to mrcs sysuser "+sysuser , null, null);
        loginInfo.setUser(sysuser);
        loginInfo.setPassword(syspass);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting MRCS sysuser session for mrcsapp", null, null);
        sMgr = client.newSessionManager();
        sMgr.setIdentity(docbase,loginInfo);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"returning", null, null);
        return sMgr;
	}
	
	public IDfSessionManager getMrcsSystemUserSessionFromFirstAttachment(Map parameters) throws Exception
	{
		/*-CONFIG-*/String m = "getMrcsSystemUserSessionFromFirstAttachment - ";
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting user session" , null, null);
	    IDfSessionManager usersession = getUserSession(parameters);
	    
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting first attachment via user session" , null, null);
	    IDfSysObject attachment = getFirstAttachment(usersession,parameters);
	    
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcsapp from first attachment" , null, null);
	    String mrcsapp = attachment.getString("mrcs_application");
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mrcsapp: "+mrcsapp  , null, null);
	    	    
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcs system session"  , null, null);
	    IDfSessionManager syssession = getMrcsSystemUserSession(parameters,mrcsapp);
	    
	    return syssession;
		
	}

	public IDfSessionManager getMrcsSystemUserSessionFromObjectId(Map parameters, String objectid) throws Exception
	{
		/*-CONFIG-*/String m = "getMrcsSystemUserSessionFromFirstAttachment - ";
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting docbase from parameters" , null, null);
        String docbase = ((String[])parameters.get("docbase_name"))[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting user session" , null, null);
	    IDfSessionManager usersessionmgr = getUserSession(parameters);
	    IDfSession usersession = usersessionmgr.getSession(docbase);
	    
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting first attachment via user session" , null, null);
	    IDfSysObject attachment = (IDfSysObject)usersession.getObject(new DfId(objectid));
	    
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcsapp from first attachment" , null, null);
	    String mrcsapp = attachment.getString("mrcs_application");
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mrcsapp: "+mrcsapp  , null, null);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing session" , null, null);
        usersessionmgr.release(usersession);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session released" , null, null);
	    
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcs system session"  , null, null);
	    IDfSessionManager syssession = getMrcsSystemUserSession(parameters,mrcsapp);
	    
	    return syssession;
		
	}
	
	public Map getTaskConfiguration(String mrcsapp, String workflow, String task)
	{
        /*-CONFIG-*/String m="getTaskConfiguration - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"accessing config factory..." , null, null);
        StateTransitionConfigFactory sts = StateTransitionConfigFactory.getSTConfig();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mrcsapp: "+mrcsapp , null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"task: "+task , null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"workflow: "+workflow , null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up task detail for above criteria" , null, null);
        MrcsWorkflowTask taskdetail = sts.getMrcsWorkflowTask(mrcsapp,workflow,task);
        return taskdetail.MethodConfiguration;
	}

	// this one scans the mrcsapplications for the given workflow, task pair, assumes workflow is uniquely named across mrcsapps
	public Map getTaskConfiguration(String workflow, String task)
	{
        /*-CONFIG-*/String m="getTaskConfiguration - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"accessing config factory..." , null, null);
        StateTransitionConfigFactory sts = StateTransitionConfigFactory.getSTConfig();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"task: "+task , null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"workflow: "+workflow , null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scanning mrcsapps for workflow match" , null, null);
        Map config = sts.getUniqueWorkflowTaskMethodConfiguration(workflow,task);
        return config;
	}
	
	public IDfWorkitem getWorkitem(IDfSessionManager sessionmgr, Map parameters) throws Exception
	{
        /*-CONFIG-*/String m="getWorkitem - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get session" , null, null);
        String docbase = ((String[])parameters.get("docbase_name"))[0];
		
		IDfSession session = sessionmgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get workitemid " , null, null);
		String workitemid = ((String[])parameters.get("packageId"))[0];
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get object from db" , null, null);
		IDfWorkitem wi = (IDfWorkitem)session.getObject(new DfId(workitemid));
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing session" , null, null);
        sessionmgr.release(session);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session released" , null, null);
		return wi;
	}

	public IDfSysObject getFirstAttachment(IDfSessionManager sessionmgr, Map parameters) throws Exception
	{
        /*-CONFIG-*/String m="getFirstAttachment - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get session" , null, null);
        String docbase = ((String[])parameters.get("docbase_name"))[0];
        IDfSession session = sessionmgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get workitem interface" , null, null);
		IDfWorkitem wi = getWorkitem(sessionmgr,parameters);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterate through packages" , null, null);
		IDfCollection packages = wi.getAttachments();
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
		packages.close();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing session" , null, null);
        sessionmgr.release(session);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session released" , null, null);
		return pkg;
	}

	public IDfSysObject getFirstPackage(IDfSessionManager sessionmgr, Map parameters) throws Exception
	{
        /*-CONFIG-*/String m="getFirstPackage - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get session" , null, null);
        String docbase = ((String[])parameters.get("docbase_name"))[0];
        IDfSession session = sessionmgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get workitem interface" , null, null);
		IDfWorkitem wi = getWorkitem(sessionmgr,parameters);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterate through packages" , null, null);
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
		packages.close();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing session" , null, null);
        sessionmgr.release(session);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session released" , null, null);
		return pkg;
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