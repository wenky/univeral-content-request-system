package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowAttachmentValidation;

public class MdtValidateAttachmentState implements IMdtWorkflowAttachmentValidation 
{

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, IDfSysObject attachment, List errors, Map context) 
    {

        boolean success = true;
        try { 
            if (context != null && context.containsKey("RequiredState")) {
                String requiredstate = (String)context.get("RequiredState");
                String currentstate = attachment.getCurrentStateName();
                if (requiredstate == null) requiredstate = "";
                /*-dbg-*/Lg.dbg("plugin top - check if in base state (zero) on %s",attachment);
                if (!requiredstate.equals(currentstate)) {
                    /*-dbg-*/Lg.dbg("attachment %s is not in base state",attachment);
                    if (context!=null && ((Map)context).containsKey("BaseStateError")) {
                        errors.add(MdtErrorService.renderErrorMessage("BaseStateError",(Map)context,mgr,docbase,formobj,attachment));
                    } else {
                        errors.add("attachment "+attachment.getObjectName()+" is not in requried state "+requiredstate);
                    }
                    success = false;
                }
            } else {
                // default behavior: attachment state is zero
                /*-dbg-*/Lg.dbg("plugin top - check if in base state (zero) on %s",attachment);
                if (attachment.getCurrentState() != 0) {
                    /*-dbg-*/Lg.dbg("attachment %s is not in base state",attachment);
                    if (context!=null && ((Map)context).containsKey("BaseStateError")) {
                        errors.add(MdtErrorService.renderErrorMessage("BaseStateError",(Map)context,mgr,docbase,formobj,attachment));
                    } else {
                        errors.add("attachment "+attachment.getObjectName()+" is not in the base state");
                    }
                    success = false;
                }
            }
            
            // validate the document is not locked/checked out
            /*-dbg-*/Lg.dbg("check if attachment is locked");
            if (attachment.isCheckedOut()) {
                /*-dbg-*/Lg.dbg("document is locked out");
                if (context!=null && ((Map)context).containsKey("AttachmentLockedError")) {
                    errors.add(MdtErrorService.renderErrorMessage("AttachmentLockedError",(Map)context,mgr,docbase,formobj,attachment));
                } else {
                    errors.add("attachment "+attachment.getObjectName()+" is locked/checked out");
                }
                success = false;
                
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking attachment status (base state, not locked) for %s",attachment,dfe);
            throw EEx.create("WFValidateState-DFE","Error checking attachment status (base state, not locked) for %s",attachment,dfe);
        }
        
        /*-dbg-*/Lg.dbg("return true");
        return success;
        
    }

}
