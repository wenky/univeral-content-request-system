package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtValidateFormNotLaunched implements IMdtWorkflowValidation {

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {
        boolean success = true;
                
        try {
            // validate it is base state
            if (formobj.getCurrentState() != 0) {
                if (context!=null && ((Map)context).containsKey("BaseStateError")) {
                    errors.add(MdtErrorService.renderErrorMessage("BaseStateError",(Map)context,mgr,docbase,formobj,null));
                } else {
                    errors.add("Form "+formobj.getObjectName()+" must be in the base state before being routed");
                }
                success = false;
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking form object standard validations %s",formobj,dfe);
            throw EEx.create("WFValidateForm-DFE","Error checking form object standard validations %s",formobj,dfe);
        }
        
        /*-dbg-*/Lg.dbg("return %b",success);
        return success;
        
    }

}
