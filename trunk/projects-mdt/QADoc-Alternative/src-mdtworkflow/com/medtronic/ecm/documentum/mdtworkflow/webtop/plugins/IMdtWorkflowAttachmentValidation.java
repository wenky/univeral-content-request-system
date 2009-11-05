package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;

// in general, this should be usable for both workflow and task validations...

public interface IMdtWorkflowAttachmentValidation {
    public boolean validate(IDfSessionManager sMgr, String docbase, String mdtapp, IDfSysObject formobj,  IDfSysObject attachment, List errors, Map context);    

}
