package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;

public class MdtSuspendForm implements IMdtWorkflowAction
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map context)
    {
        // promote form
        try { 
            if (formobj.canSuspend())
            {
                /*-dbg-*/Lg.dbg("invoking suspend from workflow on changre request form %s",formobj);
                formobj.suspend(null,true,false);
                /*-dbg-*/Lg.dbg("suspend returned successfully");
            } else {
                /*-WARN-*/Lg.wrn("attachment document canSuspend() returned false %s",formobj);
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while suspending change request form %s",formobj,dfe);
            throw EEx.create("SuspendChangeRequest","error while suspending change request form %s",formobj,dfe);            
        }
        
    }

}
