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

public class MdtPromoteForm implements IMdtWorkflowAction
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map context)
    {
        // promote form
        try { 
            if (formobj.canPromote())
            {
                /*-dbg-*/Lg.dbg("invoking promote from workflow on changre request form %s",formobj);
                formobj.promote(null,true,false);
                /*-dbg-*/Lg.dbg("promote returned successfully");
            } else {
                /*-WARN-*/Lg.wrn("attachment document canPromote() returned false %s",formobj);
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while promoting change request form %s",formobj,dfe);
            throw EEx.create("PromoteChangeRequest","error while promoting change request form %s",formobj,dfe);            
        }
        
    }

}
