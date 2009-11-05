/*
 * Created on Apr 25, 2005
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

 Filename       $RCSfile: NPPWorkflowTemplateLocatorQuery.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:22 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.Iterator;
import java.util.List;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.query.ParsedExpression;
import com.documentum.webcomponent.library.locator.SysObjectLocatorQuery;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

/**
 * @author prabhu1
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
//public class NPPWorkflowTemplateLocatorQuery extends MrcsWorkflowTemplateLocatorQuery {
public class NPPWorkflowTemplateLocatorQuery extends SysObjectLocatorQuery {

    private String mrcsAppName;
    private String setStateForWFfetch;
    private IDfSysObject document;


    protected void getNonContainerStatement(StringBuffer statement, String param) {
        List workflows = null;  
        /*
         * TODO Retreive the workflows configured in MrcsWFTemplates config element instead of the
         * hardcoded value below.
         */
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:NPPWorkflowTemplateLocatorQuery.getNonContainerStatement : get wf config factory.", null, null);
            StartWorkflowConfigFactory config = StartWorkflowConfigFactory.getWorkflowConfig();
            StateTransitionConfigFactory stconfig = StateTransitionConfigFactory.getSTConfig();

            if (stconfig.isLegacyLCWF(mrcsAppName))
            {
            	// LEGACY - pre mrcs4.2 compatibility logic
                workflows = config.getAllowableWorkflows(mrcsAppName, setStateForWFfetch);
            	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:MrcsWorkflowTemplateLocatorQuery.getNonContainerStatement : get list " + workflows.toString(), null, null);
            } else {
            	// NEW - mrcs4.2+ workflows
            	workflows = stconfig.getWorkflowsForCurrentState(mrcsAppName,document.getString("mrcs_config"), document.getString("mrcs_folder_config"),document.getPolicyName(),document.getCurrentStateName());            	
            }
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWorkflowTemplateLocatorQuery.getNonContainerStatement : get list "+ workflows.toString(), null, null);

        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWorkflowTemplateLocatorQuery.getNonContainerStatement : Exception occurred while obtaining Workflow configurations ", null, e);
        }
            if (workflows.size() > 0) {
                Iterator it = workflows.iterator();
                /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWorkflowTemplateLocatorQuery.getNonContainerStatement : Removing all WHERE clauses from current statement", null, null);
                removeAllWhereClauses();
                /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWorkflowTemplateLocatorQuery.getNonContainerStatement : Adding our own WHERE clause", null, null);
                addWhereClause("WHERE", new ParsedExpression(" object_name = '" + (String) it.next() + "'"));
                while (it.hasNext()) {
                    addWhereClause("OR", new ParsedExpression(" object_name = '" + (String) it.next() + "'"));
                }
                super.getNonContainerStatement(statement, param);
            } else {
                //setErrorMessage("MSG_WFSTART_NOTALLOWABLE");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWorkflowTemplateLocatorQuery.getNonContainerStatement : Workflows cannot be started in this State", null, null);
            }
    }


    protected void setStateForWFfetch(String state) {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWorkflowTemplateLocatorQuery.setStateForWFfetch : State: " + state, null, null);
        setStateForWFfetch = state;
    }


    protected void setAppName(String appName) {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " MRCS:NPPWorkflowTemplateLocatorQuery.setAppName : AppName: " + appName, null, null);
        mrcsAppName = appName;
    }

    public void setDocument(IDfSysObject doc)
    {
 	   document = doc;
    }

    
}