package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.form.control.databound.DatagridRow;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.RejectWorkflowTask;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;

public class MdtRejectTask extends RejectWorkflowTask 
{
    
    boolean m_ismedtronicwf = false;
    
    IWorkflowTask m_task = null;
    String m_processname = null;
    String m_workflowname = null;
    String m_taskname = null;
    String m_mdtapp = null;
    String m_reasoncontrolname = null;
    Class m_reasoncontolclass = null;
    IDfDocument m_primarypackage = null;
    boolean m_rejectpathvalid = false;
    boolean m_rejectreasonvalid = false;
    
    public static final String DOCREJECT_RSNTXT_CONTROL_NAME = "MDT_REJECT_REASON";
    

    public void onInit(ArgumentList args) {
        try {
            super.onInit(args);
            
            m_task = getWorkflowTask();
            /*-dbg-*/Lg.dbg("getting signable document's medtronic application");
            m_primarypackage = WorkflowUtils.getPrimaryPackage(getDfSession(), m_task.getWorkflowId().getId(),m_task.getItemId().getId());
            if (m_primarypackage != null) {
                if (m_primarypackage.hasAttr("m_application"))
                    m_ismedtronicwf = true;
            }
            if (m_ismedtronicwf) {
            
                m_mdtapp = m_primarypackage.getString("m_application");            
                /*-dbg-*/Lg.dbg("MDT app: %s",m_mdtapp);

                /*-dbg-*/Lg.dbg("getting activity name and process name for config lookup");
                m_taskname = m_task.getActivityName();
                /*-dbg-*/Lg.dbg(" -- taskname: "+m_taskname);
                m_processname = ((IDfProcess)getDfSession().getObject(m_task.getProcessId())).getObjectName();
                /*-dbg-*/Lg.dbg(" -- process: "+m_processname);
                m_workflowname = ((IDfWorkflow)getDfSession().getObject(m_task.getWorkflowId())).getObjectName();
                /*-dbg-*/Lg.dbg(" -- workflow: "+m_workflowname);

                setClientEvent("onOk", args); // ?necessary?
            } 

        } catch (DfException excep) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:RejectWFT.onInit - Exception", null, excep);
            throw new RuntimeException("RejectWFT component init error",excep);
        } catch (Exception ex1) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:RejectWFT.onInit - Other unhandled Exceptions", null, ex1);
            throw new RuntimeException("RejectWFT component init error",ex1);
        }

    }
    
    public void onRender() {
        super.onRender();
        setDoValidation(false); // ?necessary?

    }
    
    void performValidation() throws DfException 
    {
        /*-dbg-*/Lg.dbg("check if signoff is req'd");
        if (m_task.isSignOffRequired()) {
            setDoValidation(true);

            // need to check reject paths' checkboxes for: at least one checked
            /*-dbg-*/Lg.dbg("checking Reject checkbox datagrid for at-least-one-checked");
            Datagrid rejectgrid = (Datagrid)getControl(RejectWorkflowTask.NEXT_TASKS_GRID_CONTROL_NAME);
            Iterator i = rejectgrid.getContainedControls();
            // this seems to be infinitely looped, <sarcasm>Nice Iterator guys</sarcasm> <shrug>then again, I've seen it not infinitely looped...</shrug>
            HashSet resultset = new HashSet(); // for tracking what we've seen and haven't seen yet so we can tell when the infinitely looping iterator has looped back around
            
            /*-dbg-*/Lg.dbg("iterating through reject datagrid controls, adding them to our set of checkboxes if they are of type checkbox");
            while (i.hasNext())
            {
                Control current = (Control)i.next();
                /*-dbg-*/if(Lg.dbg())Lg.dbg("check if current control is a DatagridRow: %s",current.getName());
                
                if (current instanceof DatagridRow)
                {
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("The datagrid control: %s is instance of DatagridRow",current.getName());
                    DatagridRow row = (DatagridRow)current;
                    Iterator cboxes = row.getContainedControls();
                    String cboxname = "";
                    while (cboxes.hasNext())
                    {
                        Control subcurrent = (Control)cboxes.next();
                        if (subcurrent instanceof Checkbox)
                        {
                            cboxname = subcurrent.getName();
                            /*-dbg-*/Lg.dbg("The control: %s is instance of Checkbox", cboxname);
                            break; // break from datagridrow's infinite iterator
                        }
                    }
                    // check if we've seen this checkbox before...
                    if (resultset.contains(cboxname))
                        break; // break from datagrid's infinite iterator
                    else
                    {
                        // we haven't, so add this to the list/set of checkboxes and keep looking for new checkboxes
                        resultset.add(cboxname);
                        /*-dbg-*/Lg.dbg("adding cbox named %s to cbox search results",cboxname);
                    }
                }
            }
            
            // okay, now we should have a list of checkbox control names we can validate...that was entirely too complicated
            // CEM: this seems to be the only place that reliably can access the reject path grid. Do auto-select for single path option sets.
            int checkedcount = 0;
            int boxcount = 0;
            Checkbox firstcbox = null;
            m_rejectpathvalid = false;
            Iterator foundcboxesiterator = resultset.iterator();
            /*-dbg-*/Lg.dbg("scan checkbox search results to validate number of paths selected");
            while (foundcboxesiterator.hasNext())
            {
                String name = (String)foundcboxesiterator.next();
                /*-dbg-*/Lg.dbg("examining checkbox: %s",name);
                Checkbox cbox = (Checkbox)getControl(name);
                if (0 == boxcount)
                    firstcbox = cbox;
                boxcount++;                
                if (cbox.getValue())
                {
                    /*-dbg-*/Lg.dbg("checkbox is checked: %s",name);
                    m_rejectpathvalid = true;
                    /*-dbg-*/Lg.dbg(" iterating checkedcount");
                    checkedcount++;
                    /*-dbg-*/Lg.dbg("checkedcount is %d",checkedcount);
                }
            }
            
            if (1 == boxcount)
            {
                /*-dbg-*/Lg.dbg("only one selectable path, so autoselect the single reject path");
                if (!firstcbox.getValue())
                {
                    firstcbox.setValue(true);
                    checkedcount++;
                }
                m_rejectpathvalid = true;
            }            
            validate();
        }
        else
        {
            /*-dbg-*/Lg.dbg(":No signoff is req'd");
        }
        
        /*-dbg-*/Lg.dbg("check rejection signoff reason");
        m_rejectreasonvalid = checkReason();
        /*-dbg-*/Lg.dbg("end of method--reject validation states:: rejectpath: %b  rejectreason: %b",m_rejectpathvalid,m_rejectreasonvalid);
    
    }

    boolean onRejectTasks() throws DfException 
    {
        String reasonCtrlVal = "";
        
        //Capture the rejection reasion
        /*-dbg-*/Lg.dbg("get rejection reason");
        Map crconfig = WorkflowUtils.getChangeRequestConfig(getDfSession().getSessionManager(), getDfSession().getDocbaseName(), m_mdtapp, m_primarypackage);
        Text rsnCtrl = (Text) getControl(DOCREJECT_RSNTXT_CONTROL_NAME, Text.class);
        if (rsnCtrl != null) {
            reasonCtrlVal = rsnCtrl.getValue();
        }
        /*-dbg-*/Lg.dbg("reject reason: %s",reasonCtrlVal);
        
        IDfSessionManager syssmgr = WorkflowUtils.getSystemSessionManager(getDfSession(), m_mdtapp);
        IDfSession syssession = null;
        try { 
            
            /*-dbg-*/Lg.dbg("get system session");
            syssession = syssmgr.getSession(getDfSession().getDocbaseName());
            /*-dbg-*/Lg.dbg("reretrieve form object under system user");
            IDfSysObject formobj = (IDfSysObject)syssession.getObject(m_primarypackage.getObjectId());

            /*-dbg-*/Lg.dbg("descend one version, since the current will be destroyed/rolled back very soon");
            formobj = (IDfSysObject)syssession.getObject(formobj.getAntecedentId());
    
            // standard action: add reject reason to form reject reasons attribute...
            /*-dbg-*/Lg.dbg("lookup reject reason attribute from config");
            String rejectreasonattrname = (String)crconfig.get("RejectReasonsAttribute");
            /*-dbg-*/Lg.dbg("lookup reject record attribute from config (stores who the reason is from, etc)");
            String rejectrecordattrname = (String)crconfig.get("RejectRecordsAttribute");
        
            if (rejectreasonattrname != null) {
                /*-dbg-*/Lg.dbg("appending reject reason on formobj %s to attr %s :: %s",formobj,rejectreasonattrname,reasonCtrlVal);
                formobj.appendString(rejectreasonattrname, reasonCtrlVal);
            }
            if (rejectrecordattrname != null) {
                String rejector = getDfSession().getLoginUserName();
                /*-dbg-*/Lg.dbg("appending rejecter %s on formobj %s to attr %s",rejector,formobj,rejectreasonattrname);
                formobj.appendString(rejectrecordattrname, rejector);
            }
            
            /*-dbg-*/Lg.dbg("save changes");
            formobj.save();
            /*-dbg-*/Lg.dbg("saved");
            
        } finally {
            try{syssmgr.release(syssession);}catch(Exception e){}
        }
        return true;
    }


    public boolean onCommitChanges() {

        if (m_ismedtronicwf) {
            boolean canCommit = false;
    
            try {
                performValidation();
                /*-dbg-*/Lg.dbg("check if we can commit yet (may need to prevalidate the reject password here...)");
                if (super.canCommitChanges() && m_rejectpathvalid && m_rejectreasonvalid) {
                    /*-dbg-*/Lg.dbg("calling super.onCommitChanges()");
                    canCommit = super.onCommitChanges(); // CEM: note: this triggers the next task, so an autotask be kicked off...
                    /*-dbg-*/Lg.dbg("returned canCommit: %b",canCommit);
                }
                if (canCommit) {
                    /*-dbg-*/Lg.dbg("proceeding with rejection - process rejection reasons");
                    canCommit = onRejectTasks();  // TODO ?make this configurable?
                    /*-dbg-*/Lg.dbg("set success message");
                    MessageService.addMessage(this, "MSG_FINISH_SUCCESS", new Object[] {super.getString("MSG_OBJECT")});
                }//commit ends
            } catch (DfException e1) {
                /*-WARN-*/Lg.wrn("Exception occurred in rejection, checking severity",e1);
                canCommit = false;
                try {
                    if (m_task.getCompleteErrStatus() == 4) {
                        /*-WARN-*/Lg.wrn("incorrect password given for rejection");
                        setReturnError("MSG_INCORRECT_PASSWORD", null, null);
                        WebComponentErrorService.getService().setNonFatalError(this, "MSG_INCORRECT_PASSWORD", null, null);
                        return false;
                    } else {
                        /*-ERROR-*/Lg.err("Exception occurred in rejection - Unexpected Error ",e1);
                        setReturnError("MSG_UNEXPECTED_ERROR", new Object[] { e1.getMessage() }, e1);
                        WebComponentErrorService.getService().setNonFatalError(this, "MSG_UNEXPECTED_ERROR", new Object[] { e1.getMessage() }, e1);
                        throw e1;
                    }
                } catch (DfException e2) {
                    /*-ERROR-*/Lg.err("error processing REJECT error message", e2);
                    throw new WrapperRuntimeException(e2);
                }
            }
            return canCommit;
        } else {
            return super.onCommitChanges();
        }
    }
    
    boolean checkReason() 
    {
        /*-dbg-*/Lg.dbg("check that reject reason was entered (not empty)");
        boolean enteredReason = false;
        Text rsnCtrl = (Text) getControl(DOCREJECT_RSNTXT_CONTROL_NAME, Text.class);

        if ((rsnCtrl.getValue() == null) || ((rsnCtrl.getValue().trim()).equals(""))) {
                rsnCtrl.setFocus();
                enteredReason = false;
        } else {
            enteredReason = true;
        }
        /*-dbg-*/Lg.dbg("returning check reject reason: %b",enteredReason);
        return enteredReason;
    }
   
}
