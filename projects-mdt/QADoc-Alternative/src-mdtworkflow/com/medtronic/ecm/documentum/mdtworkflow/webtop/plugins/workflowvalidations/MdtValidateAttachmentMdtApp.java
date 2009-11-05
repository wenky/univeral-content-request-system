package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowAttachmentValidation;

public class MdtValidateAttachmentMdtApp implements IMdtWorkflowAttachmentValidation 
{

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, IDfSysObject attachment, List errors, Map context) 
    {
        try {
            if (!mdtapp.equals(attachment.getString("m_application"))) {
                errors.add("Attachment "+attachment.getObjectName()+" is not a "+mdtapp+" document");
                return false;
            }
            return true;
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking attachment required attributes %s",attachment,dfe);
            throw EEx.create("WFValidateAttrs-DFE","Error checking attachment required attributes %s",attachment,dfe);
        }
        
    }

}
