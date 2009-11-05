package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.scheduledtask;

import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

public class QADocSetApprovalDate extends MdtProcessAttachments 
{

    public void processAttachment(IDfSessionManager smgr, String docbase, String mdtapp, IDfSysObject jobobject, IDfSysObject formobj, IDfSysObject attachdoc, Map jobparameters, Map context) 
    {
        try { 
            String attrname = (String)((Map)context).get("EffectiveDateAttrName");
            attachdoc.setTime(attrname, new DfTime(new Date()));
            attachdoc.save();
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error setting effective date on doc %s",attachdoc,dfe);
            throw EEx.create("ScheduledSetEffectiveDate","Error setting effective date on doc %s",attachdoc,dfe);
        }
        
    }

}
