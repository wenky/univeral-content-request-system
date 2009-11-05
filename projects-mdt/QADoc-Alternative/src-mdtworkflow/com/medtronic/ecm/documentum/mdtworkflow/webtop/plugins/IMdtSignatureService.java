package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins;

import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.services.workflow.inbox.IWorkflowTask;

public interface IMdtSignatureService {
	public void sign(IDfSessionManager sMgr, String docbase, String mrcsapp, String processname, IWorkflowTask task, IDfDocument primarypackage,String username,String password,String reason, Map context);

}
