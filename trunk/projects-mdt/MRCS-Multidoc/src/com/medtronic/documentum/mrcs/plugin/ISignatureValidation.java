package com.medtronic.documentum.mrcs.plugin;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.services.workflow.inbox.IWorkflowTask;

// we need a new one for signing and possibly others 
public interface ISignatureValidation 
{
    public boolean validate(IDfSessionManager sMgr, String docbase, String mrcsapp, String wfname, IWorkflowTask task, IDfSysObject packagedoc, String user, String pass, String reason, List errors, Map configdata, Map customdata) throws Exception;

}
