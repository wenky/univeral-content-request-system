package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowAttachmentValidation;

public class MdtValidateAttachmentPermit implements IMdtWorkflowAttachmentValidation 
{

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, IDfSysObject attachment, List errors, Map context) 
    {
        // make sure the user has RELATE permission and exec_proc extended permit on the attachment
        // TODO: make configurable (change it to WRITE, or just READ, or doesn't need EXEC_PROC)
        boolean success = true;
        try{
            /*-dbg-*/Lg.dbg("get permit leel on %s",attachment);
            int basepermit = attachment.getPermit();
            /*-dbg-*/Lg.dbg("-- permit: %d",basepermit);
            if (basepermit < IDfACL.DF_PERMIT_RELATE) {
                /*-dbg-*/Lg.dbg("returning no RELATE validation failure");
                success = false;
                if (context!=null && ((Map)context).containsKey("NoRelateError")) {
                    errors.add(MdtErrorService.renderErrorMessage("NoRelateError",(Map)context,mgr,docbase,formobj,attachment,new ErrKey("permit",basepermit)));
                } else {
                    errors.add("You do not have RELATE permission on attachment "+attachment.getObjectName());
                }
            }
            
            /*-dbg-*/Lg.dbg("check for EXEC_PROC extended permit");
            if (!attachment.hasPermission(IDfACL.DF_XPERMIT_EXECUTE_PROC_STR, attachment.getSession().getLoginUserName())) {
                /*-dbg-*/Lg.dbg("returning extended permit validation failure");
                success = false;
                if (context!=null && ((Map)context).containsKey("NoExecProcError")) {
                    errors.add(MdtErrorService.renderErrorMessage("NoExecProcError",(Map)context,mgr,docbase,formobj,attachment));
                } else {
                    errors.add("You do not have EXEC_PROC extended permission on attachment "+attachment.getObjectName());
                }
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking attachment permission status for %s",attachment,dfe);
            throw EEx.create("WFValidateState-DFE","Error checking attachment permission status for %s",attachment,dfe);
        }
        
        /*-dbg-*/Lg.dbg("return true");        
        return success;
    }

}
