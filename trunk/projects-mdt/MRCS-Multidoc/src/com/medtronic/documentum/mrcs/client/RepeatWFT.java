/*
 * Created on Jan 24, 2005
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

 Filename       $RCSfile: RepeatWFT.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:22 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.Text;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.RepeatWorkflowTask;
import com.medtronic.documentum.mrcs.common.MrcsPreConditions;
import com.medtronic.documentum.mrcs.config.ESignatureConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;
import com.medtronic.documentum.mrcs.sbo.ESignServiceSBO;
import com.medtronic.documentum.mrcs.sbo.dto.ESignDTO;

/**
 * @author prabhu1
 *
 * Repeat workflow task component with ESignature support.
 */

public class RepeatWFT extends RepeatWorkflowTask
{
	private String m_strFolderId = null;
	private IDfLoginInfo loginInfo = null;
	private IWorkflowTask iworkflowtask = null;
	private ESignDTO signature = null;
	private IDfClient client = null;
	private boolean isValid = false;
	private IDfDocument docObject = null;
	private String appName = "";

public RepeatWFT(){
	super();
}

public void onInit(ArgumentList args)
{

	try{

		super.onInit(args);
		setClientEvent("onOk", args);
		iworkflowtask = getWorkflowTask();
    		docObject = ESignHelper.getSignableDocument(getDfSession(), iworkflowtask );
    		appName = docObject.getString("mrcs_application");

			if(iworkflowtask.isSignOffRequired()){
				//Obtain the resons for signing and initialize the Control
				DropDownList rsnListCtrl = (DropDownList) getControl(ESignHelper.REASONSELECT_CONTROL_NAME, DropDownList.class);
				ESignHelper.initReasonList(appName, rsnListCtrl);
				//Initialize the Signature Object
				signature = new ESignDTO();
				addESignhandler();
				}else{
					setMessage("MSG_SIGN_NOTREQUIRED");
					setValid(true);
				}

	}
	catch(DfException excep){
	    /*-ERROR-*/DfLogger.error(this, "MRCS:RepeatWFT.onInit - Exception : ", null, excep);
        throw new RuntimeException("DFC Error in RepeatWFT component init",excep);
		}
	catch(Exception ex1){
	    /*-ERROR-*/DfLogger.error(this, "MRCS:RepeatWFT.onInit - Other unhandled Exceptions : ", null, ex1);
        throw new RuntimeException("Unknown Error in RepeatWFT component init",ex1);
		}

}

public void onRender()
{
	super.onRender();
	setDoValidation(false);

}

private void addESignhandler(){

	Text rsnCtrl = (Text) getControl(ESignHelper.REASONTXT_CONTROL_NAME, Text.class);
	Text usrCtrl = (Text) getControl(ESignHelper.USRTXT_CONTROL_NAME, Text.class);
	Password pswdCtrl = (Password) getControl(ESignHelper.PASSWORD_CONTROL_NAME, Password.class);

	usrCtrl.setEventHandler("onchange","esignHandler",rsnCtrl);
	rsnCtrl.setEventHandler("onchange","esignHandler",rsnCtrl);
	pswdCtrl.setEventHandler("onchange","esignHandler",rsnCtrl);

}

public void esignHandler(Control control){

	Text txtCtrl = null;
	Password pswdCtrl = null;

	if(control.getTypeName().equals("Text")){
		txtCtrl = (Text)control;
		if(txtCtrl.getId().equals(ESignHelper.USRTXT_CONTROL_NAME)){
			signature.setUsr(txtCtrl.getValue());
		}
		if(txtCtrl.getId().equals(ESignHelper.REASONTXT_CONTROL_NAME)){
			signature.setReason(txtCtrl.getValue());
			}
		}
	else if(control.getTypeName().equals("Password")){
		pswdCtrl = (Password)control;
		if(pswdCtrl.getId().equals(ESignHelper.PASSWORD_CONTROL_NAME)){
			signature.setPswd(pswdCtrl.getValue());
		}
	}

}

private void performValidation() throws DfException{
	if(iworkflowtask.isSignOffRequired()){
		setDoValidation(true);

		String usrName = signature.getUsr();
		String pswd = signature.getPswd();
		String reasonTxt = signature.getReason();

		validate();
		boolean rsn = checkReason();

            if ((usrName != null) && (usrName.trim().length() > 0)  && (pswd != null) && (rsn) ) {
			setValid(true);
		}else{
			setValid(false);
		}
	}
}


private boolean checkReason(){

		boolean enteredReason = false;
		Text rsnCtrl = (Text) getControl(ESignHelper.REASONTXT_CONTROL_NAME, Text.class);
		DropDownList rsnList = (DropDownList) getControl(ESignHelper.REASONSELECT_CONTROL_NAME, DropDownList.class);

		if((rsnCtrl.getValue() == null)||((rsnCtrl.getValue().trim()).equals(""))){
			if(rsnList.getValue().equals(ESignHelper.DEFAULT_REASON)){
				rsnCtrl.setFocus();
				enteredReason = false;
			}else{
				rsnCtrl.setValue(rsnList.getValue());
				enteredReason= true;
			}
		}else {
			enteredReason = true;
			}
	 return enteredReason;
}

private void setValid(boolean valid){
	isValid = valid;
}

private boolean getValid(){
	return isValid;
}

public boolean onCommitChanges()
{
	boolean canCommit = false;
		try{
            performValidation();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RepeatWFT.onCommitChanges - getValid :" + getValid(), null, null);
	        MrcsPlugin preCheckClass = null;
	        MrcsPreConditions preCheck = null;
	        Integer noSigns = null;
	        try {
	            //Need to eliminate the usage of this try catch block by
	            //better exception handling mechanizsm at Config broker
	            MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RepeatWFT.onCommitChanges config > " + config, null, null);
	            preCheckClass = config.getPreconditionPlugin(appName, "ESignature");
	            preCheck = (MrcsPreConditions)Class.forName(preCheckClass.PluginClassName).newInstance();
	            ESignatureConfigFactory eSignConf = ESignatureConfigFactory.getESignConfig();
	            noSigns = eSignConf.getNoOfSigsAllowed(appName);
	        } catch (Exception e) {
	            /*-ERROR-*/DfLogger.error(this, "MRCS:RepeatWFT..onCommitChanges : Exception occurred while obtaining PreCheck configuartions ", null, e);
                throw new RuntimeException("Error in loading Preconditions or ESign configuration",e);
	        }
	        HashMap map = new HashMap();
	        map.put("IDfSession",getDfSession());
	        map.put("IDfDocument",docObject);
	        map.put("ITask",iworkflowtask);
	        map.put("ESignDTO",signature);
	        map.put("NoOfSigns",noSigns);

            if (super.canCommitChanges() && preCheck.isTaskEffectual(map, preCheckClass.PluginConfiguration) && getValid()) {
                canCommit = super.onCommitChanges();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:RepeatWFT.onCommitChanges : canCommit : " + canCommit, null, null);
		    }else{
		        canCommit = false;
    			setErrorMessage("MSG_CANNOT_ESIGN");
    			//setComponentReturn();
		    }

	if(canCommit){
		    if(iworkflowtask.isSignOffRequired()){
		    	IDfClientX clientx = new DfClientX();
		    	client = clientx.getLocalClient();
		    	IDfSessionManager sMgr = client.newSessionManager();
		    	//IESignServiceSBO _esignSBO = (IESignServiceSBO)client.new-Service(IESignServiceSBO.class.getName(),sMgr);
		    	ESignServiceSBO _esignSBO = new ESignServiceSBO();
		    	canCommit = _esignSBO.signDocument(docObject, signature);
				if(canCommit)setMessage("MSG_FINISH_SUCCESS", new Object[] {super.getString("MSG_OBJECT") });
					}//esign ends
		}//commit ends
    }catch(DfException e1){
        /*-ERROR-*/DfLogger.error(this, "MRCS:RepeatWFT.onCommitChanges : Exception occurred, categorizing... ", null, null);
    	canCommit = false;
        try
        {
            if(iworkflowtask.getCompleteErrStatus() == 4)
            {
                /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:RepeatWFT.onCommitChanges : Exception occurred:Incorrect Password "+ e1, null, null);
    			setErrorMessage("MSG_INCORRECT_PASSWORD");
                return false;
            }/* else if (e1.getErrorCode() == 256){
            	setMessage("MSG_FAIL_ESIGN", new Object[] {super.getString("MSG_OBJECT") });
            }*/ else
            {
                /*-ERROR-*/DfLogger.error(this, "MRCS:RepeatWFT.onCommitChanges : Exception occurred:Unexpected Error ", null, e1);
    			setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] { e1.getMessage()}, e1);
                return false;
            }
        }
        catch(DfException e2)
        {
            /*-ERROR-*/DfLogger.error(this, "MRCS:RepeatWFT.onCommitChanges : exception in RepeatWFT exception handling ", null, e1);
            throw new WrapperRuntimeException(e2);
        }
    }

    return canCommit;
}

void setMessage(String s)
{
    MessageService.addMessage(this, s, null);
}

 void setMessage(com.documentum.web.form.Form form,
        java.lang.String s,
        java.lang.Object[] oParams)
{
    MessageService.addMessage(this, s, oParams);
}
 void setMessage(String s, Object aobj[])
 {
     MessageService.addMessage(this, s, aobj);
 }

 void setErrorMessage(String s)
{
    setReturnError(s, null, null);
    ErrorMessageService.getService().setNonFatalError(this, s, null);
}
 void setErrorMessage(String s, Object aobj[])
 {
     setReturnError(s, aobj, null);
     ErrorMessageService.getService().setNonFatalError(this, s, aobj, null);
 }

 void setErrorMessage(String s, Object aobj[], Exception exception)
{
    setReturnError(s, aobj, exception);
    ErrorMessageService.getService().setNonFatalError(this, s, aobj, exception);
}



}