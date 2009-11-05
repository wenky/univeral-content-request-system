package com.medtronic.documentum.mrcs.server.interfaces;

import java.util.Map;

import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;

public interface IMrcsWorkflowServerPlugin {
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context);

}
