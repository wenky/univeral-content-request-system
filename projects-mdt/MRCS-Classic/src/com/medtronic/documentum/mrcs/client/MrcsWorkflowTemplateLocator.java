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

 Filename       $RCSfile: MrcsWorkflowTemplateLocator.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/06/30 22:53:22 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.webcomponent.library.locator.LocatorQuery;
import com.documentum.webcomponent.library.workflow.UserWorkflowTemplateLocator;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;

/**
 * @author hansom5
 *
 * TODO Start the workflow if there is only one template returned.
 * TODO If there is an action configured (to promote, for example), perform it.
 */
public class MrcsWorkflowTemplateLocator extends UserWorkflowTemplateLocator
 {

    private IDfDocument docObject;
    private String[] val;

    public void onInit(ArgumentList argumentlist) {
        val = argumentlist.getValues("objectId");
        try {
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWorkflowTemplateLocator.onInit - instantiate the document.", null, null);
            docObject = (IDfDocument) getDfSession().getObject(new DfId(val[0]));
        }
        catch (DfException dfe){
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:MrcsWorkflowTemplateLocator.onInit - exception caught: "+ dfe, null, null);
    		throw new WrapperRuntimeException(dfe);
        }
        super.onInit(argumentlist);
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWorkflowTemplateLocator.onInit - Query Statement: "+ getQueryStatement(), null, null);
    }

    protected LocatorQuery createQuery()
    {
        /*-CONFIG-*/String m="LocatorQuery-";
        // CEM: adding a plugin for pluggable query objects
        
        // --> this can be overriden using more traditional webtop means. Look at how NPP overrides the locator query with the NPPTemplateLocator component override...
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Checking for custom wf locator query plugin", null, null);
//        try { 
//            String mrcsapp = docObject.getString("mrcs_application");
//            StartWorkflowConfigFactory wfconfig = StartWorkflowConfigFactory.getWorkflowConfig();
//            if (wfconfig.hasCustomTemplateQuery(mrcsapp))
//            {
//                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"There is indeed a custom wf locator", null, null);
//                MrcsPlugin customqueryplugin = wfconfig.getCustomTemplateQueryPlugin(mrcsapp);
//                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locator class: "+customqueryplugin.PluginClassName, null, null);
//                // must subclass SysObjectLocatorQuery
//                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locator class: instantiating", null, null);
//                MrcsCustomWorkflowTemplateLocatorQuery customquery = (MrcsCustomWorkflowTemplateLocatorQuery)Class.forName(customqueryplugin.PluginClassName).newInstance();
//                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locator class: initializing", null, null);
//                if (customquery.queryInit(getDfSession(),mrcsapp,val,customqueryplugin.PluginConfiguration))
//                {
//                    customquery.setPrivateCabinetVisible(super.m_fIsPrivateCabinetVisible);
//                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locator class: returning", null, null);
//                    return customquery;
//                }
//            }
//        } catch (Exception e) {
//            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWorkflowTemplateLocator.createQuery - exception caught: "+e, null, e);
//            throw new WrapperRuntimeException(e);
//        }
        
        // CEM: default impl...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"no custom query, doing default MRCS locator (wfs associated with current doc's LCstate)", null, null);
        MrcsWorkflowTemplateLocatorQuery mrcsworkflowtemplatelocatorquery = null;
        try {
	        mrcsworkflowtemplatelocatorquery = new MrcsWorkflowTemplateLocatorQuery();
	        mrcsworkflowtemplatelocatorquery.setDocument(docObject);
	        mrcsworkflowtemplatelocatorquery.setAppName(docObject.getString("mrcs_application"));
	        mrcsworkflowtemplatelocatorquery.setPrivateCabinetVisible(super.m_fIsPrivateCabinetVisible);
        }
        catch (DfException dfe){
            /*-ERROR-*/DfLogger.error(this, m+" - exception caught: "+dfe, null, dfe);
            throw new WrapperRuntimeException(dfe);
        }
        finally {
	        return mrcsworkflowtemplatelocatorquery;
        }
    }

    /**
     *
     */
    public MrcsWorkflowTemplateLocator() {
        super();
    }


}