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
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: RejectWFT.java,v $
 Author         $Author: wallam1 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2007/02/27 20:28:03 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.form.control.databound.DatagridRow;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.RejectWorkflowTask;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.IACLPlugin;
import com.medtronic.documentum.mrcs.plugin.IAttachLabelPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsACLPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsAttachLabel;
import com.medtronic.documentum.mrcs.sbo.dto.ESignDTO;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author prabhu1
 *
 * Reject workflow task component with ESignature support.
 */
public class RejectWFT extends RejectWorkflowTask {

    private IWorkflowTask iworkflowtask = null;

    private ESignDTO signature = null;

    private IDfClient client = null;

    private boolean isValid = false;

    private IDfSessionManager _sMgr = null;

    private IDfId docId = null;

    private String appName = null;

    private IDfSession _session = null;
    private boolean rejectPathValidation = false;


    public RejectWFT() {
        super();
    }


    public void onInit(ArgumentList args) {
        try {
            super.onInit(args);
            setClientEvent("onOk", args);
            iworkflowtask = getWorkflowTask();
            IDfDocument docObject = ESignHelper.getSignableDocument(getDfSession(), iworkflowtask);
            docId = docObject.getObjectId();
            appName = docObject.getString("mrcs_application");
            setValid(true);

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
        setDoValidation(false);

    }


    private void setValid(boolean valid) {
        isValid = valid;
    }


    private boolean getValid() {
        return isValid;
    }


    private void releaseSysSession() {
        try {
            _sMgr.release(_session);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.releaseSysSession : sysSession released!!", null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:RejectWFT.releaseSysSession : Exception Occurred  ", null, e);
            throw new RuntimeException("RejectWFT session release error",e);
        }

    }





    private boolean checkReason() {

        boolean enteredReason = false;
        Text rsnCtrl = (Text) getControl(ESignHelper.DOCREJECT_RSNTXT_CONTROL_NAME, Text.class);

        if ((rsnCtrl.getValue() == null) || ((rsnCtrl.getValue().trim()).equals(""))) {
                rsnCtrl.setFocus();
                enteredReason = false;
        } else {
            enteredReason = true;
        }
        return enteredReason;
    }


