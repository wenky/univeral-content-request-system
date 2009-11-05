package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;

public interface IMdtWorkflowValidation 
{
    public boolean validate(IDfSessionManager sMgr, String docbase, String mdtapp, IDfSysObject formobj,  List errors, Map context);    
}
