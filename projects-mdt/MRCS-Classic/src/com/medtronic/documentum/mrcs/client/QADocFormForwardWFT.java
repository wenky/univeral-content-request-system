package com.medtronic.documentum.mrcs.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.form.control.databound.DatagridRow;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.ForwardWorkflowTask;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsWorkflowTask;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.ISignatureService;
import com.medtronic.documentum.mrcs.plugin.ISignatureValidation;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowActionPlugin;

// differences between forward and finish WFT:
// - forward usually has path selection tasks, finish does not
// - finish has reject reason display (and rollback, but we're going to offload the reject to a method invocation from now on)

public class QADocFormForwardWFT extends ForwardWorkflowTask 
{
	static public String USERNAME_CONTROL_NAME = "QADOC_SIGNOFF_USERNAME";
	static public String PASSWORD_CONTROL_NAME = "QADOC_SIGNOFF_PASSWORD";
	static public String REASONSELECT_CONTROL_NAME = "QADOC_SIGNOFF_REASON";
	
	IWorkflowTask m_task = null;
	String m_processname = null;
	String m_taskname = null;
	String m_mrcsapp = null;
	IDfDocument m_primarypackage = null;
	

    public void onInit(ArgumentList args) 
    {
        /*-CONFIG-*/String m="onInit-";
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling super()", null, null);
            super.onInit(args);

            // do we need to setclientevent(onOk)?
            //setClientEvent("onOk", args);

            m_task = getWorkflowTask();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"workflowtask: "+(m_task == null ? null : m_task.getActivityId().getId()), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(this,m+"getting first attachment - first get workitem",null,null);
    		IDfWorkitem wi = (IDfWorkitem)getDfSession().getObject(m_task.getId("item_id"));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(this,m+" _ GET ATTACHMENT COLLECTION",null,null);
    		IDfCollection attachments = wi.getAttachments();
            attachments.next();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(this,m+" - get r_comp_id",null,null);
            IDfId firstattachment = attachments.getId("r_component_id");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(this,m+" - look up doc "+firstattachment.getId(),null,null);
            m_primarypackage = (IDfDocument)getDfSession().getObject(firstattachment);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(this,m+" - get doc's mrcsapp",null,null);
            m_mrcsapp = m_primarypackage.getString("mrcs_application");          
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS app: "+m_mrcsapp, null, null);
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting activity name and process name for config lookup", null, null);
            m_taskname = m_task.getActivityName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- taskname: "+m_taskname, null, null);
            m_processname = ((IDfProcess)getDfSession().getObject(m_task.getProcessId())).getObjectName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- process: "+m_processname, null, null);
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Check if signoff controls are needed", null, null);
            if (m_task.isSignOffRequired()) {
                //Obtain the resons for signing and initialize the Control
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"signoff is required for this task - initializing reason list dropdown", null, null);
                DropDownList rsnListCtrl = (DropDownList) getControl(QADocFinishWFT.REASONSELECT_CONTROL_NAME,DropDownList.class);
                ESignHelper.initReasonList(m_mrcsapp, rsnListCtrl);
            } else {
                //setMessage("MSG_SIGN_NOTREQUIRED");
                //setValid(true);
            }
        } catch (Exception ex1) {
            /*-ERROR-*/DfLogger.error(this, m+"Exception in initialization of FinishWFT", null, ex1);
            throw new RuntimeException("Exception in initialization of MRCS FinishWFT component",ex1);
        }

    }
    
    String performValidation() throws DfException
    {
        /*-CONFIG-*/String m="performValidation-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Check if task requires signoff", null, null);
        if (m_task.isSignOffRequired()) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"flipping on validation", null, null);
            setDoValidation(true);
            
            // need to check forwarding paths' checkboxes for: at least one checked
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking forwarding checkbox datagrid for at-least-one-checked", null, null);
            Datagrid forwardgrid = (Datagrid)getControl(ForwardWorkflowTask.NEXT_TASKS_GRID_CONTROL_NAME);
            Iterator i = forwardgrid.getContainedControls();
            // this seems to be infinitely looped, <sarcasm>Nice Iterator guys</sarcasm> <shrug>then again, I've seen it not infinitely looped...</shrug>
            HashSet resultset = new HashSet(); // for tracking what we've seen and haven't seen yet so we can tell when the infinitely looping iterator has looped back around
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterating through controls, adding them to our set of checkboxes if they are of type checkbox", null, null);
            while (i.hasNext())
            {
                Control current = (Control)i.next();
                if (current instanceof DatagridRow)
                {
                    DatagridRow row = (DatagridRow)current;
                    Iterator cboxes = row.getContainedControls();
                    String cboxname = "";
                    while (cboxes.hasNext())
                    {
                        Control subcurrent = (Control)cboxes.next();
                        if (subcurrent instanceof Checkbox)
                        {
                            cboxname = subcurrent.getName();
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"found cbox named "+cboxname, null, null);
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
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding cbox named to cbox search results"+cboxname, null, null);
                    }
                }
            }
            // okay, now we should have a list of checkbox control names we can validate...that was entirely too complicated
            // CEM: this seems to be the only place that reliably can access the forward paths grid. Do auto-select for single path option sets.
            int checkedcount = 0;
            int boxcount = 0;
            Checkbox firstcbox = null;
            boolean forwardPathValidation = false;
            Iterator foundcboxesiterator = resultset.iterator();
            while (foundcboxesiterator.hasNext())
            {
                String name = (String)foundcboxesiterator.next();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"examining checkbox: "+name, null, null);
                Checkbox cbox = (Checkbox)getControl(name);
                if (0 == boxcount)
                	firstcbox = cbox;
                boxcount++;                
                if (cbox.getValue())
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checkbox is checked: "+name, null, null);
                    forwardPathValidation = true;
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterating checkedcount", null, null);
                    checkedcount++;
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checkedcount is "+checkedcount, null, null);
                }
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check for autoselect of single forward path", null, null);
            if (1 == boxcount)
            {
            	// TODO: does this interfere with reject? --> I would guess that reject triggers a RejectWFT object, not this one....
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"autochecking the single forward path", null, null);
            	if (!firstcbox.getValue())
            	{
            		firstcbox.setValue(true);
            		checkedcount++;
            	}
            	forwardPathValidation = true;            	
            }
            
            if (forwardPathValidation == false)
            {
            	return "MSG_FORWARD_PATH_NOT_SELECTED";
            }

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get State Transition config", null, null);
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get MRCS config task definition", null, null);
            MrcsWorkflowTask t = config.getMrcsWorkflowTask(m_mrcsapp, m_processname, m_taskname);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validating checked forward path count, # selected: "+checkedcount, null, null);
            //MJH 8-29-2006: updated workflow object to be IDfWorkflow from IDfSysobject, threw CCE in 5.3
            IDfProcess workflow = (IDfProcess)getDfSession().getObject(m_task.getProcessId());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"workflow system name: "+workflow.getObjectName(), null, null);
            if (!config.doesWorkflowTaskAllowMultiplePaths(m_mrcsapp,workflow.getObjectName(),m_task.getTaskName()))
            {
                if (checkedcount > 1)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"more than one path selected when only one is allowed", null, null);
                    return "MSG_MULTIPLE_PATHS_SELECTED_WHEN_ONLY_ONE_ALLOWED";
                }
            }

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get refs to signing controls", null, null);
            Text usernameCtrl = (Text)getControl(QADocFinishWFT.USERNAME_CONTROL_NAME,Text.class);
            Password passwordCtrl = (Password)getControl(QADocFinishWFT.PASSWORD_CONTROL_NAME,Password.class);
            DropDownList rsnListCtrl = (DropDownList)getControl(QADocFinishWFT.REASONSELECT_CONTROL_NAME,DropDownList.class);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get refs to signing controls", null, null);
            String username = usernameCtrl.getValue();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- user: "+username, null, null);
            String password = passwordCtrl.getValue();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- pass: "+(password == null ? "null" : "#########"), null, null);
            String reason = rsnListCtrl.getValue();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- reason: "+reason, null, null);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling super.validate()", null, null);
            validate();
            if (!getIsValid()) {
            	return "MSG_FORM_VALIDATION";
            }
            // if ((usrName != null) && (pswd != null) && (rsn)) {
            if ((username != null) && (username.trim().length() > 0)  && (password != null) && (reason != null) ) 
            {
                // CEM: weird bug - if password is correct, but username is wrong, then the task completes but fails when we sign -- after webtop has completed the task. 
                //      - so make sure the user matches the session user!
                String loggedinusername = getDfSession().getSessionManager().getIdentity(getDfSession().getDocbaseName()).getUser();
                if (!loggedinusername.equals(username))
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"entered username does not match current user", null, null);
                    return "MSG_USERNAME_MISMATCH";
                }
                else {
	                // apparently we need to ensure the password is correct...
	                IDfLoginInfo logininfo = new DfLoginInfo();
	                logininfo.setUser(username);
	                logininfo.setPassword(password);
	                try {
	                	getDfSession().authenticate(logininfo);
	                } catch (DfAuthenticationException authex) {
	                	// return code via exception...great design, guys
	                	return "MSG_BAD_PASSWORD";
	                }
                }
                
            } else {
                return "MSG_MISSING_USER_PASS_REASON";
            }
        }
        return null;
    }

    
    public void AutoSelectSinglePath()
    {
    	// TODO - can we do this for FinishWFT? 
    	/*-CONFIG-*/String m = "AutoSelectSinglePath - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check for premature Finish Click and single path selection", null, null);    	
    	// see if the forward paths grid was ever instantiated
    	Datagrid forwardgrid = (Datagrid)getControl(ForwardWorkflowTask.NEXT_TASKS_GRID_CONTROL_NAME);
    	// if not, check how many paths there would be
    	if (null == forwardgrid)
    	{
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"forwardgrid is null, so Finish clicked before selecting a path, see if we can autoselect", null, null);    	
    		try { 
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get wf task", null, null);    	
	    		IWorkflowTask task = this.getWorkflowTask();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get forward activities for task "+task.getActivityName(), null, null);    	
	            IDfList forwardAct = task.getNextForwardActivities();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Get count of next activities", null, null);    	
	            int count = forwardAct.getCount();
	            if (1 == count)
	            {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"only one next activity, so autoselect that one", null, null);    	
	            	// fake it baby
		            IDfTypedObject act = (IDfTypedObject)forwardAct.get(0);
		            String actId = act.getString("r_object_id");
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"id of next activity: "+actId, null, null);    	
	            	String cboxname = "__NEXT_TASKS_CHECKBOX_CONTROL_NAME" + actId;
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"creating fake checkbox for that activity: "+cboxname, null, null);    	
	            	Checkbox cbox = (Checkbox)getControl(cboxname,Checkbox.class);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"selecting the cbox", null, null);    	
	            	cbox.setValue(true);
	            	//dangerous levels of hackitude here...
	            	// because the superclass has the "list of next activities" property private and immutable from this class, I have to resort to this trickery
	            	//   page == forward forces the superclass to check for selected forward paths. if it's not forward, it just assumes the list is ready (which its not, and I
	            	//   can't make it ready since it's frikking PRIVATE. thanks, DCTM
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"set component page to forward so that the super.onCommitChanges call works correcly", null, null);    	
	            	this.setComponentPage("forward");
	            }
    		} catch (DfException dfe) {
    			throw new RuntimeException("Df Error in single path autoselect",dfe);
    		}
    	}
    	
    }

    public boolean onCommitChanges() 
    {
        /*-CONFIG-*/String m="onCommitChanges-";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get State Transition config", null, null);
        StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get MRCS config task definition", null, null);
        MrcsWorkflowTask t = config.getMrcsWorkflowTask(m_mrcsapp, m_processname, m_taskname);
    	
        boolean canCommit = false;
        try {
            AutoSelectSinglePath();

        	// perform basic CFR part 11 checks: must have valid user/pass/reason, user is user that is logged in, password is valid, reason is not empty/null
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check form and user/pass/reason validations", null, null);
            String validation = performValidation();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"result: "+(validation == null ? "valid" : validation), null, null);            
            if (validation != null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"User/Pass/Reason error detected: "+validation, null, null);
            	setErrorMessage(validation);
            	return false;
            }            
          
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get refs to signing controls", null, null);
            Text usernameCtrl = (Text)getControl(QADocForwardWFT.USERNAME_CONTROL_NAME,Text.class);
            Password passwordCtrl = (Password)getControl(QADocForwardWFT.PASSWORD_CONTROL_NAME,Password.class);
            DropDownList rsnListCtrl = (DropDownList)getControl(QADocForwardWFT.REASONSELECT_CONTROL_NAME,DropDownList.class);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get refs to signing controls", null, null);
            String username = usernameCtrl.getValue();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- user: "+username, null, null);
            String password = passwordCtrl.getValue();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- pass: "+(password == null ? "null" : "#########"), null, null);
            String reason = rsnListCtrl.getValue();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -- reason: "+reason, null, null);
            
            // HACK the class hierarchy: set Password on superclass, otherwise it won't successfully complete when calling super.onCommitChanges()
            Password passwdCtrl = (Password)getControl("__PASSWORD_CONTROL_NAME");            
            passwdCtrl.setValue(password);
            
            try { 
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check for signature validations", null, null);	    	    		    	
	            if (t.MethodConfiguration != null && t.MethodConfiguration.containsKey("SignatureValidations"))
	            {
	    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"signature validations detected", null, null);	    	    		    	
	            	List signvalidations = (List)t.MethodConfiguration.get("SignatureValidations");
		    		Map context = new HashMap();
		    		List errmsgs = new ArrayList();
		    		boolean vflag = false;
		    		for (int i=0; i < signvalidations.size(); i++)
		    		{
		    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"running signing validation plugin #"+i, null, null);	    	    		    	
		    			MrcsPlugin plugin = (MrcsPlugin)signvalidations.get(i);
		    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- instantiating - "+plugin.PluginClassName, null, null);
		    		    // we'll reuse IMrcsWorkflowValidation as the plugin interface, even though it lacks the task name, which I guess we could hardcode in the MRCS config for the task if need be...
		    		    ISignatureValidation vplug = (ISignatureValidation)Class.forName(plugin.PluginClassName).newInstance();
		    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- exec", null, null);
		    			if (!vplug.validate(getDfSession().getSessionManager(),getDfSession().getDocbaseName(),m_mrcsapp,m_processname,m_task,m_primarypackage,username,password,reason,errmsgs,plugin.PluginConfiguration,context))
		    			{
		    				// break immediately? Or run through all of them?
		    				vflag = false;
			    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- VALIDATION FAILURE", null, null);
		    				break;
		    			}
		    		}
		    		if (vflag) // validation failed if true...
		    		{
		    			for (int ii=0; ii < errmsgs.size(); ii++)
		    				setErrorMessage((String)errmsgs.get(ii));
		    			return false;
		    		}
	            }
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this, m+"Error in Signature Validations", null, e);
                throw new WrapperRuntimeException(e);
            }

            // now, I'm pretty sure that super.canCommitChanges() is a dangerous point-of-no-return call that actually has side effects, despite its stateless-implying name,
            // so we should make sure as we can that the signing info has been verified at this point, by checking audit records, rendition presence, number of signatures, etc
            if (super.canCommitChanges()) 
            {
            	// I'm pretty sure this super.onCommitChanges call is point-of-no-return: the task will be completed and the workflow will proceed with the next task
            	// if they are automatic. This can create race conditions with stuff that happens after the task is done, such as workflow plugins, etc. If you get an error
            	// message with "can't find object reference" of something like 4d017f38XXXXX, then the workflow completed and cleaned itself before the plugins/signing
            	// could complete in time.
            	
            	// THEREFORE, we will sign first (signing multiple documents can take...awhile...), THEN commit changes
                if (m_task.isSignOffRequired()) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"onCommitChanges is signing the document - I don't think this should ever happen for FinishWFT", null, null);
                    try { 
	                    if (t.MethodConfiguration != null && t.MethodConfiguration.containsKey("SignatureService"))
	                    {
	                    	MrcsPlugin signingplugin = (MrcsPlugin)t.MethodConfiguration.get("SignatureService");
	    	    		    ISignatureService signplug = (ISignatureService)Class.forName(signingplugin.PluginClassName).newInstance();
	    	    		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- exec signing", null, null);
	    	    			signplug.sign(getDfSession().getSessionManager(),getDfSession().getDocbaseName(),m_mrcsapp,m_processname,m_task,m_primarypackage,username,password,reason,signingplugin.PluginConfiguration,null);
	                    }
                    } catch (Exception e) {
                        /*-ERROR-*/DfLogger.error(this, m+"Error in Signature Service", null, e);
                        throw new WrapperRuntimeException(e);
                    }
                }
            	
                canCommit = super.onCommitChanges();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"canCommit (after validation and performing super call) : " + canCommit, null, null);
            } else {
                canCommit = false;
            }
            if (canCommit) {            	
            	                
            	// uses Actions as a pluginlayer...
                // IN GENERAL these are a bad idea, due to the race conditions documented in the comments above...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:FinishWFT: onCommitChanges : get ref to workflow defn" , null, null);
            	IDfId workflowid = m_task.getWorkflowId();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:FinishWFT: onCommitChanges : look up workflow object (IDfWorkflow)" , null, null);
                IDfWorkflow workflow = (IDfWorkflow)getDfSession().getObject(workflowid);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:FinishWFT: onCommitChanges : look up process object (IDfProcess)" , null, null);
            	IDfProcess wfdef = (IDfProcess)getDfSession().getObject(workflow.getProcessId());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:FinishWFT: onCommitChanges : workflow name: "+wfdef.getObjectName() , null, null);
            	MrcsWorkflowTask mrcstask = config.getMrcsWorkflowTask(m_mrcsapp,wfdef.getObjectName(),m_task.getTaskName());
            	if (mrcstask.Actions != null) 
            	{
            		Map context = new HashMap();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:FinishWFT: onCommitChanges : executing client-side plugins" , null, null);
                    for (int k=0; k < mrcstask.Actions.size(); k++)
                    {
                    	try { 
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:FinishWFT: onCommitChanges : executing plugin "+(k+1) , null, null);
                        	MrcsPlugin plugin = (MrcsPlugin)mrcstask.Actions.get(k);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:FinishWFT: onCommitChanges : -- class: "+plugin.PluginClassName , null, null);
                            IMrcsWorkflowActionPlugin wfaction = (IMrcsWorkflowActionPlugin)Class.forName(plugin.PluginClassName).newInstance();
                            wfaction.execute(getDfSession().getSessionManager(),getDfSession().getDocbaseName(), m_task, workflow, m_mrcsapp, plugin.PluginConfiguration, context);
                    	} catch (Exception e) {
                            /*-ERROR-*/DfLogger.error(this, m+"error in MRCS 4.2 client-side workflow actions", null, e);
                            setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] { e.getMessage() }, e);
                    		throw new WrapperRuntimeException ("error in MRCS workflow actions",e); 
                    	}
                    }
            	}
                if (canCommit)
                    setMessage("MSG_FINISH_SUCCESS", new Object[] { super.getString("MSG_OBJECT") });

            }//commit & esign ends
        } catch (DfException e1) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:FinishWFT.onCommitChanges - DfException occurred: ", null, e1);
            canCommit = false;
            try {
                if (m_task.getCompleteErrStatus() == 4) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Exception occurred:Incorrect Password "+ e1, null, null);
                    setErrorMessage("MSG_INCORRECT_PASSWORD");
                    return false;
                }
                /*
                 * else if(){ [DM_SYSOBJECT_E_ESIGN_SIGNATURE_METHOD_FAILED }
                 */
                else {
                    /*-ERROR-*/DfLogger.error(this, "MRCS:FinishWFT.onCommitChanges - Exception occurred:Unexpected Error ", null, e1);
                    setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] { e1.getMessage() }, e1);
                    return false;
                }
            } catch (DfException e2) {
                /*-ERROR-*/DfLogger.error(this, m+"error in exception handling code", null, e2);
                throw new WrapperRuntimeException(e2);
            }
        }

        return canCommit;
   }

    void setMessage(String s) {
        MessageService.addMessage(this, s, null);
    }


    void setMessage(com.documentum.web.form.Form form, java.lang.String s, java.lang.Object[] oParams) {
        MessageService.addMessage(this, s, oParams);
    }


    void setMessage(String s, Object aobj[]) {
        MessageService.addMessage(this, s, aobj);
    }


    void setErrorMessage(String s) {
        setReturnError(s, null, null);
        ErrorMessageService.getService().setNonFatalError(this, s, null);
    }


    void setErrorMessage(String s, Object aobj[]) {
        setReturnError(s, aobj, null);
        ErrorMessageService.getService().setNonFatalError(this, s, aobj, null);
    }


    void setErrorMessage(String s, Object aobj[], Exception exception) {
        setReturnError(s, aobj, exception);
        ErrorMessageService.getService().setNonFatalError(this, s, aobj, exception);
    }
    
    //CEM: Patch...
    protected boolean isDynamicPerformersAssigned()
    {
    	/*-CONFIG-*/String m = "ForwardWFT.isDynamicPerformersAssigned - ";
    	try { 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Invoked fixed isDynamicPerformersAssigned - overrides incorrect impl in ForwardWorkflowTask", null, null);
	        IWorkflowTask task = getWorkflowTask();
	        // CEM: fix - replace this incorrect code..
	        String page = this.getComponentPage();
	        if ("assignperformers".equals(this.getComponentPage()))
	        {
	        	return isDynamicPerformersAssignedHelper(task.getNextForwardActivities());
	        } else {
	        	//return isDynamicPerformersAssignedHelper(task.getNextForwardPortNames());
	        	return isDynamicPerformersAssignedHelper(task.getNextForwardActivities());
	        }
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }


}
