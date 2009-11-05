package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.services.workflow.inbox.IWorkflowTask;

public interface ISignatureService 
{
	public void sign(IDfSessionManager sMgr, String docbase, String mrcsapp, String processname, IWorkflowTask task, IDfDocument primarypackage,String username,String password,String reason, Map plugin, Map context) throws DfException;
	
}
