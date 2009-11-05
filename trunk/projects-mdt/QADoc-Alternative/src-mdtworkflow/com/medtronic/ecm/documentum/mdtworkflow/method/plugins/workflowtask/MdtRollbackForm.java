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

public class MdtRollbackForm implements IMdtWorkflowAction
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map context)
    {
        // promote form
        try { 
            /*-dbg-*/Lg.dbg("deleting current version of form object %s", formobj);
            formobj.destroy();
            /*-dbg-*/Lg.dbg("done");
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while promoting change request form %s",formobj,dfe);
            throw EEx.create("PromoteChangeRequest","error while promoting change request form %s",formobj,dfe);            
        }
    }
}
