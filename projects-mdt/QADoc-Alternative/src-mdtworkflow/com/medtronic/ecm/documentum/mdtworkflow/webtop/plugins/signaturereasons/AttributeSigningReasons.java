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

// signing reason or reasons are stored in an attribute on the change request/approval form document
// features that can be employed by this strategy:
//  - hardcoded list of values in a form document template
//  - value assistance or value assistance query
//  - QA doc or change request author-specified reasons

public class AttributeSigningReasons implements IMdtSigningReasons 
{
    // reason or selectable reasons are stored in an attribute on the form
    public List getReasons(String processname,String workflowname,IWorkflowTask task,Map taskconfig,Map context)
    {
        IDfSysObject sysobj = null;
        try {
            Map pluginconfig = (Map)context;
            String attributename = (String)pluginconfig.get("Attribute");
            try {
                sysobj = WorkflowUtils.getPrimaryPackage(task.getObjectSession(),task.getWorkflowId().getId(), task.getItemId().getId());
            } catch (DfException dfe1) {
                /*-ERROR-*/Lg.err("Exception getting change request form to resolve signing reasons",dfe1);
                throw EEx.create("AttrSignReasons-getForm","Exception getting change request form to resolve signing reasons",dfe1);                            
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
            /*-ERROR-*/Lg.err("getting signing reasons for form %s",sysobj,dfe);
            throw EEx.create("AttrSignReasons-GetReasons","getting signing reasons for form %s",sysobj,dfe);            
        }
    }


}
