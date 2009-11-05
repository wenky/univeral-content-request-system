package com.medtronic.ecm.documentum.mdtworkflow.webtop.common;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSignatureService;

public class MdtSignatureServiceQuickFlow implements IMdtSignatureService 
{	

	public void sign(IDfSessionManager mgr, String docbase, String mrcsapp, String processname, IWorkflowTask task, IDfDocument primarypackage,String username, String password, String reason, Map context) 
	{		
    	// get the session
    	IDfSession session = null;	   
    	 IDfSessionManager sysmgr = null;
	    try {
	        sysmgr = WorkflowUtils.getSystemSessionManager(primarypackage.getSession(),mrcsapp);
		    /*-dbg-*/Lg.dbg("get session");
	    	session = sysmgr.getSession(docbase);
	    	String attrname = (String)context.get("ReasonAttr");
	    	IDfSysObject formobj = (IDfSysObject)session.getObject(new DfId(primarypackage.getObjectId().getId()));
	    	// append to an attr - may need to switch to sysuser
	    	formobj.appendString(attrname, username + " - " + reason);
	    	formobj.save();
	    	// refetch to be safe
	    	primarypackage.fetch(null);
	    } catch (DfException e) {
    	    /*-ERROR-*/Lg.err("error in promoting workitem attachments...",e);
    	    throw EEx.create("SignatureService","error in promoting workitem attachments...",e);
	    } finally {
		    /*-dbg-*/Lg.dbg("finally, close the attachment collection");
	    	try{sysmgr.release(session);}catch(Exception e){Lg.wrn("Unable to release session",e);}
	    }
		

	}
		

}
