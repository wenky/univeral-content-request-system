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

public class MdtValidateFormInBaseState implements IMdtWorkflowValidation {

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {
        boolean success = true;
                
        try {
            // validate m_rollback_form is not "in-wf"; 
            if ("in-wf".equals(formobj.getString("m_rollback_form"))) {
                if (context!=null && ((Map)context).containsKey("InWorkflowError")) {
                    errors.add(MdtErrorService.renderErrorMessage("InWorkflowError",(Map)context,mgr,docbase,formobj,null));
                } else {
                    errors.add("Form "+formobj.getObjectName()+ " appears to be already routed");
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
