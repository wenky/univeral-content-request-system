package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.signaturereasons;

import java.util.List;
import java.util.Map;

import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSigningReasons;

// signing reasons are explicitly listed/specified in the plugin config 

public class ConfigSignatureReasons implements IMdtSigningReasons 
{
    public List getReasons(String processname,String workflowname,IWorkflowTask task,Map taskconfig,Map context)
    {
        Map pluginconfig = (Map)context;
        List signingreasons = (List)pluginconfig.get("SigningReasons");
        return signingreasons;
    }


}
