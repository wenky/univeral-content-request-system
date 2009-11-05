package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowTemplateResolver;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtValidateWorkflowTemplatePermissions implements IMdtWorkflowValidation 
{

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {
        Map config = (Map)context;   
        String wftname = resolveWFT(mgr,docbase,mdtapp,formobj,config);
        boolean permitcheck = checkPermitWFT(mgr,docbase,wftname);
        if (!permitcheck) {
            errors.add("Need RELATE permission on workflow template "+wftname);            
        }
        return false;
    }
    
    boolean checkPermitWFT(IDfSessionManager mgr, String docbase, String wftname) 
    {
        IDfSession session = null;
        try { 
            session= mgr.getSession(docbase);
            String qual = "dm_process where object_name = '"+wftname+"'";
            IDfSysObject workflowobj = (IDfSysObject)session.getObjectByQualification(qual);
            int permit = workflowobj.getPermit();
            /*-dbg-*/Lg.dbg("is permit %d greater than relate?",permit);
            if (permit >= IDfACL.DF_PERMIT_RELATE) {
                return true;
            }
            return false;
        } catch (DfException dfe){
            /*-ERROR-*/Lg.err("Exception checking permit on wft %s",wftname,dfe);
            throw EEx.create("VldtWFTPermit-CheckWFTPermit","Exception checking permit on wft %s",wftname,dfe);                                   
        } finally {
            try {mgr.release(session);}catch(Exception e){}            
        }
    }
    
    String resolveWFT(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, Map config) 
    {
        try {
            MdtConfigService cfgsvc = MdtConfigService.getConfigService(mgr, docbase);
            /*-dbg-*/Lg.dbg("approval form mdt app: %s",mdtapp);
            /*-dbg-*/Lg.dbg("lookup config for mdt app");
            Map mdtcfg = (Map)cfgsvc.getAppConfig(mdtapp);
            // determine workflow/process id to be launched
            /*-dbg-*/Lg.dbg("Default workflow/approval from resolution - get Change Requests map from config");
            Map workflows = (Map)mdtcfg.get("ChangeRequests");
            /*-dbg-*/Lg.dbg("get change request for approval form type");
            Map wfdef = (Map)workflows.get(formobj.getTypeName());
            /*-dbg-*/Lg.dbg("get DCTM wofklow template dm_process name");
            String processname = (String)wfdef.get("WorkflowTemplate");
            if (processname == null) {
                // see if there is a resolver configured
                if (wfdef.containsKey("WorkflowTemplateResolver")) {
                    MdtPlugin resolverplugin = (MdtPlugin)wfdef.get("WorkflowTemplateResolver");
                    IMdtWorkflowTemplateResolver resolver = (IMdtWorkflowTemplateResolver)MdtPluginLoader.loadPlugin(resolverplugin, mgr);
                    processname = resolver.resolveWFT(mgr,docbase, mdtapp, formobj, resolverplugin.context);
                }
            }
            return processname;
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Exception resolving form's wft name %s",formobj,dfe);
            throw EEx.create("VldtWFTPermit-ResolveWFT","Exception resolving form's wft name %s",formobj,dfe);                       
        }
    }


}
