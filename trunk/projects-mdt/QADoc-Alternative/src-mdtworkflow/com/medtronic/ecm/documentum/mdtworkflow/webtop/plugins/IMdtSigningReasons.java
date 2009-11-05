package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins;

import java.util.List;
import java.util.Map;

import com.documentum.services.workflow.inbox.IWorkflowTask;

public interface IMdtSigningReasons {
	
	public List getReasons(String processname,String workflowname,IWorkflowTask task,Map taskconfig,Map context);

}
