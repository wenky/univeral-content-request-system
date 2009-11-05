package com.medtronic.ecm.documentum.mdtworkflow.common.plugins;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;

public interface IMdtRemoveAttachment 
{
    public void remove(IDfSysObject formdoc, IDfSysObject attachment, Map crconfig, Map pluginconfig);
}
