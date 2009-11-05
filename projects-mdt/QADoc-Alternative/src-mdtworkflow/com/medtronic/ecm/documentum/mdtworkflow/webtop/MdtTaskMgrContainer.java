package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfId;
import com.documentum.services.workflow.inbox.ITask;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Panel;
import com.documentum.webcomponent.library.workflow.taskmgrcontainer.TaskMgrContainer;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MdtTaskMgrContainer extends TaskMgrContainer
{

    boolean m_ismedtronicwf = false;
    
    IWorkflowTask m_task = null;
    String m_processname = null;
    String m_workflowname = null;
    String m_taskname = null;
    String m_mdtapp = null;
    IDfDocument m_primarypackage = null;

    public void onInit(ArgumentList argumentlist)
    {
        try {
            /*-dbg-*/Lg.dbg("calling super()");
            super.onInit(argumentlist);
                        
            String taskId = argumentlist.get("objectId");
            /*-dbg-*/Lg.dbg("checking taskid %s",taskId);
            ITask curtask = getInboxService().getTask(new DfId(taskId), true);
            /*-dbg-*/Lg.dbg("check if this is a Workflow task");
            if (curtask.getType() == 4 && curtask instanceof IWorkflowTask)
            {
                m_task = (IWorkflowTask)curtask;
                /*-dbg-*/Lg.dbg("check if this is a medtronic workflow/task");
                m_primarypackage = WorkflowUtils.getPrimaryPackage(getDfSession(), m_task.getWorkflowId().getId(), m_task.getItemId().getId());
                if (m_primarypackage != null) {
                    if (m_primarypackage.hasAttr("m_application"))
                        m_ismedtronicwf = true;
                }
                if (m_ismedtronicwf) {
                    
                    /*-dbg-*/Lg.dbg("getting signable document's medtronic application");
                    m_mdtapp = m_primarypackage.getString("m_application");            
                    /*-dbg-*/Lg.dbg("MDT app: %s",m_mdtapp);
                    
                    /*-dbg-*/Lg.dbg("getting activity name and process name for config lookup");
                    m_taskname = m_task.getActivityName();
                    /*-dbg-*/Lg.dbg(" -- taskname: "+m_taskname);
                    m_processname = ((IDfProcess)getDfSession().getObject(m_task.getProcessId())).getObjectName();
                    /*-dbg-*/Lg.dbg(" -- process: "+m_processname);
                    m_workflowname = ((IDfWorkflow)getDfSession().getObject(m_task.getWorkflowId())).getObjectName();
                    /*-dbg-*/Lg.dbg(" -- workflow: "+m_workflowname);
                    
                    // TODO - custom widgets....can we do a programmatic component include?
    
                    String page = this.getComponentPage();
                    // MRCS is disabling REPEAT for now...
                    /*-dbg-*/Lg.dbg("disabling repeat button");
                    getControl(TaskMgrContainer.REPEAT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                    
                    boolean validationsuccess = true;
                    List validationerrors = new ArrayList(); 
                    /*-dbg-*/Lg.dbg("get task config");
                    Map taskcfg = WorkflowUtils.getTaskConfig(getDfSession().getSessionManager(), getDfSession().getDocbaseName(), m_mdtapp, m_task.getTaskName(), m_primarypackage);
                    
                    /*-dbg-*/Lg.dbg("check if there are task validations");
                    if (taskcfg.containsKey("TaskValidations"))
                    {
                        /*-dbg-*/Lg.dbg("executing Task Validations");
                        // TODO: task validations plugins if needed
                        // Validation Errors to date have only been related to rendition generation being complete
                        // -- we will avoid task validation plugins until a second use case comes along
                        // validationerrors = taskvalidator.validate(this,task,workflow,process,formobj,context);
                        
                        if (validationerrors == null || validationerrors.size() ==0)
                        {
                            validationsuccess = false;
                        } 
                        
                    } else if (taskcfg.containsKey("ValidateRenditions")) {                        
                        String format = (String)taskcfg.get("ValidateRenditions");
                        if (!WorkflowUtils.checkRenditions(m_primarypackage, format)) {
                            validationsuccess = false;
                            if (taskcfg.containsKey("ValidateRenditionsError")) {
                                validationerrors.add(MdtErrorService.renderErrorMessage("ValidateRenditionsError",null,getDfSession().getSessionManager(),getDfSession().getDocbaseName(),m_primarypackage,null,new ErrKey("format",format)));                                
                            } else {
                                validationerrors.add("Rendition not yet generated for form "+m_primarypackage.getObjectName());                                                                
                            }                            
                        }
                            
                        // handle the only use case to date...renditions not generated
                        List attachments = WorkflowUtils.getMostRecentAttachments(getDfSession(), m_task.getWorkflowId().getId());
                        if (attachments !=null)
                            for (int i=0; i < attachments.size(); i++) {
                                IDfSysObject attobj = (IDfSysObject)attachments.get(i);
                                Lg.wrn("TEst sysobject log: %s",attobj);
                                if (!WorkflowUtils.checkRenditions(attobj, format)) {                                    
                                    validationsuccess = false;
                                    if (taskcfg.containsKey("ValidateRenditionsError")) {
                                        validationerrors.add(MdtErrorService.renderErrorMessage("ValidateRenditionsError",null,getDfSession().getSessionManager(),getDfSession().getDocbaseName(),attobj,null,new ErrKey("format",format)));                                
                                    } else {
                                        validationerrors.add("Rendition not yet generated for attachment "+attobj.getObjectName());                                                                
                                    }                            
                                }
                            }
                    } 
                    
                    /*-dbg-*/Lg.dbg("check for validation failure");
                    if (!validationsuccess)
                    {
                        /*-dbg-*/Lg.dbg("get reference to mdtvalidationerror Label control");
                        Label errmsg = (Label)getControl("mdtvalidationerror",Label.class);
                        /*-dbg-*/Lg.dbg("make validation error label control visible");
                        errmsg.setVisible(true);
                        // generate validation errors label
                        /*-dbg-*/Lg.dbg("set validation label control text to html-ized validation messages");
                        String errorstring = "<BR>VALIDATION ERRORS<BR>  <BR>";
                        for (int errs = 0; errs < validationerrors.size(); errs++)
                        {
                            // get NLS id from error list
                            String err = (String)validationerrors.get(errs);
                            /*-dbg-*/Lg.dbg("append errmesg %s",err);
                            errorstring += "  -->"+err+"<BR>";
                        }
                        errmsg.setLabel(errorstring);
                        /*In some cases the user needs to accept the task.  Accept should behave as default.*/
                        //getControl(TaskMgrContainer.ACCEPT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                        /*-dbg-*/Lg.dbg("disabling finish, forward, delegate, and repeat, but reject is enabled");
                        getControl(TaskMgrContainer.FINISH_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                        getControl(TaskMgrContainer.FORWARD_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                        getControl(TaskMgrContainer.REJECT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(true); // allow them to reject
                        getControl(TaskMgrContainer.REJECT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setEnabled(true); // allow them to reject
                        getControl(TaskMgrContainer.DELEGATE_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                        getControl(TaskMgrContainer.REPEAT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                        /*-dbg-*/Lg.dbg("validation failure processing done");
                    }
                    
                }
            }
        } catch (Exception dfe) {
            /*-ERROR-*/Lg.err("Exception in initialization of task maanger container",dfe);
            throw EEx.create("TaskMgrContainer-Init","Exception in initialization of task maanger container",dfe);            
        }
    }

}
