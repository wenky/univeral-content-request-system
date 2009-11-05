/*
 * Created on Mar 3, 2005
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

 Filename       $RCSfile: StartWorkflowConfigFactory.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/06/30 22:53:23 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.config;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;
import com.documentum.services.workflow.inbox.ITask;
import com.medtronic.documentum.mrcs.plugin.MrcsCustomSignatureRedirect;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class StartWorkflowConfigFactory extends GenericConfigFactory {

    private static StartWorkflowConfigFactory _wfConfig = null;


    /**
     * @throws Exception
     */
    private StartWorkflowConfigFactory()
    {
        super();
    }


    /**
     * Obtain the Start Workflow ConfigFactory
     *
     * @return @throws
     *         Exception
     */
    public static StartWorkflowConfigFactory getWorkflowConfig()
    {

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(StartWorkflowConfigFactory.class))DfLogger.debug(StartWorkflowConfigFactory.class, "MRCS:StartWorkflowConfigFactory.getConfigBroker - check if we need to instantiate", null,null);
        // our factory method for configbroker
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(StartWorkflowConfigFactory.class))DfLogger.debug(StartWorkflowConfigFactory.class, "MRCS:StartWorkflowConfigFactory.getConfigBroker - check if we need to instantiate", null,null);
        if (_wfConfig == null) {
            synchronized (StartWorkflowConfigFactory.class) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(StartWorkflowConfigFactory.class))DfLogger.debug(StartWorkflowConfigFactory.class, "MRCS:StartWorkflowConfigFactory.getConfigBroker - need to instantiate and load config", null,null);
                _wfConfig = new StartWorkflowConfigFactory();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(StartWorkflowConfigFactory.class))DfLogger.debug(StartWorkflowConfigFactory.class, "MRCS:StartWorkflowConfigFactory.getConfigBroker - config loaded", null,null);
            }
        }

        return _wfConfig;
    }    

    /**
     * Get the configured State Information for the current application.
     *
     * @deprecated
     * @param application
     * @return @throws
     *         Exception
     */
    public List getWorkflows(String application) throws Exception {
        // we know the app from the Document DCTM object, passed in as the application parameter
        MrcsApplication app = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getWorkflows application: " + app, null,null);

        Map doctypes = app.DocumentTypes;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getWorkflows doctypes: " + doctypes.size(), null,null);

        Collection c = doctypes.values();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getWorkflows collection: " + c.size(), null,null);

        Iterator it = c.iterator();
       // /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getWorkflows iterator: " + it, null,null);

        List wfTemplates = new ArrayList();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getWorkflows new ArrayList.", null,null);

        while (it.hasNext()) {
            MrcsDocumentType doctype = (MrcsDocumentType) it.next();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getWorkflows doctype: " + doctype.Name, null,null);
            wfTemplates.addAll(doctype.MrcsWFTemplates);
        }
        return wfTemplates;
    }


    /**
     * Get the configured State Information for the current application.
     *
     * @deprecated
     * @param application
     * @return @throws
     *         Exception
     */
    public List getAllowableWorkflows(String application, String state) throws Exception {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : for application: "+ application + " and State: " + state, null,null);
        List allowablewfs = null;
        String docwf = null;
        String docStatewf = null;
        List docwfs = getWorkflows(application);

        if ((state != null) && (state.length() > 0)) {
            allowablewfs = new ArrayList();
            // we know the app from the Document DCTM object, passed in as the application parameter
            MrcsApplication app = getApplication(application);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : getStateInfo " + app, null,null);
            StateInfo stInf = (StateInfo) app.MrcsLifecycleState.get(state);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : stInf" + stInf, null,null);
            List ablefs = stInf.getAllowableWorkflows();

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : NO. of Allowable Workflows in this State "+ ablefs.size(), null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : NO. of Allowable Workflows for this document type "+ docwfs.size(), null,null);

            for (int t = 0; t < ablefs.size(); t++) {
                docStatewf = ((String) ablefs.get(t)).trim();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : docStatewf : "+ docStatewf, null,null);
                for (int i = 0; i < docwfs.size(); i++) {
                    docwf = ((String) docwfs.get(i)).trim();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : docwf : " + docwf, null, null);
                    if (docwf.equals(docStatewf)) {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : Allowable Workflows in this State "+ docStatewf, null,null);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : Allowable Workflows for this document type  " + docwf, null,null);
                        allowablewfs.add(docStatewf);
                        continue;
                    }

                }
            }
        } else {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : No Lifecycle Applied!!! ", null,null);
            allowablewfs = docwfs;
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StartWorkflowConfigFactory.getAllowableWorkflows : NO. of allowablewfs (returned) "+ allowablewfs.size(), null,null);
        return allowablewfs;
    }



    /**
     * 
     * @deprecated
     * 
     * @param session
     * @param mrcsapp
     * @param workflowtask
     * @param action
     * @return
     */
   
    public String getCustomSigningComponent(IDfSession session, String mrcsapp, ITask workflowtask, String action)
    {
        /*-CONFIG-*/String m="getCustomSigningComponent-";
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checking for custom esigner in mrcsapp "+mrcsapp, null,null);
        if (app.ESignature != null && app.ESignature.CustomSignaturePlugin != null) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing component redirect plugin"+mrcsapp, null,null);
            try {
                MrcsCustomSignatureRedirect redirect = (MrcsCustomSignatureRedirect)Class.forName(app.ESignature.CustomSignaturePlugin.PluginClassName).newInstance();
                String redirectcomponentname = redirect.getRedirect(session,mrcsapp,workflowtask,action,app.ESignature.CustomSignaturePlugin.PluginConfiguration);
                return redirectcomponentname;                
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this, m+"Exception in esign redirection plugin", null,null);
                throw new RuntimeException("Custom Esign redirection error",e);
            }
            
        }
        else 
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"no custom esigner registered, returning null", null,null);
            return null;
        }
        
    }
    
    /**
     * @deprecated
     * @param application
     * @return
     */
    
    public Map getCustomSigningConfiguration(String application)
    {
        /*-CONFIG-*/String m="getCustomSigningConfiguration-";
        MrcsApplication app = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retrieving custom esign configuration", null,null);
        return app.ESignature.CustomSignaturePlugin.PluginConfiguration;
    }
    
    /**
     * @deprecated
     * @param application
     * @return
     */
    
    public boolean hasCustomTemplateQuery(String application)
    {
        /*-CONFIG-*/String m="hasCustomTemplateQuery-";
        MrcsApplication app = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checking for custom workflow locator query configuration", null,null);
        return app.CustomWorkflowLocatorQuery != null;        
    }

    
    /**
     * @deprecated
     * @param application
     * @return
     */
    public MrcsPlugin getCustomTemplateQueryPlugin(String application)
    {
        /*-CONFIG-*/String m="getCustomTemplateQueryPlugin-";
        MrcsApplication app = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"returning custom workflow locator query configuration", null,null);
        return app.CustomWorkflowLocatorQuery;        
    }
    
}