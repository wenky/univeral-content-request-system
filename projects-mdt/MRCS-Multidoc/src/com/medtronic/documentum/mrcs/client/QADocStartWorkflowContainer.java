/*
 * Created on May 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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

 Filename       $RCSfile: QADocStartWorkflowContainer.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2007/10/30 17:30:35 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Label;
import com.documentum.webcomponent.library.workflow.startwfcontainer.StartWorkflowContainer;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;
import com.medtronic.documentum.mrcs.plugin.IMrcsWorkflowValidation;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QADocStartWorkflowContainer extends StartWorkflowContainer {

    private IDfSessionManager _sMgr = null;
    private IDfSession _session = null;
    private String appName = null;
    private String docId = null;
    private String wfid = null;
    
    protected List validationMessages = null;

    /**
     *
     */
    public QADocStartWorkflowContainer() {
        super();
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);

        /*-CONFIG-*/String m = "MrcsStartWorkflowContainer.onInit";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsStartWorkflowContainer : onInit...", null,null);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsStartWorkflowContainer : onInit objectId " +argumentlist.get("objectId") , null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsStartWorkflowContainer : onInit component " +argumentlist.get("component") , null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsStartWorkflowContainer : onInit startworkflowId " +argumentlist.get("startworkflowId"), null,null );
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsStartWorkflowContainer : onInit type " +argumentlist.get("type") , null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsStartWorkflowContainer : onInit attachmentIds " +argumentlist.get("attachmentIds"), null,null );
        docId = argumentlist.get("attachmentIds");
        wfid = argumentlist.get("objectId");
        
        /*
         * MTW 03/20/2007 - PDCTM00000367
         * Added code to report error and return nicely from case where user selects multiple 
         * documents when attempting to start workflow attachment
         * Previous behavior was to return ugly stacktrace 
         * In conjunction iwth this change, also changed JSP page to remove ability to add attachments
         * May need to enable that in future if this class later enables attachment of multiple
         * documents
         */
        StringTokenizer docIdTokenizer = new StringTokenizer(docId, ",");

        if (docIdTokenizer.countTokens() > 1)
        {
        	ArrayList docCntErrMsgs = new ArrayList();
        	Map errors = new HashMap();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"it's empty, validation failed", null, null);       
            // Object[] params = {labelmap.get(attr)};
            // error.put("Params",params);
            // errmsgs.add(error);                            

        	errors.put("Error","ERR_MORE_THAN_ONE_DOCUMENT_SELECTED");/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation failure, redirecting to message screen",null,null);
        	docCntErrMsgs.add(errors);
        	
            setComponentPage("validationfailure");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing validation messages",null,null);
            Label validationerrors = (Label)getControl("validationerrors",Label.class);
            String errorstring = "";
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" entering validation for loop",null,null);
            for (int errs = 0; errs < docCntErrMsgs.size(); errs++)
            {
            	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" Get errdata Map from errs",null,null);
            	// get NLS id from error list
                Map errdata = (Map)docCntErrMsgs.get(errs);
                String nlserr = (String)errdata.get("Error");
            	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" Error String is: " + nlserr,null,null);
                Object[] errparams = (Object[])errdata.get("Params");
                // decode the NLS
                String errmsg = getString(nlserr,errparams);
                errorstring += errmsg+"<BR>";
            }
            validationerrors.setLabel(errorstring);
            return;
        }
        /*
         * End cahnge for PDCTM00000367
         */
        try {
            // workflow validation plugins
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" - retrieving doc to validate:  " + docId, null,null);
            IDfDocument doc = (IDfDocument)getDfSession().getObject(new DfId(docId));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" - getting MRCS attrs", null,null);
            String mrcsapp = doc.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" - mrcsapp: "+mrcsapp, null,null);
            String workflowid = argumentlist.get("objectId");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" - workflow: "+workflowid +", retrieving...", null,null);
            IDfSysObject wf = (IDfSysObject)getDfSession().getObject(new DfId(workflowid));
            String wfname = wf.getObjectName();

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" - getting doc config to look up validations", null,null);
            MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" - looking up validation plugins: ", null,null);
            List validations = docconfig.getDocumentWorkflowValidationPlugins(mrcsapp,wfname);

            if (validations != null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation plugins defined, executing",null,null);
                boolean validationpassed = true;
                try {
                    HashMap scratchpad = new HashMap();
                    ArrayList errmsgs = new ArrayList();
                    scratchpad.put("Errors",errmsgs);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing validation plugin list",null,null);
                    for (int i=0; i < validations.size(); i++)
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing plugin #"+i,null,null);
                        MrcsPlugin curplugin = (MrcsPlugin)validations.get(i);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting Class for plugin: "+curplugin.PluginClassName,null,null);
                        Class pluginclass = Class.forName(curplugin.PluginClassName);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"instantiating",null,null);
                        IMrcsWorkflowValidation plugin = (IMrcsWorkflowValidation)pluginclass.newInstance();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing current validation",null,null);                        
                        validationpassed = plugin.validate(getDfSession().getSessionManager(), docconfig.getApplicationDocbase(mrcsapp), doc, mrcsapp, wfname, errmsgs, curplugin.PluginConfiguration, scratchpad);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation result: "+validationpassed,null,null);
                        if (!validationpassed)
                        {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation failure, redirecting to message screen",null,null);
                            validationMessages = errmsgs;
                            setComponentPage("validationfailure");
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing validation messages",null,null);
                            Label validationerrors = (Label)getControl("validationerrors",Label.class);
                            String errorstring = "";
                            for (int errs = 0; errs < validationMessages.size(); errs++)
                            {
                                // get NLS id from error list
                                Map errdata = (Map)validationMessages.get(errs);
                                String nlserr = (String)errdata.get("Error");
                                Object[] errparams = (Object[])errdata.get("Params");
                                // decode the NLS
                                String errmsg = getString(nlserr,errparams);
                                errorstring += errmsg+"<BR>";
                            }
                            validationerrors.setLabel(errorstring);
                            return;
                        }
                    }
                } catch (Exception e) {
                    /*-ERROR-*/DfLogger.error(this,m+"Error in workflow initiation MRCS validations",null,e);
                    ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_WF_VALIDATION_ERROR", e);
                    setReturnError("MSG_DFC_ERROR", null, e);
                    return;
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validations execution complete ---<<<>>>---",null,null);
            }
            
        } catch (Exception dfe) {
            ErrorMessageService.getService().setNonFatalError(this, "Exception thrown during MRCS Workflow Validation", dfe);
            RuntimeException re = new RuntimeException("Exception thrown during MRCS Workflow Validation",dfe);
            setReturnError("Error encountered during MRCS Workflow Validation", null, re);
            throw re;
        }
    }
    
    public void cancelValidation(Control button, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsStartWorkflowContainer.cancelValidation - custom component data submit invoked",null,null);
        setComponentReturn();
    }

