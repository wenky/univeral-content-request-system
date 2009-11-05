package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtValidateForm implements IMdtWorkflowValidation {

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {
        boolean success = true;
        
        
        try {
            // validate it is base state
            if (formobj.getCurrentState() != 0) {
                if (context!=null && ((Map)context).containsKey("BaseStateError")) {
                    errors.add(MdtErrorService.renderErrorMessage("BaseStateError",(Map)context,mgr,docbase,formobj,null));
                } else {
                    errors.add("Form "+formobj.getObjectName()+" must be in the base state before being routed");
                }
                success = false;
            }
            // validate m_rollback_form is not "in-wf"; 
            if ("in-wf".equals(formobj.getString("m_rollback_form"))) {
                if (context!=null && ((Map)context).containsKey("InWorkflowError")) {
                    errors.add(MdtErrorService.renderErrorMessage("InWorkflowError",(Map)context,mgr,docbase,formobj,null));
                } else {
                    errors.add("Form "+formobj.getObjectName()+ " appears to be already routed");
                }
                success = false;            
            }
            
            // validate that tasks with attachment attributes have users in those attributes
            /*-dbg-*/Lg.dbg("getting config for mdtapp " );        
            MdtConfigService configsvc = MdtConfigService.getConfigService(mgr,docbase);
            Map config = (Map)configsvc.getAppConfig(mdtapp);
            /*-dbg-*/Lg.dbg("get CR defs");
            Map wfs = (Map)config.get("ChangeRequests");
            String formtype = formobj.getTypeName();
            /*-dbg-*/Lg.dbg("get cr config for %s",formtype);
            Map wf = (Map)wfs.get(formtype);
            /*-dbg-*/Lg.dbg("get tasks");
            Map tasks = (Map)wf.get("Tasks");
            if (context.containsKey("ValidateApprovers")) {
                Iterator taskiter = tasks.keySet().iterator();
                while (taskiter.hasNext())
                {
                    String taskname = (String)taskiter.next();
                    /*-dbg-*/Lg.dbg("task: %s",taskname);
                    Map task = (Map)tasks.get(taskname);
                    if (task.containsKey("PerformerAttribute")) {
                        /*-dbg-*/Lg.dbg("has form-based performer assignment, make sure attr has at least one value, and validate the users");
                        String performerattr = (String)task.get("PerformerAttribute");
                        /*-dbg-*/Lg.dbg("attr: %s",performerattr);
                        if (formobj.isAttrRepeating(performerattr)) {
                            /*-dbg-*/Lg.dbg("repeating, iterate values");
                            boolean hasone = false;
                            for (int p=0; p < formobj.getValueCount(performerattr); p++) {
                                String username = formobj.getRepeatingString(performerattr, p);
                                IDfUser user = null;
                                if (username == null || "".equals(username.trim())) {
                                    user = null;
                                } else {
                                    /*-dbg-*/Lg.dbg("looking up user %s",username);
                                    user = formobj.getSession().getUser(username);
                                }
                                // ?how do you tell if bad user? exception? isnull?
                                if (user == null) {
                                    /*-dbg-*/Lg.dbg("bad username %s",username);
                                    if (context!=null && ((Map)context).containsKey("BadUsernameError")) {
                                        errors.add(MdtErrorService.renderErrorMessage("BadUsernameError",(Map)context,mgr,docbase,formobj,null,new ErrKey("taskname",taskname),new ErrKey("username",username),new ErrKey("performerattribute",performerattr)));
                                    } else {
                                        errors.add("Performer "+username+" for task "+taskname+" could not be found in the system");
                                    }
                                    success = false;
                                } else {
                                    hasone = true;
                                }
                            }
                            if (!hasone) {
                                /*-dbg-*/Lg.dbg("repeating attr was empty");
                                if (context!=null && ((Map)context).containsKey("NoUsersAssignedError")) {
                                    errors.add(MdtErrorService.renderErrorMessage("NoUsersAssignedError",(Map)context,mgr,docbase,formobj,null, new ErrKey("taskname",taskname),new ErrKey("performerattribute",performerattr)));
                                } else {
                                    errors.add("Performers for task "+taskname+" have not been assigned yet.");
                                }
                                success = false;
                            }
                        } else {
                            String username = formobj.getString(performerattr);
                            /*-dbg-*/Lg.dbg("check single value %s",username);
                            if (username == null || "".equals(username.trim())) {
                                /*-dbg-*/Lg.dbg("user attr was empty");
                                if (context!=null && ((Map)context).containsKey("NoUsersAssignedError")) {
                                    errors.add(MdtErrorService.renderErrorMessage("NoUsersAssignedError",(Map)context,mgr,docbase,formobj,null,new ErrKey("taskname",taskname),new ErrKey("performerattribute",performerattr)));
                                } else {
                                    errors.add("Performers for task "+taskname+" have not been assigned yet.");
                                }
                                success = false;
                            } else { 
                                IDfUser user = formobj.getSession().getUser(username);
                                // ?how do you tell if bad user? exception? isnull?
                                if (user == null) {
                                    /*-dbg-*/Lg.dbg("user %s not found",username);
                                    if (context!=null && ((Map)context).containsKey("BadUsernameError")) {
                                        errors.add(MdtErrorService.renderErrorMessage("BadUsernameError",(Map)context,mgr,docbase,formobj,null,new ErrKey("taskname",taskname),new ErrKey("username",username),new ErrKey("performerattribute",performerattr)));
                                    } else {
                                        errors.add("Performer "+username+" for task "+taskname+" could not be found in the system");
                                    }
                                    success = false;
                                }
                            }                    
                        }
                    }            
                }
            }
    
            // validate that the attachment attribute has attachments in it
            if (wf.containsKey("AttachmentsAttribute")) {
                String attachattr = (String)wf.get("AttachmentsAttribute");
                /*-dbg-*/Lg.dbg("check attachments attribute %s",attachattr);                
                if (formobj.getValueCount(attachattr) < 1) {
                    /*-dbg-*/Lg.dbg("attachments attribute is empty");                
                    if (context!=null && ((Map)context).containsKey("NoAttachmentsError")) {
                        errors.add(MdtErrorService.renderErrorMessage("NoAttachmentsError",(Map)context,mgr,docbase,formobj,null,new ErrKey("attachmentsattribute",attachattr)));
                    } else {
                        errors.add("Routable attachments have not been added to the form");
                    }
                    success = false;                
                }
                // validate that attachments are valid objects
                for (int i=0; i < formobj.getValueCount(attachattr); i++) {
                    String attname = formobj.getRepeatingString(attachattr, i);
                    /*-dbg-*/Lg.dbg("check to see if %s exists",attname);
                    try {
                        IDfSysObject att = AttachmentUtils.lookupAttachmentByName(formobj, attname);
                        if (att == null)
                            // lazy...
                            throw new DfException();
                    } catch (DfException dfe) {
                        // retrieval failure
                        success = false;
                        /*-dbg-*/Lg.dbg("attachment %s not found",attname);
                        if (context!=null && ((Map)context).containsKey("BadAttachmentError")) {
                            errors.add(MdtErrorService.renderErrorMessage("BadAttachmentError",(Map)context,mgr,docbase,formobj,null,new ErrKey("attachmentname",attname),new ErrKey("attachmentsattribute",attachattr)));
                        } else {
                            errors.add("Attachment "+attname+" not found in docbase");
                        }
                        success = false;
                    }
                }
            }
            /*-dbg-*/Lg.dbg("check if form is locked");
            if (formobj.isCheckedOut()) {
                /*-dbg-*/Lg.dbg("form is locked out");
                if (context!=null && ((Map)context).containsKey("AttachmentLockedError")) {
                    errors.add(MdtErrorService.renderErrorMessage("AttachmentLockedError",(Map)context,mgr,docbase,formobj,null));
                } else {
                    errors.add("Form "+formobj.getObjectName()+" is locked/checked out");
                }
                success = false;
                
            }

            
            /*-dbg-*/Lg.dbg("check if form is CURRENT");
            if (!formobj.getLatestFlag()) {
                /*-dbg-*/Lg.dbg("form is not current");
                if (context!=null && ((Map)context).containsKey("AttachmentNotCurrentError")) {
                    errors.add(MdtErrorService.renderErrorMessage("AttachmentNotCurrentError",(Map)context,mgr,docbase,formobj,null));
                } else {
                    errors.add("Form "+formobj.getObjectName()+" version "+formobj.getVersionLabels().getImplicitVersionLabel()+" is not the most CURRENT or recent version");
                }
                success = false;
                
            }

        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking form object standard validations %s",formobj,dfe);
            throw EEx.create("WFValidateForm-DFE","Error checking form object standard validations %s",formobj,dfe);
        }
        
        /*-dbg-*/Lg.dbg("return %b",success);
        return success;
        
    }
    

}
