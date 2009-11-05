package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.form.control.Text;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.FinishWorkflowTask;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSignatureService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSignatureValidation;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSigningReasons;

public class MdtFinishTask extends FinishWorkflowTask 
{
	static public String USERNAME_CONTROL_NAME = "MDT_SIGNOFF_USERNAME";
	static public String PASSWORD_CONTROL_NAME = "MDT_SIGNOFF_PASSWORD";
	static public String REASONSELECT_CONTROL_NAME = "MDT_SIGNOFF_REASON";
	static public String REASONENTRY_CONTROL_NAME = "MDT_SIGNOFF_ENTRY";
	
	boolean m_ismedtronicwf = false;
	
	IWorkflowTask m_task = null;
	String m_processname = null;
	String m_workflowname = null;
	String m_taskname = null;
	String m_mdtapp = null;
	String m_reasoncontrolname = null;
	Class m_reasoncontolclass = null;
	IDfDocument m_primarypackage = null;
	
    public void onInit(ArgumentList args) 
    {
        try {
            /*-dbg-*/Lg.dbg("calling super()");
            super.onInit(args);
                        
            m_task = getWorkflowTask();
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
                
                /*-dbg-*/Lg.dbg("Check if signoff controls are needed");
                if (m_task.isSignOffRequired()) {
                    //Obtain the resons for signing and initialize the Control
                    Map taskcfg = WorkflowUtils.getTaskConfig(getDfSession().getSessionManager(), getDfSession().getDocbaseName(), m_mdtapp, m_task.getTaskName(), m_primarypackage);
                	String signatureinterface = (String)taskcfg.get("SignatureInterface");
                    /*-dbg-*/Lg.dbg("signature interface: %s",signatureinterface);
                    Text rsntext = (Text) getControl(REASONENTRY_CONTROL_NAME,Text.class);
            		DropDownList rsnselect = (DropDownList)getControl(REASONSELECT_CONTROL_NAME,DropDownList.class);            		
                	if ("text".equals(signatureinterface))
                	{
                		// freeform signing reason entry
                        /*-dbg-*/Lg.dbg("enabling text, disabling select");
                        rsntext.setVisible(true);
                        rsntext.setEnabled(true);
                        rsnselect.setVisible(false);
                        rsnselect.setEnabled(false);
                        m_reasoncontrolname = REASONENTRY_CONTROL_NAME;
                        m_reasoncontolclass = Text.class;
                	}
                	if ("fixed".equals(signatureinterface))
                	{
                		// reason has been predetermined
                        /*-dbg-*/Lg.dbg("enabling text read-only, disabling select");
                        rsntext.setVisible(true);
                        rsntext.setEnabled(false);                    
                        rsnselect.setVisible(false);
                        rsnselect.setEnabled(false);
                        // get assigned role/reason
                        /*-dbg-*/Lg.dbg("lookup reasons list plugin");
                        MdtPlugin reasonplugindef = (MdtPlugin)taskcfg.get("SignatureReasonList");
                        /*-dbg-*/Lg.dbg("load reasons list plugin %s",reasonplugindef == null ? null : reasonplugindef.classname);
                        IMdtSigningReasons signingreasons = (IMdtSigningReasons)MdtPluginLoader.loadPlugin(reasonplugindef,getDfSession().getSessionManager());
                        /*-dbg-*/Lg.dbg("exec plugin");                    
                        List reasons = signingreasons.getReasons(m_processname,m_workflowname,m_task,taskcfg,reasonplugindef.context);  
                        // set reason
                        /*-dbg-*/Lg.dbg("getting first reason");                    
                        String firstreason = (String)reasons.get(0); 
                        /*-dbg-*/Lg.dbg("first reason: %s",firstreason);                    
                        rsntext.setValue(firstreason);
                        m_reasoncontrolname = REASONENTRY_CONTROL_NAME;
                        m_reasoncontolclass = Text.class;
                	}
                	if ("select".equals(signatureinterface))
                	{
                        /*-dbg-*/Lg.dbg("enabling Select, disabling Text");
                		// reason has been predetermined
                        rsntext.setVisible(false);
                        rsntext.setEnabled(false);                    
                		rsnselect.setVisible(true);
                		rsnselect.setEnabled(true);                    
                        // get assigned role/reason
                        /*-dbg-*/Lg.dbg("lookup reasons list plugin");
                        MdtPlugin reasonplugindef = (MdtPlugin)taskcfg.get("SignatureReasonList");
                        /*-dbg-*/Lg.dbg("load reasons list plugin %s",reasonplugindef == null ? null : reasonplugindef.classname);
                        IMdtSigningReasons signingreasons = (IMdtSigningReasons)MdtPluginLoader.loadPlugin(reasonplugindef,getDfSession().getSessionManager());
                        /*-dbg-*/Lg.dbg("exec plugin");                    
                        List reasons = signingreasons.getReasons(m_processname,m_workflowname,m_task,taskcfg,reasonplugindef.context);  
                        // set reason
                    	Iterator itList = reasons.iterator();
                        /*-dbg-*/Lg.dbg("iterating");                    
                		while(itList.hasNext()){
                			String val = (String)itList.next();
                            /*-dbg-*/Lg.dbg("adding option %s",val);                    
                			Option opt = new Option();
                			opt.setLabel(val);
                			opt.setValue(val);
                			rsnselect.addOption(opt);
                		}
                        m_reasoncontrolname = REASONSELECT_CONTROL_NAME;
                        m_reasoncontolclass = DropDownList.class;
                	}
                }
            }
        } catch (Exception ex1) {
            /*-ERROR-*/Lg.err("Exception in initialization of FinishWFT",ex1);
            throw EEx.create("FinishWFT-Init","Exception in initialization of FinishWFT component",ex1);
        }

    }    
    String performValidation() throws DfException 
    {
        /*-dbg-*/Lg.dbg("Check if task requires signoff");
        if (m_task.isSignOffRequired()) {
            /*-dbg-*/Lg.dbg("flipping on validation");
            setDoValidation(true);

            /*-dbg-*/Lg.dbg("get refs to signing controls");
            Text usernameCtrl = (Text)getControl(USERNAME_CONTROL_NAME,Text.class);
            Password passwordCtrl = (Password)getControl(PASSWORD_CONTROL_NAME,Password.class);
            StringInputControl rsnListCtrl = (StringInputControl)getControl(m_reasoncontrolname,m_reasoncontolclass);
            /*-dbg-*/Lg.dbg("get refs to signing controls");
            String username = usernameCtrl.getValue();
            /*-dbg-*/Lg.dbg(" -- user: "+username);
            String password = passwordCtrl.getValue();
            /*-dbg-*/Lg.dbg(" -- pass: "+(password == null ? "null" : "#########"));
            String reason = rsnListCtrl.getValue();
            /*-dbg-*/Lg.dbg(" -- reason: "+reason);

            /*-dbg-*/Lg.dbg("calling super.validate()");
            validate();
            if (!getIsValid()) {
                /*-dbg-*/Lg.dbg("superclass validation failed");
            	return "MSG_FORM_VALIDATION";
            }
            // if ((usrName != null) && (pswd != null) && (rsn)) {
            /*-dbg-*/Lg.dbg("check that entered username and password are valid and match the current logged in user");
            if ((username != null) && (username.trim().length() > 0)  && (password != null) && (reason != null) ) 
            {
                // CEM: weird bug - if password is correct, but username is wrong, then the task completes but fails when we sign -- after webtop has completed the task. 
                //      - so make sure the user matches the session user!
                String loggedinusername = getDfSession().getSessionManager().getIdentity(getDfSession().getDocbaseName()).getUser();
                /*-dbg-*/Lg.dbg("comparing entered login %s with current session user %s",username,loggedinusername);
                if (!loggedinusername.equals(username))
                {
                    /*-dbg-*/Lg.dbg("entered username does not match current user");
                    return "MSG_USERNAME_MISMATCH";
                }
                else {
                    /*-dbg-*/Lg.dbg("check password is valid for user");
	                // apparently we need to ensure the password is correct...
	                IDfLoginInfo logininfo = new DfLoginInfo();
	                logininfo.setUser(username);
	                logininfo.setPassword(password);
	                try {
	                    /*-dbg-*/Lg.dbg("attenmpting authentication");
	                	getDfSession().authenticate(logininfo);
	                } catch (DfAuthenticationException authex) {
	                	// return code via exception...great design, guys
                        /*-dbg-*/Lg.dbg("authentication failed, return MSG_BAD_PASSWORD");
	                	return "MSG_BAD_PASSWORD";
	                }
                }
                
            } else {
                /*-dbg-*/Lg.dbg("one of the user/password/reason widgets doesn't have a value");
                return "MSG_MISSING_USER_PASS_REASON";
            }
        }
        return null;
    }
    
    public boolean onCommitChanges() 
    {   
        if (m_ismedtronicwf)  {
            boolean canCommit = false;
            try {
                /*-dbg-*/Lg.dbg("get MDT config task definition");
                Map taskconfig = WorkflowUtils.getTaskConfig(getDfSession().getSessionManager(), getDfSession().getDocbaseName(), m_mdtapp, m_task.getTaskName(), m_primarypackage);
                
            	// perform basic CFR part 11 checks: must have valid user/pass/reason, user is user that is logged in, password is valid, reason is not empty/null
                /*-dbg-*/Lg.dbg("check form and user/pass/reason validations");
                String validation = performValidation();
                /*-dbg-*/Lg.dbg("result: "+(validation == null ? "valid" : validation));            
                if (validation != null)
                {
                    /*-dbg-*/Lg.dbg("User/Pass/Reason error detected: "+validation);
                	setErrorMessage(validation);
                	return false;
                }
                String username = null, password = null, reason = null;
                if (m_task.isSignOffRequired()) {
    
    	            /*-dbg-*/Lg.dbg("get refs to signing controls");
    	            Text usernameCtrl = (Text)getControl(USERNAME_CONTROL_NAME,Text.class);
    	            Password passwordCtrl = (Password)getControl(PASSWORD_CONTROL_NAME,Password.class);
    	            StringInputControl rsnListCtrl = (StringInputControl)getControl(m_reasoncontrolname,m_reasoncontolclass);
    	            /*-dbg-*/Lg.dbg("get refs to signing controls");
    	            username = usernameCtrl.getValue();
    	            /*-dbg-*/Lg.dbg(" -- user: "+username);
    	            password = passwordCtrl.getValue();
    	            /*-dbg-*/Lg.dbg(" -- pass: "+(password == null ? "null" : "#########"));
    	            reason = rsnListCtrl.getValue();
    	            /*-dbg-*/Lg.dbg(" -- reason: "+reason);
    	            
                    // set the superclass password so the superclass can perform its impl of signoff - otherwise super.onCommitChanges will fail 
                    Password passwdCtrl = (Password)getControl("__PASSWORD_CONTROL_NAME");            
                    passwdCtrl.setValue(password);    	            
    	            
    	            try { 
    				    /*-dbg-*/Lg.dbg("check for additional signature validations");	    	    		    	
    		            if (taskconfig != null && taskconfig.containsKey("SignatureValidations"))
    		            {
    		    		    /*-dbg-*/Lg.dbg("signature validations detected");	    	    		    	
    		            	List signvalidations = (List)taskconfig.get("SignatureValidations");
    			    		Map context = new HashMap();
    			    		List errmsgs = new ArrayList();
    			    		boolean vflag = false;
    			    		for (int i=0; i < signvalidations.size(); i++)
    			    		{
    			    		    /*-dbg-*/Lg.dbg("running signing validation plugin #"+i);	    	    		    	
    			    			MdtPlugin plugin = (MdtPlugin)signvalidations.get(i);
    			    		    /*-dbg-*/Lg.dbg("-- instantiating - %s",plugin.classname);
    			    		    IMdtSignatureValidation vplug = (IMdtSignatureValidation)MdtPluginLoader.loadPlugin(plugin, getDfSession().getSessionManager());
    			    		    /*-dbg-*/Lg.dbg("-- exec");
    			    			if (!vplug.validate(getDfSession().getSessionManager(),getDfSession().getDocbaseName(),m_mdtapp,m_processname,m_task,m_primarypackage,username,password,reason,errmsgs,plugin.context))
    			    			{
    			    				// break immediately? Or run through all of them?
    			    				vflag = false;
    				    		    /*-dbg-*/Lg.dbg("-- VALIDATION FAILURE");
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
    	                /*-ERROR-*/Lg.err("Error in Signature Validations",e);
    	                throw EEx.create("FinishWFT-SigningValidations", "Error in Signature Validations",e);	                
    	            }
                }
                
                // now, I'm pretty sure that super.canCommitChanges() is a dangerous point-of-no-return call that actually has side effects, despite its stateless-implying name,
                // so we should make sure as we can that the signing info has been verified at this point, by checking audit records, rendition presence, number of signatures, etc
                if (super.canCommitChanges()) 
                {
                    if (m_task.isSignOffRequired()) {
                        /*-dbg-*/Lg.dbg("onCommitChanges is signing the document");
                        try { 
    	                    if (taskconfig != null && taskconfig.containsKey("SignatureService"))
    	                    {
    	                    	MdtPlugin signingplugin = (MdtPlugin)taskconfig.get("SignatureService");
    	    	    		    IMdtSignatureService signplug = (IMdtSignatureService)Class.forName(signingplugin.classname).newInstance();
    	    	    		    /*-dbg-*/Lg.dbg("-- exec signing");
    	    	    			signplug.sign(getDfSession().getSessionManager(),getDfSession().getDocbaseName(),m_mdtapp,m_processname,m_task,m_primarypackage,username,password,reason,signingplugin.context);
    	                    }
                        } catch (Exception e) {
                            /*-ERROR-*/Lg.err("Error in Signature Service",e);
                            throw new WrapperRuntimeException(e);
                        }
                        
                    }
                	
                    canCommit = super.onCommitChanges();
                    /*-dbg-*/Lg.dbg("canCommit (after validation and performing super call) : " + canCommit);
                } else {
                    canCommit = false;
                }
    
                // TODO: check if Forward has the correct approach (I think it does based on comments...)
                
                if (canCommit) {            	
                    	                
                	// CEM: client-side action plugin layer deprecated. It used to be here in case we want to do it again...                
                    setMessage("MSG_FINISH_SUCCESS", new Object[] { super.getString("MSG_OBJECT") });
    
                }//commit & esign ends
            } catch (DfException e1) {
                /*-ERROR-*/Lg.err("DfException occurred", e1);
                canCommit = false;
                try {
                    if (m_task.getCompleteErrStatus() == 4) {
                        /*-dbg-*/Lg.dbg("Exception occurred:Incorrect Password ",e1);
                        setErrorMessage("MSG_INCORRECT_PASSWORD");
                        return false;
                    }
                    /*
                     * else if(){ [DM_SYSOBJECT_E_ESIGN_SIGNATURE_METHOD_FAILED }
                     */
                    else {
                        /*-ERROR-*/Lg.err("Exception occurred:Unexpected Error ", e1);
                        setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] { e1.getMessage() }, e1);
                        return false;
                    }
                } catch (DfException e2) {
                    /*-WARN-*/Lg.wrn("error in exception handling code",e2);
                }
            }

            return canCommit;
        } else {
            // Not a medtronic wf, just call super...
            return super.canCommitChanges();
        }
    }
    


    // repeat these methods, since we lack package access to the superclass's -- ^*@&#$^*&#+^*
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

