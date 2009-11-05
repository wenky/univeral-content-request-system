package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

public interface IMdtWorkflowTemplateResolver {

    public String resolveWFT(IDfSessionManager sMgr, String docbase, String mdtapp, IDfSysObject formobj, Map context) throws DfException;     

}
