package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.webcomponent.library.workflow.abort.AbortWorkflow;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;

public class MdtAbortWorkflow extends AbortWorkflow
{
    public boolean onCommitChanges()
    {
        // get workflow id
        /*-dbg-*/Lg.dbg("get workflow id from superclass");
        String workflowid = super.m_strWorkflowId;
        /*-dbg-*/Lg.dbg("-- wfid: %s",workflowid);
        // check if it is a MdtWorkflow
        try { 
            /*-dbg-*/Lg.dbg("get primary package");
            IDfSysObject pkg = null;
            try {
                pkg = WorkflowUtils.getPrimaryPackage(getDfSession(),workflowid);
            } catch (Exception e) {
                //warn and do normal OOTB aborting
                /*-WARN-*/Lg.dbg("ERROR in getting workflow package for wfid: %s, proceeding with standard abort",workflowid);                
            }
            if (pkg != null)
            {                
                /*-dbg-*/Lg.dbg("package exists, is %s a mdt_workflow_form instance?",pkg);
                if (pkg.isInstanceOf("mdt_workflow_form")) {
                    /*-dbg-*/Lg.dbg("get mdtapp of Mdt Workflow Form %s",pkg);
                    // attempt form and attachment rollback
                    String mdtapp = pkg.getString("m_application");
                    // switch to superuser

                    /*-dbg-*/Lg.dbg("switch to mdtapp %s system user",mdtapp);
                    IDfSession susession = null;
                    IDfSessionManager smgr = WorkflowUtils.getSystemSessionManager(getDfSession(), mdtapp);
                    try {
                        /*-dbg-*/Lg.dbg("get syssession");
                        susession = smgr.getSession(getDfSession().getDocbaseName());
                        
                        /*-dbg-*/Lg.dbg("get pkg as sysuser");
                        pkg = WorkflowUtils.getPrimaryPackage(susession,workflowid);
                        /*-dbg-*/Lg.dbg("checking on in-wf for the main approval form %s",pkg);
                        List attachments = WorkflowUtils.getMostRecentAttachments(susession,workflowid);                                                    
                        
                        // custom abort or is there an abort action specified?
                        Map wfconfig = WorkflowUtils.getChangeRequestConfig(smgr, susession.getDocbaseName(), mdtapp, pkg);
                        if (wfconfig.containsKey("AbortTask")) {
                            // use the specified AbortTask name in tasks to abort the workflow (i.e.: quickflow, obsolete aborts)
                            String aborttask = (String)wfconfig.get("AbortTask");
                            Map abortconfig = WorkflowUtils.getTaskConfig(smgr, susession.getDocbaseName(), mdtapp, aborttask, pkg);
                            List rollbackactions = (List)abortconfig.get("MethodActions");
                            for (int i=0; i < rollbackactions.size(); i++) {
                                MdtPlugin p = (MdtPlugin)rollbackactions.get(i);                                
                                IMdtWorkflowAction rollbackaction = (IMdtWorkflowAction)MdtPluginLoader.loadPlugin(p,smgr);
                                rollbackaction.execute(smgr, susession.getDocbaseName(), mdtapp, pkg, attachments, null, null, p.context);
                            }
                        } else {
                            //default rollback - destroy current version of form and attachments
                            // check that the pkg has the in-wf value in m_rollback_form, which indicates the initial form versioning was successful
                            String formrbstatus = pkg.getString("m_rollback_form"); 
                            if ("in-wf".equals(formrbstatus)) {                        
                                /*-dbg-*/Lg.dbg("approval form versioning was successful - iterate attachments");
                                for (int i=0; i < attachments.size(); i++) {
                                    IDfSysObject attachment = (IDfSysObject)attachments.get(i);
                                    // check that attachment was successfully versioned (it's in the m_rollback attr on the approval form obj)
                                    /*-dbg-*/Lg.dbg("make sure attachment id is in the form's m_rollback tracking attribute");
                                    String attid = attachment.getObjectId().getId();
                                    boolean found = false;
                                    for (int a=0; a < pkg.getValueCount("m_rollback"); a++) {
                                        if (attid.equals(pkg.getRepeatingString("m_rollback",a))) {
                                            found = true;
                                        }
                                    }
                                    /*-dbg-*/Lg.dbg("att found in attachment rollback tracking? "+found);
                                    if (found) {                                
                                        /*-INFO-*/Lg.inf("ROLLBACK attachment ROLLBACK -- destroy attachmetn %s",attachment);
                                        attachment.destroy();
                                    } else {
                                        /*-WARN-*/Lg.wrn("ROLLBACK attachment could not be rolled back due to initial versioning problems ROLLBACK --  %s",attachment);                                    
                                    }
                                }
                                /*-INFO-*/Lg.dbg("ROLLBACK form ROLLBACK -- destroy %s",pkg);
                                pkg.destroy();
                                /*-INFO-*/Lg.inf("ROLLBACK success ROLLBACK!");
                            }
                        }
                    } finally {
                        try { smgr.release(susession); } catch (Exception e) {}
                    }
                }
            }
        } catch (DfException dfe) {
            /*-err-*/Lg.err("MdtAbortWorkflow : Exception Occurred ",dfe);
            throw EEx.create("MdtAbortWorkflow-CheckPkg","MdtAbortWorkflow : Exception Occurred ",dfe);            
        }
        
        // proceed with abort and destroy
        /*-dbg-*/Lg.dbg("proceeding with workflow abort");
        return super.onCommitChanges();
    }
    
    
    

}
