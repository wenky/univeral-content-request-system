/*
 * Created on Nov 15, 2005
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

 Filename       $RCSfile: QADocTaskMgrContainer.java,v $
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

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.services.workflow.inbox.ITask;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DateTime;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Panel;
import com.documentum.webcomponent.library.workflow.taskmgrcontainer.TaskMgrContainer;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsWorkflowTask;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.IMrcsWorkflowValidation;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QADocTaskMgrContainer extends TaskMgrContainer
{
    public void onInit(ArgumentList argumentlist)
    {
        String page = this.getComponentPage();
        boolean validationfailure = false;
        boolean analysismessage = false;
        // identify if this is an MRCS task...
        /*-CONFIG-*/String m="onInit - ";
        ArrayList errmsgs = new ArrayList();
        ArrayList analysismsgs = new ArrayList();

//QADoc effective date hack
boolean effectivedate = false;
//QADoc hack code
Label datelabel = (Label)getControl("EffectiveDateLabel",Label.class);
datelabel.setVisible(false);
DateTime datectrl = (DateTime)getControl("EffectiveDate",DateTime.class);
datectrl.setVisible(false);
        
        // MRCS is disabling REPEAT for now...
        getControl(TaskMgrContainer.REPEAT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);

        try {
            String taskId = argumentlist.get("objectId");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking taskid "+taskId,null,null);
            ITask curtask = getInboxService().getTask(new DfId(taskId), true);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking if it is a workflow task",null,null);
            if (curtask.getType() == 4 && curtask instanceof IWorkflowTask)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"casting to wf task",null,null);
                IWorkflowTask task = (IWorkflowTask)curtask;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting attachment to the task",null,null);
                IDfDocument attachedDoc = ESignHelper.getSignableDocument(getDfSession(),curtask);

                if (attachedDoc != null)
                {
                    if (attachedDoc.hasAttr("mrcs_application"))
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS doc detected",null,null);
                        // MRCS doc detected
                        String mrcsapp = attachedDoc.getString("mrcs_application");
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"mrcsapp: "+mrcsapp,null,null);
                        // get task name
                        String taskname = task.getTaskName();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"taskname: "+taskname,null,null);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up process",null,null);
                        IDfProcess wf = (IDfProcess)getDfSession().getObject(task.getProcessId());
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting process name",null,null);
                        String wfname = wf.getObjectName();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"process name: "+wfname,null,null);
                        // get config
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting ST config",null,null);
                        StateTransitionConfigFactory stconfig = StateTransitionConfigFactory.getSTConfig();
                        
                        
// QADoc hack code
                        
MrcsWorkflowTask mrcstask = stconfig.getMrcsWorkflowTask(mrcsapp,wfname,taskname);
if (mrcstask.MethodConfiguration != null && mrcstask.MethodConfiguration.containsKey("EffectiveDate")) {
	effectivedate = true;
}
                        
                        // analysis plugins
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking for analysis",null,null);
                        List analyses = null;
                        if (stconfig.isLegacyLCWF(mrcsapp)) {
                        	analyses = stconfig.getWFTaskAnalysis(mrcsapp,taskname);
                        } else {
                        	analyses = stconfig.getMrcsWorkflowTaskAnalysis(mrcsapp,wfname,taskname);
                        }

                        if (analyses != null)
                        {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation plugins defined, executing",null,null);
                            try {
                                HashMap scratchpad = new HashMap();
                                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing analysis plugin list",null,null);
                                for (int i=0; i < analyses.size(); i++)
                                {
                                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing plugin #"+i,null,null);
                                    MrcsPlugin curplugin = (MrcsPlugin)analyses.get(i);
                                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting Class for plugin: "+curplugin.PluginClassName,null,null);
                                    Class pluginclass = Class.forName(curplugin.PluginClassName);
                                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"instantiating",null,null);
                                    // reusing validation interface for analyses plugins
                                    IMrcsWorkflowValidation plugin = (IMrcsWorkflowValidation)pluginclass.newInstance();
                                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing current validation",null,null);
                                    boolean analysis = plugin.validate(getDfSession().getSessionManager(), stconfig.getApplicationDocbase(mrcsapp),attachedDoc, mrcsapp, wfname, analysismsgs,curplugin.PluginConfiguration,scratchpad);
                                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"analysis result: "+analysis,null,null);
                                    if (analysis)
                                    {
                                        analysismessage = true;
                                    }
                                }
                            } catch (Exception e) {
                                /*-ERROR-*/DfLogger.error(this,m+"Error in MRCS workflow task analyses",null,e);
                                setReturnError("MSG_DFC_ERROR", null, e);
                                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_TASK_ANALYSIS_ERROR", e);
                                return;
                            }
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"analyses execution complete, passed ---<<<>>>---",null,null);

                        }
                        // validation plugins?
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking for validations",null,null);
                        List validations = null;
                        if (stconfig.isLegacyLCWF(mrcsapp)) {
                        	validations = stconfig.getWFTaskValidations(mrcsapp,taskname);
                        } else {
                        	validations = stconfig.getMrcsWorkflowTaskValidations(mrcsapp,wfname,taskname);
                        }

                        if (validations != null)
                        {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation plugins defined, executing",null,null);
                            boolean validationpassed = true;
                            try {
                                HashMap scratchpad = new HashMap();
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
                                    validationpassed = plugin.validate(getDfSession().getSessionManager(), stconfig.getApplicationDocbase(mrcsapp),attachedDoc, mrcsapp, wfname, errmsgs,curplugin.PluginConfiguration,scratchpad);
                                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation result: "+validationpassed,null,null);
                                    if (!validationpassed)
                                    {
                                        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation failure, redirecting to message screen",null,null);
                                        //setComponentPage("mrcsvalidationfailure");
                                        //return;
                                        validationfailure = true;
                                    }
                                }
                            } catch (Exception e) {
                                /*-ERROR-*/DfLogger.error(this,m+"Error in MRCS workflow task validations",null,e);
                                setReturnError("MSG_DFC_ERROR", null, e);
                                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_TASK_VALIDATION_ERROR", e);
                                return;
                            }
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validations execution complete, passed ---<<<>>>---",null,null);

                        }

                    }
                }
            }
            //if (validationfailure) setComponentPage("mrcsvalidationfailure");
