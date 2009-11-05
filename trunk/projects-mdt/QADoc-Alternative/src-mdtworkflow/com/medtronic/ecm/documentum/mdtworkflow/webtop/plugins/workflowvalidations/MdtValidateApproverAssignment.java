package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.ArrayList;
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
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtValidateApproverAssignment implements IMdtWorkflowValidation {

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {
        boolean success = true;
        
        try {
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
                    List validperformers = new ArrayList();
                    if (formobj.isAttrRepeating(performerattr)) {
                        /*-dbg-*/Lg.dbg("repeating, iterate values");
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
                                validperformers.add(username);
                            }
                        }
                    } else {
                        String username = formobj.getString(performerattr);
                        /*-dbg-*/Lg.dbg("check single value %s",username);
                        if (username == null || "".equals(username.trim())) {
                            /*do nothing*/
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
                            } else {
                                validperformers.add(username);
                            }
                        }                    
                    }
                    // validate performers: min, max, optional
                    boolean needed = true;
                    if (context!=null && context.containsKey(performerattr+".optional")) {
                        // get controlling boolean attribute
                        String switchattr = (String)context.get(performerattr+".optional");
                        needed = formobj.getBoolean(switchattr);
                    } 
                    
                    if (needed){
                        // must have at least one
                        if (validperformers.size() < 1) {
                            if (context!=null && ((Map)context).containsKey("NoUsersAssignedError")) {
                                errors.add(MdtErrorService.renderErrorMessage("NoUsersAssignedError",(Map)context,mgr,docbase,formobj,null,new ErrKey("taskname",taskname),new ErrKey("performerattribute",performerattr)));
                            } else {
                                errors.add("Performers for task "+taskname+" have not been assigned yet.");
                            }
                            success = false;                            
                        }
                        // check for minimum enforcement
                        if (context!=null && context.containsKey(performerattr+".minapprovers")) {
                            Object o = context.get(performerattr+".minapprovers");
                            int mincount = Integer.parseInt(o.toString());
                            if (mincount > validperformers.size()) {
                                errors.add(""+validperformers.size()+" is not enough approvers, at least "+mincount+" valid performers needed for task "+taskname);                                
                                success = false;
                            }
                        }
                        // check for maximum enforcement
                        if (context!=null && context.containsKey(performerattr+".maxapprovers")) {
                            Object o = context.get(performerattr+".maxapprovers");
                            int maxcount = Integer.parseInt(o.toString());
                            if (maxcount > validperformers.size()) {
                                errors.add(""+validperformers.size()+" is too many approvers, no more than "+maxcount+" valid performers can be assigned for task "+taskname);                                
                                success = false;
                            }
                        }
                        // TODO: others?
                    }
                }            
            }
            
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking form object standard validations %s",formobj,dfe);
            throw EEx.create("WFValidateForm-DFE","Error checking form object standard validations %s",formobj,dfe);
        }
        
        /*-dbg-*/Lg.dbg("return %b",success);
        return success;
        
    }
    

}