//    public boolean onCommitChanges()
//    {
//    	/*-CONFIG-*/String m="onCommitChanges - ";
//    	
//        boolean flag =super.onCommitChanges();
//        
//        // snipped: snapshot generation (now done as an initial task in the workflow...) 
//
//        return flag;
//    }



    private void releaseSysSession() {
        try {
            _sMgr.release(_session);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsStartWorkflowContainer: releaseSysSession : syssession released!! ", null, null);
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:MrcsStartWorkflowContainer: releaseSysSession : Error : " + e, null, null);
        }

    }



    //Get the Session
    private void setSysSession() throws DfException {
        //create Client object
        IDfClient client = new DfClient();
        //create a Session Manager object
        _sMgr = client.newSessionManager();

        StartWorkflowConfigFactory config = null;
        try {
            config = StartWorkflowConfigFactory.getWorkflowConfig();
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:MrcsStartWorkflowContainer.setSysSession: Error encountered while obtaining System user info "+e, null, null);
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsStartWorkflowContainer : setSysSession - appName : " + appName, null, null);
        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = new DfLoginInfo();
        loginInfoObj.setUser(config.getSystemUsername(appName));
        loginInfoObj.setPassword(config.getSystemPassword(appName));
        loginInfoObj.setDomain(null);

        String _docBase = config.getApplicationDocbase(appName);

        //bind the Session Manager to the login info
        _sMgr.setIdentity(_docBase, loginInfoObj);

        _session = _sMgr.getSession(_docBase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsStartWorkflowContainer.setSysSession: NEWSESSION created!!", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsStartWorkflowContainer.setSysSession: _sMgr : " + _sMgr, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsStartWorkflowContainer.setSysSession: session : " + _session, null, null);
    }

}
