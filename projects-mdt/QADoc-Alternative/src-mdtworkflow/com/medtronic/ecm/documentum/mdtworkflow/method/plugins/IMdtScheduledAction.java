package com.medtronic.ecm.documentum.mdtworkflow.method.plugins;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;

public interface IMdtScheduledAction 
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfSysObject jobobject, Map methodparameters, Map context);
}
