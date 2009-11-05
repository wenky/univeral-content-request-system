/*
 * Created on Jan 26, 2005
 *
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.2
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: ForwardWFT.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.8 $
 Modified on    $Date: 2007/01/27 23:35:43 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
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
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.form.control.databound.DatagridRow;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.ForwardWorkflowTask;
import com.medtronic.documentum.mrcs.common.ESignPrecondition;
import com.medtronic.documentum.mrcs.common.MrcsPreConditions;
import com.medtronic.documentum.mrcs.config.ESignatureConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.ESignServiceSBO;
import com.medtronic.documentum.mrcs.sbo.dto.ESignDTO;


/**
 * @author prabhu1
 *
 * Forward workflow task component with ESignature support.
 *
 */
public class ForwardWFT extends ForwardWorkflowTask {

    private IWorkflowTask iworkflowtask = null; 
    private ESignDTO signature = null;
    private IDfClient client = null;
    private boolean isValid = false;
    private IDfDocument docObject = null;
    private String appName = "";

    private boolean forwardPathValidation = false;
    boolean forwardPathCheckedCount = false; // not used currently, unless we need separate validation message

    public ForwardWFT() {
        super();
    }

    public void onInit(ArgumentList args) {
        /*-CONFIG-*/String m="onInit-";
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling super()", null, null);
            super.onInit(args);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling setClientEvent for onOk", null, null);
            setClientEvent("onOk", args);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting workflowtask", null, null);
            iworkflowtask = getWorkflowTask();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"workflowtask: "+(iworkflowtask == null ? null : iworkflowtask.getActivityId().getId()), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting signable document", null, null);
            docObject = ESignHelper.getSignableDocument(getDfSession(), iworkflowtask);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"determining attached doc's MRCS app", null, null);
            appName = docObject.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS app: "+appName, null, null);
            if (iworkflowtask.isSignOffRequired()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"signoff is required for this task", null, null);
                //Obtain the resons for signing and initialize the Control
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"initializing reason list dropdown", null, null);
                DropDownList rsnListCtrl = (DropDownList) getControl(ESignHelper.REASONSELECT_CONTROL_NAME,
                    DropDownList.class);
                ESignHelper.initReasonList(appName, rsnListCtrl);
                //Initialize the Signature Object
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"initializing signature DTO object and adding listener", null, null);
                signature = new ESignDTO();
                addESignhandler();
            } else {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"signing not required by this task", null, null);
                setMessage("MSG_SIGN_NOTREQUIRED");
                setValid(true);
            }

        } catch (DfException excep) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:ForwardWFT.onInit - Exception", null, excep);
            throw new RuntimeException("DFC Error in ForwardWFT Component init",excep);
        } catch (Exception ex1) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:ForwardWFT.onInit - Other unhandled Exceptions", null, ex1);
            throw new RuntimeException("Unknown Error in ForwardWFT Component init",ex1);
        }

    }

    public void onRender() {
        super.onRender();
        setDoValidation(false);
    }


    private void addESignhandler() {

        /*-CONFIG-*/String m="addESignhandler-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting up eSignHandler", null, null);
        Text rsnCtrl = (Text) getControl(ESignHelper.REASONTXT_CONTROL_NAME, Text.class);
        Text usrCtrl = (Text) getControl(ESignHelper.USRTXT_CONTROL_NAME, Text.class);
        Password pswdCtrl = (Password) getControl(ESignHelper.PASSWORD_CONTROL_NAME, Password.class);

        // the listener stores the signing reason in eSignDTO whenever any of them are changed I think
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding event listener to the user, reason, and password controls", null, null);
        usrCtrl.setEventHandler("onchange", "esignHandler", rsnCtrl);
        rsnCtrl.setEventHandler("onchange", "esignHandler", rsnCtrl);
        pswdCtrl.setEventHandler("onchange", "esignHandler", rsnCtrl);

    }


    public void esignHandler(Control control) {

        /*-CONFIG-*/String m="esignHander-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"esignHandler called", null, null);
        Text txtCtrl = null;
        Password pswdCtrl = null;

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"determine if the changed widget the user or reason fields, or the password field", null, null);
        if (control.getTypeName().equals("Text")) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"text reason field", null, null);
            txtCtrl = (Text) control;
            if (txtCtrl.getId().equals(ESignHelper.USRTXT_CONTROL_NAME)) {
                String user = txtCtrl.getValue();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting signing user to "+user, null, null);
                signature.setUsr(user);
            }
            if (txtCtrl.getId().equals(ESignHelper.REASONTXT_CONTROL_NAME)) {
                String reason = txtCtrl.getValue();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting reason to "+reason, null, null);
                signature.setReason(reason);
            }
        } else if (control.getTypeName().equals("Password")) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"password control, storing password", null, null);
            pswdCtrl = (Password) control;
            if (pswdCtrl.getId().equals(ESignHelper.PASSWORD_CONTROL_NAME)) {
                signature.setPswd(pswdCtrl.getValue());
            }
        }

    }


    private void performValidation() throws DfException {
        /*-CONFIG-*/String m="performValidation-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check if signoff is req'd", null, null);
        if (iworkflowtask.isSignOffRequired()) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"performing signoff validation", null, null);
            setDoValidation(true);

            // grab task info so we can also validate the maximum number of forward paths selected...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting MRCS workflow task info for the current task", null, null);
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();

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
            forwardPathValidation = false;
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

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validating checked forward path count, # selected: "+checkedcount, null, null);
            //MJH 8-29-2006: updated workflow object to be IDfWorkflow from IDfSysobject, threw CCE in 5.3
            IDfProcess workflow = (IDfProcess)getDfSession().getObject(iworkflowtask.getProcessId());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"workflow system name: "+workflow.getObjectName(), null, null);
            if (!config.doesWorkflowTaskAllowMultiplePaths(appName,workflow.getObjectName(),iworkflowtask.getTaskName()))
            {
                if (checkedcount > 1)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"more than one path selected when only one is allowed", null, null);
                    forwardPathValidation = false;
                }
            }

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling super.validate()", null, null);
            validate();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling checkReason", null, null);
            // this call may overwrite the reason, it doesn't just validate the reason
            boolean rsn = checkReason();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting user, pwd from eSignDTO", null, null);
            // why aren't we just getting the signature info directly from the widgets? -CEM
            String usrName = signature.getUsr();
            String pswd = signature.getPswd();
            if ((usrName != null) && (usrName.trim().length() > 0)  && (pswd != null) && (rsn) && forwardPathValidation) {
                setValid(true);
                // CEM: weird bug - if password is correct, but username is wrong, then the task completes but fails when we sign -- after webtop has completed the task. 
                //      - so make sure the user matches the session user!
                String loggedinusername = getDfSession().getSessionManager().getIdentity(getDfSession().getDocbaseName()).getUser();
                if (!loggedinusername.equals(usrName))
                {
                    setValid(false);
                }
                else 
                {
	                // apparently we need to ensure the password is correct...
	                IDfLoginInfo logininfo = new DfLoginInfo();
	                logininfo.setUser(usrName);
	                logininfo.setPassword(pswd);
	                try {
	                	getDfSession().getClient().authenticate(getDfSession().getDocbaseName(),logininfo);
	                } catch (DfException authex) {
	                	setValid(false);
	                }
                }
                
            } else {
                setValid(false);
            }
        }
    }


    private boolean checkReason() {
        /*-CONFIG-*/String m="checkReason()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null, null);

        boolean enteredReason = false;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting signing reason controls from WDK form", null, null);
        Text rsnCtrl = (Text) getControl(ESignHelper.REASONTXT_CONTROL_NAME, Text.class);
        DropDownList rsnList = (DropDownList) getControl(ESignHelper.REASONSELECT_CONTROL_NAME, DropDownList.class);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking if text entry (manually specified) signing reason is not empty", null, null);
        if ((rsnCtrl.getValue() == null) || ((rsnCtrl.getValue().trim()).equals(""))) {
            // CEM: this "if" statement is technically a no-no: doing GUI focusing in a validation function.
            // - this has caused strange bugs in the signing process that allows signing without a reason.
            //   QuickDirty solution is to set the signature value here as well...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"text field for manual signing reason is not specified, checking dropdown list", null, null);
            if (rsnList.getValue().equals(ESignHelper.DEFAULT_REASON)) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"dropdown list has not been set either (still on default value), so reason has not been entered", null, null);
                rsnCtrl.setFocus();
                enteredReason = false;
            } else {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"set reason text field to selected reason in the text dropdown", null, null);
                String reason = rsnList.getValue();
                rsnCtrl.setValue(reason);
                // quick and dirty solution to various signing w/o signing reason bugs...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"set the signature value to be paranoid, since this has caused sign w/o reason loopholes/bugs to reason: "+reason, null, null);
                if (signature != null) signature.setReason(reason);
                enteredReason = true;
            }
        } else {
            enteredReason = true;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checkReason is returning: "+enteredReason, null, null);
        return enteredReason;
    }


    private void setValid(boolean valid) {
        isValid = valid;
    }


    private boolean getValid() {
        return isValid;
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
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"set forwardPathValidation to true", null, null);    	
	            	this.forwardPathValidation = true;
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
    	} else {
        	// if not, check how many paths there would be
    		// not necessary, there is cleaner code in the validation logic for this class, I'll comment out for now since this may be useful in the future
			/*
    		try {
	    		IWorkflowTask task = this.getWorkflowTask();
	            IDfList forwardAct = task.getNextForwardActivities();
	            int count = forwardAct.getCount();
	            if (1 == count)
	            {
	            	// fake it baby
		            IDfTypedObject act = (IDfTypedObject)forwardAct.get(0);
		            String actId = act.getString("r_object_id");
	            	String cboxname = this.NEXT_TASKS_CHECKBOX_CONTROL_NAME + actId;
	            	Checkbox cbox = (Checkbox)getControl(cboxname);
	            	if (null != cbox) {
	            		cbox.setValue(true);
	            	}
	            }
    		} catch (DfException dfe) {
    			throw new RuntimeException("Df Error in single path autoselect in onRender",dfe);
    		}
    		*/
    	}
    	
    }

    public boolean onCommitChanges() {
        boolean canCommit = false;
        try {
            AutoSelectSinglePath();
        	performValidation();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ForwardWFT.onCommitChanges: getValid :" + getValid(), null, null);
            MrcsPlugin preCheckClass = null;
            MrcsPreConditions preCheck = null;
            Integer noSigns = null;
            try {
                //Need to eliminate the usage of this try catch block by
                //better exception handling mechanizsm at Config broker
                MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ForwardWFT.onCommitChanges: onCommitChanges config > " + config, null, null);
                preCheckClass = config.getPreconditionPlugin(appName, "ESignature");
                preCheck = (MrcsPreConditions) Class.forName(preCheckClass.PluginClassName).newInstance();
                ESignatureConfigFactory eSignConf = ESignatureConfigFactory.getESignConfig();
                noSigns = eSignConf.getNoOfSigsAllowed(appName);
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this, "MRCS:ForwardWFT.onCommitChanges: Exception occurred while trying obtain PreCheck configuartions : ", null, e);
                throw new RuntimeException("Error in Preconditions or ESign config load for ForwardWFT.onCommitChanges",e);
            }
            HashMap map = new HashMap();
            map.put("IDfSession", getDfSession());
            map.put("IDfDocument", docObject);
            map.put("ITask", iworkflowtask);
            map.put("ESignDTO", signature);
            map.put("NoOfSigns", noSigns);

            if (super.canCommitChanges() && preCheck.isTaskEffectual(map, preCheckClass.PluginConfiguration) && getValid()) {
                //if(getValid()){
                //canCommit = super.onCommitChanges();//CEM: Can't call super.onCOmmit here, since that will close the task even if the esign fails and we cancel.
                canCommit = true;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ForwardWFT.onCommitChanges canCommit :" + canCommit, null, null);
                /*}else{
                    setErrorMessage("MSG_REQ");
                }*/
            } else {
                canCommit = false;
                // hack this important error message assignment
                if (preCheck instanceof ESignPrecondition)
                {
                    ESignPrecondition esigncheck = (ESignPrecondition)preCheck;
                    switch  (esigncheck.rejectReason) {
                      case 1:
                          setErrorMessage("MSG_RENDITION_NOT_FOUND");
                          break;
                      case 2:
                          setErrorMessage("MSG_SIGNATURE_OVERLOAD_OR_DUPLICATE_REASON");
                          break;
                      case 3:
                          setErrorMessage("MSG_NOT_QUALIFIED");
                          break;
                      default:
                          if (!forwardPathValidation)
                              setErrorMessage("MSG_FORWARD_PATH_NOT_SELECTED");
                          else
                              setErrorMessage("MSG_CANNOT_ESIGN");
                    }
                }
                else
                    setErrorMessage("MSG_CANNOT_ESIGN");
                //setComponentReturn();
            }
            if (canCommit) {
                if (iworkflowtask.isSignOffRequired()) {
                    //IDfClientX clientx = new DfClientX();
                    //client = clientx.getLocalClient();
                    //IDfSessionManager sMgr = client.newSessionManager();
                    //IESignServiceSBO _esignSBO = (IESignServiceSBO) client.new-Service(IESignServiceSBO.class.getName(),sMgr);
                    ESignServiceSBO _esignSBO = new ESignServiceSBO();
                    canCommit = _esignSBO.signDocument(docObject, signature);
                }// esign ends
            // CEM: NOW we can call super.onCommitChanges, since we have a successful eSign.
            if (canCommit)
                canCommit = super.onCommitChanges();
            if (canCommit)
                setMessage("MSG_FINISH_SUCCESS", new Object[] { super.getString("MSG_OBJECT") });
            }//commit
        } catch (DfException e1) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:ForwardWFT.onCommitChanges - DfException occurred: ", null, e1);

            canCommit = false;
            try {
                if (iworkflowtask.getCompleteErrStatus() == 4) {
                    /*-ERROR-*/DfLogger.error(this, "MRCS:ForwardWFT.onCommitChanges - Exception occurred:Incorrect Password "+ e1, null, null);
                    setErrorMessage("MSG_INCORRECT_PASSWORD");
                    return false;
                }/*
                  * else if (e1.getErrorCode() == 256){ setMessage("MSG_FAIL_ESIGN", new Object[]
                  * {super.getString("MSG_OBJECT") }); }
                  */else {
                      /*-ERROR-*/DfLogger.error(this, "MRCS:ForwardWFT.onCommitChanges - Exception occurred:Unexpected Error "+ e1, null, null);
                    setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] { e1.getMessage() }, e1);
                    return false;
                }
            } catch (DfException e2) {
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