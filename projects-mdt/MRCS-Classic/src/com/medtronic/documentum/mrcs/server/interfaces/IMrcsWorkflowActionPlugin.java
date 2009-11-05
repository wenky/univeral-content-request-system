package com.medtronic.documentum.mrcs.server.interfaces;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.services.workflow.inbox.IWorkflowTask;

public interface IMrcsWorkflowActionPlugin {
	public void execute(IDfSessionManager sMgr, String docbase, IWorkflowTask task, IDfWorkflow workflow, String mrcsapp, Map config, Map context);
}
