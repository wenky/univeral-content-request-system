package com.medtronic.ecm.documentum.mdtworkflow.method;

import java.io.OutputStream;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;
import com.medtronic.ecm.common.Lg;
 
/**
 * docbase 			= Docbase Name
 * workItemCount 	= Number of Workitems to be delegated.
 * toUserName 		= to user Id
 * workitemId0,workitemId1,workitemId2 ... = Workitems 1,2,3,...
 * 
 * @author molakr1
 *
 */
public class MdtReassignTaskMethod implements IDmMethod {
    /**
     * @param Map parameters(docbasename,workitemcount,workitemsids,tousername)
     * @param OutputStream outputstream
     */
	public void execute(Map parameters, OutputStream outputstream) throws Exception 	{

		
		//Get the docbase Name 
		String[] paramvals 		= (String[])parameters.get("docbase");
    	String strDocbaseName 	=  paramvals[0];
    	/*-dbg-*/Lg.wrn("MdtReassignTaskMethod: strDocbaseName  %s",strDocbaseName);

    	//Get the sesssion
    	IDfSession  mSession = getDfcSession(strDocbaseName); 
    	
    	//Get the number of Workitems to be delegated
    	paramvals = (String[])parameters.get("workItemCount");
    	int workItemCount 	= Integer.parseInt(paramvals[0]);
    	/*-dbg-*/Lg.wrn("MdtReassignTaskMethod: workItemCount  %s",workItemCount);

    	//Get the user_login_name
    	paramvals = (String[])parameters.get("toUserName");
    	String strToUserId 	=  paramvals[0];
    	/*-dbg-*/Lg.wrn("MdtReassignTaskMethod: strToUserId  %s",strToUserId);

        paramvals = (String[])parameters.get("fromUserName");
        String strFromUserId  =  paramvals[0];
        /*-dbg-*/Lg.wrn("MdtReassignTaskMethod: strFromUserId  %s",strFromUserId);

    	//Get the user_name
    	IDfUser dfUser = (IDfUser) mSession.getObjectByQualification("dm_user where user_login_name='"+ strToUserId +"'");
    	String strToUserName =  dfUser.getUserName();
    	/*-dbg-*/Lg.wrn("MdtReassignTaskMethod: strToUserName  %s",strToUserName);
        IDfUser dfFromUser = (IDfUser) mSession.getObjectByQualification("dm_user where user_login_name='"+ strFromUserId +"'");
        String strFromUserName =  dfFromUser.getUserName();
        /*-dbg-*/Lg.wrn("MdtReassignTaskMethod: strFromUserName  %s",strFromUserName);
    	
    	//Get WorkItemId's and call the delegate method 
    	String strWorkItemId = null;
    	for(int i=0;i < workItemCount; i++) {
        	paramvals = (String[])parameters.get("workitemId" + i);
        	strWorkItemId = paramvals[0];
        	doDelegateTask(mSession,strWorkItemId,strToUserName,strFromUserName);
        	/*-dbg-*/Lg.wrn("MdtReassignTaskMethod: strWorkitemId  %s", strWorkItemId);


    	}
    	    	
    	//Disconnect
    	if(mSession != null) {
    		mSession.disconnect();
    	}
	}	

	/**
	 * Delegate the task to user
	 * @param mSession
	 * @param strWorkItemId
	 * @param strToUserId
	 * @throws DfException
	 */
	public void doDelegateTask(IDfSession mSession,String strWorkItemId ,String strToUserName, String strFromUserName) throws DfException{
			//Get the workItem object
			IDfWorkitem workItem = (IDfWorkitem) mSession.getObject(new DfId(strWorkItemId));
			//Acquire the taask
			workItem.acquire();
			/*-dbg-*/Lg.wrn("MdtReassignTaskMethod:doDelegateTask : Task Acquired");
			//Delegate the Task
			workItem.delegateTask(strToUserName);
			/*-dbg-*/Lg.wrn("MdtReassignTaskMethod:doDelegateTask : Task Delegated");
			
            // CEM: Audit Entry to record reassignment
            IDfPersistentObject audit = mSession.newObject("dm_audittrail");
            audit.setString("event_name", "mdt_task_reassign");
            audit.setString("event_source", "MdtReassignTaskMethod");
            audit.setString("user_name", mSession.getLoginUserName());
            audit.setId("audited_obj_id",new DfId(strWorkItemId));
            audit.setString("string_1",strFromUserName);
            audit.setString("string_2",strToUserName);
            audit.setId("chronicle_id", new DfId(strWorkItemId));
            audit.setString("object_name", workItem.getActivity().getObjectName());
            audit.setString("object_type", "dmi_workitem");
            audit.save();
	}
	
	/**
	 * Get the session as installation owner.
	 * @param docbase
	 * @return
	 */
	public IDfSession  getDfcSession(String docbase) throws DfException{
		String sUserName = System.getProperty("user.name");     
		IDfClientX clientx = new DfClientX();
		IDfLoginInfo li = clientx.getLoginInfo();   
		li.setDomain("");        
		li.setUser(sUserName);   
		li.setPassword("");      
		IDfClient client = clientx.getLocalClient();
		IDfSessionManager sessionMgr = client.newSessionManager(); 
		sessionMgr.setIdentity(docbase, li); 
		IDfSession session = sessionMgr.getSession(docbase);
		return session;
	}
	
	// unittest
	public static void main(String[] args) 
	{
        try { 
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser("ecsadmin");
            loginInfoObj.setPassword("spring2005");
            loginInfoObj.setDomain(null);
            sMgr.setIdentity("mqadoc_dev", loginInfoObj);             
            IDfSession sess = sMgr.getSession("mqadoc_dev");            
            
            
            
            MdtReassignTaskMethod methodinst = new MdtReassignTaskMethod();
            methodinst.doDelegateTask(sess, "4a017f4480001950", "Mueller, Carl", "Hanson, MIchael");
            
            sMgr.release(sess);
        } catch (Exception ez) {
            int i = 1;
            i++;
            
        }        
    
	}
	
	

}
