package com.medtronic.ecm.documentum.mdtworkflow.method.plugins;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;

public interface IMdtJobAction 
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, String taskname, Map methodparameters, Map context);    
}