// QADoc hack code
if (effectivedate) {
	datelabel = (Label)getControl("EffectiveDateLabel",Label.class);
	datelabel.setVisible(true);
	datectrl = (DateTime)getControl("EffectiveDate",DateTime.class);
	datectrl.setVisible(true);
}

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"proceeding with standard Webtop behavior",null,null);
            super.onInit(argumentlist);

            
            if (analysismessage)
            {
                Label analysislabel = (Label)getControl("mrcsanalysismessage",Label.class);
                analysislabel.setVisible(true);
                // generate analysis label
                String analysisstring = "<BR>  MRCS TASK ANALYSIS  <BR><BR>";
                for (int msgs = 0; msgs < analysismsgs.size(); msgs++)
                {
                    // get NLS id from error list
                    Map analysisdata = (Map)analysismsgs.get(msgs);
                    String nlsmsg = (String)analysisdata.get("Analysis");
                    Object[] analysisparams = (Object[])analysisdata.get("Data");
                    // decode the NLS
                    String analysisdecode = getString(nlsmsg,analysisparams);
                    analysisstring += "  "+analysisdecode+"<BR>";
                }
                analysislabel.setLabel(analysisstring);
            }

            if (validationfailure)
            {
                //setComponentPage("start");
                // set error message on start page
                //TaskHeader.TASK_DESCR_CONTROL_NAME
                // disable start page's forward/accept/finish/next controls
                Label errmsg = (Label)getControl("mrcsvalidationerror",Label.class);
                errmsg.setVisible(true);
                // generate validation errors label
                String errorstring = "<BR>  MRCS VALIDATION ERRORS<BR>  <BR>";
                for (int errs = 0; errs < errmsgs.size(); errs++)
                {
                    // get NLS id from error list
                    Map errdata = (Map)errmsgs.get(errs);
                    String nlserr = (String)errdata.get("Error");
                    Object[] errparams = (Object[])errdata.get("Params");
                    // decode the NLS
                    String err = getString(nlserr,errparams);
                    errorstring += "  -->"+err+"<BR>";
                }
                errmsg.setLabel(errorstring);
                
                
                /*In some cases the user needs to accept the task.  Accept should behave as default.*/
                //getControl(TaskMgrContainer.ACCEPT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                getControl(TaskMgrContainer.FINISH_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                getControl(TaskMgrContainer.FORWARD_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                getControl(TaskMgrContainer.REJECT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(true); // allow them to reject
                getControl(TaskMgrContainer.REJECT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setEnabled(true); // allow them to reject
                getControl(TaskMgrContainer.DELEGATE_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);
                getControl(TaskMgrContainer.REPEAT_BUTTON_PANEL_CONTROL_NAME,Panel.class).setVisible(false);

                //Component taskheader = (Component)getControl("taskheader",Component.class);
                //Label taskdesc = (Label)taskheader.getControl(TaskHeader.TASK_DESCR_CONTROL_NAME);
                int i =1;


            }
        } catch (DfException dfe) {
            /*-ERROR-*/DfLogger.error(this,m+"error in MRCS workflow task introspection",null,dfe);
            setReturnError("MSG_DFC_ERROR", null, dfe);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_TASK_MGR_ERROR", dfe);
            // Since we are at the Container component level, we should set error messages rather than throw exceptions...
            //throw new RuntimeException("Mrcs Task Manager Container threw error",dfe);
        }
    }



    public void cancelValidation(Control button, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"cancelValidation - custom component data submit invoked",null,null);
        setComponentReturn();
    }

}