    //Get the Session
    private void setSysSession() throws DfException {
        //create Client object
        IDfClient client = new DfClient();
        //create a Session Manager object
        _sMgr = client.newSessionManager();

        StateTransitionConfigFactory config = null;
        try {
            config = StateTransitionConfigFactory.getSTConfig();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:RejectWFT.setSysSession :Error encountered while getting System user info ", null, e);
            throw new RuntimeException("RejectWFT setSysSession config init error",e);
        }
        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = new DfLoginInfo();
        loginInfoObj.setUser(config.getSystemUsername(appName));
        loginInfoObj.setPassword(config.getSystemPassword(appName));
        loginInfoObj.setDomain(null);

        String _docBase = config.getApplicationDocbase(appName);

        //bind the Session Manager to the login info
        _sMgr.setIdentity(_docBase, loginInfoObj);

        _session = _sMgr.getSession(_docBase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.setSysSession : NEWSESSION created!!", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.setSysSession : _sMgr : " + _sMgr, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.setSysSession : session : " + _session, null, null);
        //return _session;
    }


    private boolean onRejectTasks() throws DfException {
        String reasonCtrl = "";

        IDfDocument docObject = (IDfDocument) _session.getObject(docId);
        StateTransitionConfigFactory config = null;
        //Suspend the Document.

        config = StateTransitionConfigFactory.getSTConfig();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onRejectTasks : config  " + config, null, null);
        String suspendfromstate = docObject.getCurrentStateName(); // used by legacy code...
        docObject.suspend("", false, false);

        //Capture the reason for signing
        Text rsnCtrl = (Text) getControl(ESignHelper.DOCREJECT_RSNTXT_CONTROL_NAME, Text.class);
        if (rsnCtrl != null)
            reasonCtrl = rsnCtrl.getValue();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onRejectTasks : Reject Reason : " + reasonCtrl, null, null);
        //Persist the reason for rejection
        IDfPersistentObject rjctRsnObj = _session.newObject("mrcs_rejection");
        /*
         * rejected_doc_chronicle_id ID 0 rejected_version String 64 rejection_comment String
         * 400 i_is_replica Boolean rejected_time Time i_vstamp Integer
         */
        rjctRsnObj.setId("rejected_doc_chronicle_id", docObject.getChronicleId());
        //rjctRsnObj.setString("rejected_version", docObject.getVersionPolicy().getVersionSummary(","));
        rjctRsnObj.setString("rejected_version", docObject.getVersionPolicy().getSameLabel());
        rjctRsnObj.setString("rejection_comment", reasonCtrl);
        rjctRsnObj.setTime("rejected_time", new DfTime());
        rjctRsnObj.setBoolean("is_rejected_doc_active", true);
        rjctRsnObj.setId("rejected_doc_object_id", docObject.getObjectId());
        rjctRsnObj.save();
        
        
        //MRCS 4.1.2 legacy compatibility - ACL and labelling
        if (config.isLegacyLCWF(appName))
        {
	        StateInfo stInfo = null;
	        //stInfo = config.getStateInfo(appName, docObject.getCurrentStateName()); // error since I'm doing this after doc.suspend, whereas I used to do this before doc.suspend
	        stInfo = config.getStateInfo(appName, suspendfromstate);

	        if ((stInfo != null) && (stInfo.getExceptionState() != null)) {
	            	            
	            /*****************************************/
	            
	            //Attach Label
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onRejectTasks : docObject: " + docObject.getObjectId().toString() , null, null);
	            IAttachLabelPlugin attchPlugin = new MrcsAttachLabel();
	            Map attchPluginParams = new HashMap();
	            attchPluginParams.put("ApplyToObject", docObject);
	            attchPluginParams.put("LabelToAttach", stInfo.getExceptionState());
	            attchPluginParams.put("LabelToIgnore", stInfo.getLabel());
	            attchPluginParams.put("MakeCurrent", new Boolean (stInfo.getCurrentLabel()));   
	            attchPlugin.attachlabel(_session.getSessionManager(), appName, attchPluginParams);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onRejectTasks : Document Symbolic label attached!! " , null, null);
	
	            //Set the ACL
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onRejectTasks : rejectedACL: " + stInfo.getRejectedDocACL() , null, null);
	            IACLPlugin aclPlugin = new MrcsACLPlugin();
	            Map aclPluginParams = new HashMap();
	            aclPluginParams.put("ApplyToObject", docObject);
	            aclPluginParams.put("ACLName", stInfo.getRejectedDocACL());
	            aclPlugin.applyDocACL(_session.getSessionManager(), appName, aclPluginParams);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onRejectTasks : Document ACL updated!! " , null, null);
	            
	            /*****************************************/            
	        } else {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onRejectTasks : No Exception State Configured", null, null);
	        }
        }
        return true;
    }


    public boolean onCommitChanges() {

        boolean canCommit = false;

        try {
            performValidation();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onCommitChanges : getValid :" + getValid(), null, null);
            if (super.canCommitChanges() && getValid()) {
                canCommit = super.onCommitChanges();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RejectWFT.onCommitChanges : canCommit :" + canCommit, null, null);
            }
            if (canCommit) {
                setSysSession();
                canCommit = onRejectTasks();
                setMessage("MSG_FINISH_SUCCESS", new Object[] { super.getString("MSG_OBJECT") });
            }//commit ends
        } catch (DfException e1) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:RejectWFT.onCommitChanges : Exception occurred, setting error: ", null, e1);
            canCommit = false;
            try {
                if (iworkflowtask.getCompleteErrStatus() == 4) {
                    /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:RejectWFT.onCommitChanges : Exception occurred:Incorrect Password " + e1, null, null);
                    setErrorMessage("MSG_INCORRECT_PASSWORD");
                    //canCommit = onRejectTasks();
                    return false;
                } else {
                    /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:RejectWFT.onCommitChanges : Exception occurred:Unexpected Error " + e1, null, null);
                    setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] { e1.getMessage() }, e1);
                    // rethrow exception - this just feels "wrong"
                    throw e1;
                }
            } catch (DfException e2) {
                /*-ERROR-*/DfLogger.error(this, "MRCS:RejectWFT.onCommitChanges : error processing user error message on exception: ", null, e2);
                throw new WrapperRuntimeException(e2);
            }
        } finally {
            if (canCommit)
                releaseSysSession();
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

}