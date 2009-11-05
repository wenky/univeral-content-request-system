package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;

public class MdtDetectOptionalApprovers implements IMdtWorkflowAction
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map cfg)
    {
        // clear configured attributes
        try {
            Map approverattrs = (Map)cfg.get("OptionalApprovers");
            /*-dbg-*/Lg.dbg("iterate through attributes");
            
            // make sure we have latest version of form object
            formobj = (IDfSysObject)formobj.getSession().getObjectByQualification("dm_sysobject where i_chronicle_id = '"+formobj.getChronicleId().getId()+"'");
            
            Iterator i = approverattrs.keySet().iterator();
            while (i.hasNext())
            {
                /*-dbg-*/Lg.dbg("get next attr");
                String attrname = (String)i.next();
                boolean hasvalue = false;
                /*-dbg-*/Lg.dbg("check if attr %s is repeating",attrname);
                if (formobj.isAttrRepeating(attrname)) {
                    /*-dbg-*/Lg.dbg("check repeating values");
                    if (formobj.getValueCount(attrname) > 0) {
                        String curval = formobj.getRepeatingString(attrname,0);
                        if (curval != null && !"".equals(curval.trim())) {
                            hasvalue = true;
                        }
                    }
                } else {
                    /*-dbg-*/Lg.dbg("single value");
                    String curval = formobj.getString(attrname);
                    if (curval != null && !"".equals(curval.trim())) {
                        hasvalue = true;
                    }
                }
                String flagattr = (String)approverattrs.get(attrname);
                /*-dbg-*/Lg.dbg("set attr %s to %b",flagattr,hasvalue);
                formobj.setBoolean(flagattr, hasvalue);
            }
            /*-dbg-*/Lg.dbg("save changes");
            formobj.save();
            /*-dbg-*/Lg.dbg("done");
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while promoting change request form %s",formobj,dfe);
            throw EEx.create("PromoteChangeRequest","error while promoting change request form %s",formobj,dfe);            
        }
    }
}

