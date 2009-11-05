package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtValidateDuplicateApprovers implements IMdtWorkflowValidation 
{

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {
        // iterate through approver attributes
        Map config = (Map)context;
        List attrlist = (List)config.get("ApproverAttributes");
        Map approvers = new HashMap();
        boolean success = true;
        for (int i=0; i < attrlist.size(); i++) {
            String attr = (String)attrlist.get(i);
            try { 
                if (formobj.isAttrRepeating(attr)) {
                    for (int j=0; j < formobj.getValueCount(attr); j++) {
                        String val = formobj.getRepeatingString(attr, j);
                        if (val != null && !"".equals(val.trim())) {
                            if (approvers.containsKey(val)) {
                                success = false;
                                // add error
                                errors.add("User "+val+" found in multiple approval tasks: ("+attr+" , "+approvers.get(val)+")");            
                            } else {
                                approvers.put(val, attr);
                            }
                        }
                    }
                } else {
                    String val = formobj.getString(attr);
                    if (val != null && !"".equals(val.trim())) {
                        if (approvers.containsKey(val)) {
                            success = false;
                            // add error
                            errors.add("User "+val+" found in multiple approval tasks: ("+attr+" , "+approvers.get(val)+")");            
                        } else {
                            approvers.put(val, attr);
                        }
                    }                    
                }
            } catch (DfException dfe) {
                /*-ERROR-*/Lg.err("Exception checking duplicate approvers on attr %s, formobj %s",attr,formobj,dfe);
                throw EEx.create("VldtDupAppvrs","Exception checking duplicate approvers on attr %s, formobj %s",attr,formobj,dfe);                                   
                
            }
            
        }
        return success;
        
    }

}
