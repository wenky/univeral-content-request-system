package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.signaturereasons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSigningReasons;

// gets reason or reasons from an attribute on a document, with the document specified by a full path+document name.

public class ValueDocumentSigningReasons implements IMdtSigningReasons 
{
    // reason or selectable reasons are stored in an attribute on the form
    public List getReasons(String processname,String workflowname,IWorkflowTask task,Map taskconfig,Map context)
    {
        IDfSysObject sysobj = null;
        try {
            Map pluginconfig = (Map)context;
            String documentpath = (String)pluginconfig.get("Document");
            String attributename = (String)pluginconfig.get("Attribute");
            try {
                sysobj = (IDfSysObject)task.getObjectSession().getObjectByPath(documentpath);
            } catch (DfException dfe1) {
                /*-ERROR-*/Lg.err("Exception getting signing reasons document %s",documentpath,dfe1);
                throw EEx.create("ValDocSignReasons-GetDoc","Exception getting signing reasons document %s",documentpath,dfe1);                            
            }
            if (sysobj.isAttrRepeating(attributename)) {
                // get list of reasons from repeating attribute
                List reasons = new ArrayList();
                for (int i=0; i < sysobj.getValueCount(attributename); i++) {
                    reasons.add(sysobj.getRepeatingString(attributename, i));
                }
                return reasons;
            } else {
                List reasons = new ArrayList();
                reasons.add(sysobj.getString(attributename));
                return reasons;
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error getting signing reasons from valuedoc %s",sysobj,dfe);
            throw EEx.create("ValDocSignReasons-GetReasons","Error getting signing reasons from valuedoc %s",sysobj,dfe);            
        }
    }


}

