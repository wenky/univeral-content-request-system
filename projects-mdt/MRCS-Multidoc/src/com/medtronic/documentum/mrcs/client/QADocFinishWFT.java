package com.medtronic.documentum.mrcs.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.Text;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.FinishWorkflowTask;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsWorkflowTask;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.ISignatureService;
import com.medtronic.documentum.mrcs.plugin.ISignatureValidation;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowActionPlugin;

public class QADocFinishWFT extends FinishWorkflowTask 
{
	static public String USERNAME_CONTROL_NAME = "QADOC_SIGNOFF_USERNAME";
	static public String PASSWORD_CONTROL_NAME = "QADOC_SIGNOFF_PASSWORD";
	static public String REASONSELECT_CONTROL_NAME = "QADOC_SIGNOFF_REASON";
	
	IWorkflowTask m_task = null;
	String m_processname = null;
	String m_taskname = null;
	String m_mrcsapp = null;
	IDfDocument m_primarypackage = null;
	
    public QADocFinishWFT() { super(); }

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
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting signable document's mrcs application", null, null);
            m_primarypackage = ESignHelper.getSignableDocument(getDfSession(), m_task); 
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
    
    public boolean onCommitChanges() {
        /*-CONFIG-*/String m="onCommitChanges-";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get State Transition config", null, null);
        StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get MRCS config task definition", null, null);
        MrcsWorkflowTask t = config.getMrcsWorkflowTask(m_mrcsapp, m_processname, m_taskname);

        // actually, we should implement rollback as a method...it shouldn't happen here/client side at all unless an explicit Reject is selected, so I will remove it.
        
        boolean canCommit = false;
        try {
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
                canCommit = super.onCommitChanges();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"canCommit (after validation and performing super call) : " + canCommit, null, null);
            } else {
                canCommit = false;
            }

            if (canCommit) {            	
                	
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
                
            	// uses Actions as a pluginlayer...
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
    


    // repeat these methods, since we lack package access to the superclass's -- 
    void setErrorMessage(String id)
    {
        setReturnError(id, null, null);
        WebComponentErrorService.getService().setNonFatalError(this, id, null);
    }

    void setErrorMessage(String id, Object params[])
    {
        setReturnError(id, params, null);
        WebComponentErrorService.getService().setNonFatalError(this, id, params, null);
    }

    void setErrorMessage(String id, Exception e)
    {
        setReturnError(id, null, e);
        WebComponentErrorService.getService().setNonFatalError(this, id, e);
    }

    void setErrorMessage(String id, Object params[], Exception e)
    {
        setReturnError(id, params, e);
        WebComponentErrorService.getService().setNonFatalError(this, id, params, e);
    }

    void setMessage(String id)
    {
        MessageService.addMessage(this, id, null);
    }

    void setMessage(String id, Object params[])
    {
        MessageService.addMessage(this, id, params);
    }

    
}
