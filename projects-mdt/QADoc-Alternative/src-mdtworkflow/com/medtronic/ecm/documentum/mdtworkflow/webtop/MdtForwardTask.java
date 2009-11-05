package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.form.control.databound.DatagridRow;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.ForwardWorkflowTask;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSignatureService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSignatureValidation;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSigningReasons;

public class MdtForwardTask extends ForwardWorkflowTask
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
            /*-dbg-*/Lg.dbg("getting signable document");
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
            /*-ERROR-*/Lg.err("Exception in initialization of ForwardWFT",ex1);
            throw EEx.create("ForwardWFT-Init","Exception in initialization of ForwardWFT component",ex1);
        }

    }
    
    String performValidation() throws DfException 
    {
        /*-CONFIG-*/String m="performValidation-";
        /*-dbg-*/Lg.dbg("Check if task requires signoff");
        if (m_task.isSignOffRequired()) {
            /*-dbg-*/Lg.dbg("flipping on validation");
            setDoValidation(true);
            
            // forward does this, finish does not...
            String forwardpathvalidation = performForwardPathValidation();
            /*-dbg-*/Lg.dbg("forward path validation result: %s",forwardpathvalidation);
            if (forwardpathvalidation != null){
                /*-dbg-*/Lg.dbg("returning forward path validation violation");
            	return forwardpathvalidation;
            }

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
                    /*-dbg-*/Lg.dbg("entered username does not match current user");
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

    String performForwardPathValidation() throws DfException
    {
        // need to check forwarding paths' checkboxes for: at least one checked
        /*-dbg-*/Lg.dbg("checking forwarding checkbox datagrid for at-least-one-checked");
        Datagrid forwardgrid = (Datagrid)getControl(ForwardWorkflowTask.NEXT_TASKS_GRID_CONTROL_NAME);
        Iterator i = forwardgrid.getContainedControls();
        // this seems to be infinitely looped, <sarcasm>Nice Iterator guys</sarcasm> <shrug>then again, I've seen it not infinitely looped...</shrug>
        HashSet resultset = new HashSet(); // for tracking what we've seen and haven't seen yet so we can tell when the infinitely looping iterator has looped back around
        /*-dbg-*/Lg.dbg("iterating through controls, adding them to our set of checkboxes if they are of type checkbox");
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
                        /*-dbg-*/Lg.dbg("found cbox named %s",cboxname);
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
                    /*-dbg-*/Lg.dbg("adding cbox named to cbox search results %s",cboxname);
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
            /*-dbg-*/Lg.dbg("examining checkbox: %s",name);
            Checkbox cbox = (Checkbox)getControl(name);
            if (0 == boxcount)
            	firstcbox = cbox;
            boxcount++;                
            if (cbox.getValue())
            {
                /*-dbg-*/if(Lg.dbg())Lg.dbg("checkbox is checked: %s",""+cbox.getValue());
                forwardPathValidation = true;
                /*-dbg-*/Lg.dbg("iterating checkedcount");
                checkedcount++;
                /*-dbg-*/Lg.dbg("checkedcount is %d",checkedcount);
            }
        }
        /*-dbg-*/Lg.dbg("check for autoselect of single forward path");
        if (1 == boxcount)
        {
            /*-dbg-*/Lg.dbg("autochecking the single forward path");
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

        /*-dbg-*/Lg.dbg("get task config");
        Map taskconfig = WorkflowUtils.getTaskConfig(getDfSession().getSessionManager(), getDfSession().getDocbaseName(), m_mdtapp, m_task.getTaskName(), m_primarypackage);

        /*-dbg-*/Lg.dbg("validating checked forward path count, # selected: "+checkedcount);
        //MJH 8-29-2006: updated workflow object to be IDfWorkflow from IDfSysobject, threw CCE in 5.3
        IDfProcess workflow = (IDfProcess)getDfSession().getObject(m_task.getProcessId());
        /*-dbg-*/Lg.dbg("workflow system name: "+workflow.getObjectName());
        if (!taskconfig.containsKey("AllowMultiplePaths"))
        {
            if (checkedcount > 1)
            {
                /*-dbg-*/Lg.dbg("more than one path selected when only one is allowed");
                return "MSG_MULTIPLE_PATHS_SELECTED_WHEN_ONLY_ONE_ALLOWED";
            }
        }
        return null;    	
    }
 	
    public void AutoSelectSinglePath()
    {
        /*-dbg-*/Lg.dbg("check for premature Finish Click and single path selection");    	
    	// see if the forward paths grid was ever instantiated
    	Datagrid forwardgrid = (Datagrid)getControl(ForwardWorkflowTask.NEXT_TASKS_GRID_CONTROL_NAME);
    	// if not, check how many paths there would be
    	if (null == forwardgrid)
    	{
            /*-dbg-*/Lg.dbg("forwardgrid is null, so Finish clicked before selecting a path, see if we can autoselect");    	
    		try { 
                /*-dbg-*/Lg.dbg("get wf task");    	
	    		IWorkflowTask task = this.getWorkflowTask();
                /*-dbg-*/if (Lg.dbg())Lg.dbg("get forward activities for task "+task.getActivityName());    	
	            IDfList forwardAct = task.getNextForwardActivities();
                /*-dbg-*/Lg.dbg("Get count of next activities");    	
	            int count = forwardAct.getCount();
	            if (1 == count)
	            {
	                /*-dbg-*/Lg.dbg("only one next activity, so autoselect that one");    	
	            	// fake it baby
		            IDfTypedObject act = (IDfTypedObject)forwardAct.get(0);
		            String actId = act.getString("r_object_id");
	                /*-dbg-*/Lg.dbg("id of next activity: %s",actId);    	
	            	String cboxname = "__NEXT_TASKS_CHECKBOX_CONTROL_NAME" + actId;
	                /*-dbg-*/Lg.dbg("creating fake checkbox for that activity: %s",cboxname);    	
	            	Checkbox cbox = (Checkbox)getControl(cboxname,Checkbox.class);
	                /*-dbg-*/Lg.dbg("selecting the cbox");    	
	            	cbox.setValue(true);
	            	//dangerous levels of hackitude here...
	            	// because the superclass has the "list of next activities" property private and immutable from this class, I have to resort to this trickery
	            	//   page == forward forces the superclass to check for selected forward paths. if it's not forward, it just assumes the list is ready (which its not, and I
	            	//   can't make it ready since it's frikking PRIVATE. thanks, DCTM
	                /*-dbg-*/Lg.dbg("set component page to forward so that the super.onCommitChanges call works correcly");    	
	            	this.setComponentPage("forward");
	            }
    		} catch (DfException dfe) {
    			throw new RuntimeException("Df Error in single path autoselect",dfe);
    		}
    	}
    	
    }    
    
    public boolean onCommitChanges() 
    {        
        if (m_ismedtronicwf) {
        
            boolean canCommit = false;
            try {
            	/*-dbg-*/Lg.dbg("autoselect single path");
                AutoSelectSinglePath();
    
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
                
                // TODO: check if Forward has the correct approach (I think it does based on comments...)
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
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("canCommit (after validation and performing super call) : %s",""+canCommit);
                } else {
                    canCommit = false;
                }
    
                
                if (canCommit) {            	
                	// CEM: client-side action plugin layer deprecated. It used to be here in case we want to do it again...
                    // IN GENERAL these are a bad idea, due to the race conditions documented in the comments above...
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("commit has completed");
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
                    /*-WARN-*/Lg.wrn("error in exception handling",e2);
                }
            }
    
            return canCommit;
        } else {
            // non-medtronic WF task
            return super.canCommitChanges();
        }
    }

    //CEM: Patch... --> validate for D6...I think this was updated since the current call is "deprecated"
    protected boolean isDynamicPerformersAssigned53()
    {
    	try { 
            /*-dbg-*/Lg.dbg("Invoked fixed isDynamicPerformersAssigned - overrides incorrect impl in ForwardWorkflowTask");
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
}
