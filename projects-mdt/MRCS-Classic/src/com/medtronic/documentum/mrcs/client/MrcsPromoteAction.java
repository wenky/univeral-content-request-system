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

 Filename       $RCSfile: MrcsPromoteAction.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.8 $
 Modified on    $Date: 2006/12/05 20:52:10 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.PromoteAction;
import com.medtronic.documentum.mrcs.common.MrcsLCStateActions;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.IACLPlugin;
import com.medtronic.documentum.mrcs.plugin.IAttachLabelPlugin;
import com.medtronic.documentum.mrcs.plugin.IMrcsPromotePlugin;
import com.medtronic.documentum.mrcs.plugin.IMrcsRenditionPlugin;
import com.medtronic.documentum.mrcs.plugin.IRenditionPlugin;
import com.medtronic.documentum.mrcs.plugin.IStateTransitionPlugin;
import com.medtronic.documentum.mrcs.plugin.IVersionChangePlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsACLPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsAttachLabel;
import com.medtronic.documentum.mrcs.plugin.MrcsCreateRendition;
import com.medtronic.documentum.mrcs.plugin.MrcsPromote;
import com.medtronic.documentum.mrcs.plugin.MrcsVersionChange;
import com.medtronic.documentum.mrcs.plugin.PdfRenditionAlreadyPresent;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.server.MrcsPromotionServiceModule;

/*  
 * @author muellc4, usha prabhakar
 *
 * This is the MRCS manual promote command. 
 * - transactionalized with MrcsWFRollback and a wrapper transaction for post-promote activities
 */


public class MrcsPromoteAction extends PromoteAction {

    public String[] getRequiredParams() {
        return (new String[] { "objectId" });
    }


    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component) {
    	/*-CONFIG-*/String m="queryExecute ~ ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~ ", null, null);
        boolean flag = super.queryExecute(s, iconfigelement, argumentlist, context, component);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+":  Super.QueryExecute:  " + flag, null, null);
        try {
            if (flag) {
                String id = argumentlist.get("objectId");
                StateTransitionConfigFactory config = null;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :Doc Id " + id, null, null);
                IDfDocument doc = (IDfDocument) component.getDfSession().getObject(new DfId(id));
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+ ":checking access permissions", null, null);
                int permission = doc.getPermit();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+":permission level: "+permission, null, null);
                if (permission < 4)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+":permission level must be >= 4 (RELATE)", null, null);
                    return false;
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+":DOC " + doc, null, null);
                String appName = doc.getString("mrcs_application");
                String currState = doc.getCurrentStateName();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": appName " + appName, null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": Document - currState " + currState, null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": Document - lifecycle " + doc.getPolicyName(), null, null);
                
                // check legacy 4.1.2 lifecycles versus 4.2+ lifecycles
                config = StateTransitionConfigFactory.getSTConfig();
                
                if (config.isLegacyLCWF(appName)) {
                
	                if ((currState != null) && (currState.length() > 0)) {
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : config " + config, null, null);
	                    StateInfo stInfo = config.getStateInfo(appName, currState);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : stInfo " + stInfo, null, null);
	                    String PromotionType = stInfo.getPromotionType();
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : PromotionType " + PromotionType, null, null);
	
	                    if ("MANUAL".equalsIgnoreCase(PromotionType))
	                        flag = true;
	                    else
	                        flag = false;
	                } else {
	                    flag = false;
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": Document does not have any Lifeccyle attached " , null, null);
	                }
                } else {
                	// MRCS 4.2+
	                if ((currState != null) && (currState.length() > 0)) {
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : getting mrcslifecycle", null, null);	
	                	String mrcslifecycle = config.getMrcsLifecycleFromSystemLifecycleName(appName,doc.getString("mrcs_config"),doc.getString("mrcs_folder_config"),doc.getPolicyName());
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : getting lcstate for mrcslifecycle "+mrcslifecycle+" and state "+currState, null, null);	
	                	MrcsLifecycleState lcstate = (MrcsLifecycleState)config.getLifecycleState(appName,mrcslifecycle,currState);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : PromotionType " + lcstate.PromotionType, null, null);	
	                    if ("MANUAL".equalsIgnoreCase(lcstate.PromotionType))
	                        flag = true;
	                    else
	                        flag = false;
	                } else {
	                	flag = false;
	                }
                	
                }
	                
	                
	                
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": Can manually promote " + flag, null, null);
            }
        } catch (Exception exception) {
            /*-ERROR-*/DfLogger.error(this, m+":Exception Occurred: ", null, exception);
            //throw new WrapperRuntimeException("Error in MRCS Promote Action queryexecute",exception);
            return false;
        }
        return flag;
    }


    public boolean execute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context,
            Component component, Map map) {

    	/*-CONFIG-*/String m="execute-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get session from component", null, null);
        IDfSession session = component.getDfSession();
    	// override for new implementation of Lifecycles and Workflow execution
    	try {    		
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check for new LC/WF implementation - getting MRCS configuration", null, null);
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"config " + config, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting document id from action args", null, null);
            String id = argumentlist.get("objectId");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Doc Id " + id, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"retrieving object", null, null);
            IDfDocument idoc = (IDfDocument)session.getObject(new DfId(id));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"DOC " + idoc, null, null);
            String appName = idoc.getString("mrcs_application");
            if (!config.isLegacyLCWF(appName))
            {
            	// get promotion service module from 
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"prepare to retrieve promotion service module from registry", null, null);
            	//IDfClient client = session.getClient();
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"determining global registry docbase", null, null);
            	//String registrydocbase = client.getModuleRegistry().getRegistryHostName();
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"global registry docbase: "+registrydocbase, null, null);
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retrieving promotion service module from registry", null, null);
                //IDfModule promoteservicemodule = client.newModule(registrydocbase,"MrcsPromoteServiceModule",session.getSessionManager());		            	
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"module retrieved? "+(promoteservicemodule != null), null, null);
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"casting module to promotion service interface", null, null);
                //IMrcsPromotionService promotionservice = (IMrcsPromotionService)promoteservicemodule;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"instantiating promote service the old-fashioned way", null, null);
	        	MrcsPromotionServiceModule promotionservice = new MrcsPromotionServiceModule();     
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"switch to system user", null, null);
                IDfSessionManager sMgr = promotionservice.getSystemSession(config,appName);
                IDfSession syssession = sMgr.getSession(config.getApplicationDocbase(appName));
                IDfDocument sysdoc = (IDfDocument)syssession.getObject(idoc.getObjectId());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"invoking promote via manual promote command", null, null);
            	promotionservice.promoteMrcsDocument(sysdoc);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"promote returned successfully", null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"release system session", null, null);
                sMgr.release(syssession);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"released", null, null);
            	return true;
            }
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"error in new implementation promote", null, e);
            throw new WrapperRuntimeException("Error in MRCS Promote Action - New Promote",e);
        }
    	
    	// deprecated/legacy execution (pre MRCS 4.2)
        boolean flag = false;
        try {

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : LEGACY - get config", null, null);
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : config " + config, null, null);

            String id = argumentlist.get("objectId");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : Doc Id " + id, null, null);
            IDfDocument idoc = (IDfDocument) component.getDfSession().getObject(new DfId(id));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : DOC " + idoc, null, null);
            String appName = idoc.getString("mrcs_application");
            String currState = idoc.getCurrentStateName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : appName " + appName, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : currState " + currState, null, null);
            StateInfo stInfo = config.getStateInfo(appName, currState);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : stInfo " + stInfo, null, null);
            MrcsLCStateActions spclPromote = null;
            MrcsPlugin promotePlugin = stInfo.getMrcsNPPManualPromoteInfo();
            //int pluginExists = tskInfo.getMrcsPlugin().size();
            //if (pluginExists > 0)promotePlugin = (MrcsPlugin) tskInfo.getMrcsPlugin().get(0);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute: promotePlugin " + promotePlugin, null, null);

            //This performs the usual promote
            // boolean flag = super.execute(s, iconfigelement, argumentlist, context, component, map);
            /***********************Plugin instead of TBO ***************************/
            //IDfSessionManager sMgr = component.getDfSession().getSessionManager(); 
            // CEM: tranactionalize!
            // - we need to create a new session as the system user
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: creating new system-user session manager" , null, null);
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser(config.getSystemUsername(appName));
            loginInfoObj.setPassword(config.getSystemPassword(appName));
            loginInfoObj.setDomain(null);
            sMgr.setIdentity(config.getApplicationDocbase(appName), loginInfoObj);
            IDfSession syssession = sMgr.getSession(config.getApplicationDocbase(appName));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: new system-user session manager created" , null, null);

            // refetch the document with the system session, I don't trust the old doc object since it was retrieved with the non-system sMgr
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: fetching document with system session" , null, null);
            IDfDocument doc2 = (IDfDocument) syssession.getObject(new DfId(id));
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: begin transactionalized promote" , null, null);
            // backup the current document in a snapshot in case we need to roll back. 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: backing up doc with mrcs document snapshot to prep for rollback" , null, null);
            IDfPersistentObject attachment = syssession.newObject("mrcs_document_snapshot");
            attachment.setId("doc_chronicle_id", doc2.getChronicleId());
            attachment.setString("doc_version_summary", doc2.getVersionPolicy().getVersionSummary(","));
            attachment.setId("doc_object_id", doc2.getObjectId());
            attachment.setId("doc_acl_id", doc2.getACL().getObjectId());
            attachment.setTime("doc_creation_date", doc2.getCreationDate());
            attachment.setId("doc_policy_id", doc2.getPolicyId());
            attachment.setString("doc_state", doc2.getCurrentStateName());
            attachment.setString("transaction_type","lifecycle");
            attachment.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction :~~ Document backup snapshot saved ~~   ", null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup ChronicleId() " + doc2.getChronicleId(), null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup Version Summary  " + doc2.getVersionPolicy().getVersionSummary(","), null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup ObjectId()  " +  doc2.getObjectId(), null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup CreationDate() " + doc2.getCreationDate(), null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup PolicyId() " + doc2.getPolicyId(), null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup CurrentStateName() " + doc2.getCurrentStateName(), null,null);            

            IDfId newId = null;
            try { 
                //Create New Version (CEM note: had to rewrite to accomodate a sMgr with an active transaction)
                IVersionChangePlugin verPlugin = new MrcsVersionChange();
                Map verPluginParams = new HashMap();
                verPluginParams.put("StateInfo", stInfo);
                verPluginParams.put("ApplyToObject", doc2);
                newId = verPlugin.performVersionChange(sMgr,appName,verPluginParams);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : VersionChange done!! " , null, null);
    
                //Promote new document (deadlock issues here with transactions...)
                doc2 = (IDfDocument) syssession.getObject(newId);            
                IMrcsPromotePlugin pmtePlugin = new MrcsPromote();
                Map pmtePluginParams = new HashMap();
                pmtePluginParams.put("DocToPromote", doc2);
                pmtePluginParams.put("override", new Boolean (false));
                pmtePluginParams.put("testOnly", new Boolean (false));          
                pmtePlugin.mrcsPromote(pmtePluginParams);       
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : Document Promoted!! " , null, null);
            } catch (Exception e) {
                // rollback 
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteAction: exception/error in versioning or promotion, rolling back...",null,e);
                // perform mrcs manual rollback
                MrcsLCRollback rollback = new MrcsLCRollback(doc2,syssession);
                boolean rollbackflag = rollback.rollback();
                throw e;
            }

            boolean bNewtran = false;
            try {
                
                // start post-promote transaction
                if (!sMgr.isTransactionActive())
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: -->TRANSACTION BEGIN<--",null,null);
                    bNewtran = true;
                    sMgr.beginTransaction();
                }
                
                //Get the Updated Document Properties
                StateInfo newStInfo = config.getStateInfo(appName, doc2.getCurrentStateName());
                StateInfo newPrevStInfo = config.getStateInfo(appName, newStInfo.getDemoteState());
                
                //Attach Label
                IAttachLabelPlugin attchPlugin = new MrcsAttachLabel();
                Map attchPluginParams = new HashMap();
                attchPluginParams.put("ApplyToObject", doc2);
                attchPluginParams.put("LabelToAttach", newStInfo.getLabel());
                attchPluginParams.put("LabelToIgnore", newPrevStInfo.getLabel());
                attchPluginParams.put("MakeCurrent", new Boolean (newStInfo.getCurrentLabel()));   
                attchPlugin.attachlabel(sMgr, appName, attchPluginParams);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : Document Symbolic label attached!! " , null, null);
                
                //Set the ACL
                IACLPlugin aclPlugin = new MrcsACLPlugin();
                Map aclPluginParams = new HashMap();
                aclPluginParams.put("ApplyToObject", doc2);
                aclPluginParams.put("ACLName", newStInfo.getDocACL());
                aclPlugin.applyDocACL(sMgr, appName, aclPluginParams);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : Document ACL updated!! " , null, null);
                 
                //Create the Rendition - if need be
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : checking if rendition is to be generated - newStInfo.SuppressRendition: "+newStInfo.SuppressRendition , null, null);
                if (!("true".equalsIgnoreCase(newStInfo.SuppressRendition) || "yes".equalsIgnoreCase(newStInfo.SuppressRendition)))
                {
                    // see if we're using the default (original) rendition processor. If a custom one is defined, use that one
                    if (newStInfo.RenditionPluginClass == null)
                    {
                        // check for extant pdf renditions
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : checking if doc already has pdf renditions before we request them" , null, null);
                        PdfRenditionAlreadyPresent prap = new PdfRenditionAlreadyPresent();
                        boolean check = prap.hasRendition("pdf",id,syssession);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : PRAP.hasRendition returned: "+check , null, null);
                        if (check) {
                            /*-WARN-*/DfLogger.warn(this, "MRCS:MrcsPromoteAction :execute : bypassing rendition generation (default implementation) - rendition already present" , null, null);
                        } else {                               
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute : generating rendition (default implementation)" , null, null);
                            IRenditionPlugin renditionPlugin = new MrcsCreateRendition(); 
                            Map renditionPluginParams = new HashMap();
                            renditionPluginParams.put("DocObject", doc2);
                            renditionPlugin.createRendition(renditionPluginParams);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: executeWFAction: Pdf Rendition of Document created!! " , null, null);
                        }
                    }
                    else
                    {
                        // instantiate the Rendition plugin
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: executeWFAction: custom rendition plugin registered" , null, null);
                        String renditionpluginclass = newStInfo.RenditionPluginClass;
                        Map renditionpluginconfig = newStInfo.RenditionPluginConfig;
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: instantiating rendition plugin "+newStInfo.RenditionPluginClass, null, null);
                        IMrcsRenditionPlugin renditionPlugin = (IMrcsRenditionPlugin)Class.forName(renditionpluginclass).newInstance();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: executing rendition plugin ", null, null);
                        renditionPlugin.render(syssession,doc2,renditionpluginconfig);                        
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: rendition plugin exec complete", null, null);
                    }
                }
                
                //Create Copy of the Document [TBD]
                /***********************Plugin instead of TBO ***************************/
                
                if (promotePlugin != null) {
                    spclPromote = (MrcsLCStateActions) Class.forName(promotePlugin.PluginClassName).newInstance();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute: Mrcs Promote class " + spclPromote, null, null);
                    Map params = new HashMap();
                    params.put("DFSession", syssession);
                    //params.put("StateInfo", stInfo);
                    params.put("StateInfo", newStInfo);
                    //params.put("DocId", id);
                    params.put("DocId", newId.getId());
                    flag = spclPromote.executeLCAction(params);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction :execute after promote " + flag, null, null);
                }
    
                // CEM - post-promote plugin layer            
                List transactionplugins = stInfo.getPostPromotePlugins();
                if (transactionplugins != null)
                {
    
                    // get a session...
                    IDfDocument newdoc = (IDfDocument)syssession.getObject(newId);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction :execute INTRA-TRANSACTION plugins defined, executing",null,null);
                    try {
                        HashMap scratchpad = new HashMap();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction :execute INTRA-TRANSACTION executing plugin list",null,null);
                        for (int i=0; i < transactionplugins.size(); i++)
                        {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction :execute INTRA-TRANSACTION executing plugin #"+i,null,null);
                            MrcsPlugin curplugin = (MrcsPlugin)transactionplugins.get(i);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction :execute getting Class for plugin: "+curplugin.PluginClassName,null,null);
                            Class pluginclass = Class.forName(curplugin.PluginClassName);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction :execute instantiating",null,null);
                            IStateTransitionPlugin plugin = (IStateTransitionPlugin)pluginclass.newInstance();
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction :execute executing current plugin",null,null);
                            plugin.execute(sMgr,config.getApplicationDocbase(appName),stInfo,appName,doc2,newdoc,curplugin.PluginConfiguration, scratchpad);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction :execute current plugin executed",null,null);
                        }
                    } catch (Exception e) {
                        /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteAction :execute Error in INTRA-TRANSACTION postpromote plugin execution",null,e);
                        throw e;
                    }
                }
                // CEM - end post-promote plugin layer
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: check if we need to commit (tranaction is local to this method)",null,null);
                if (bNewtran && sMgr.isTransactionActive()) {
                    // commit if the transaction is local to this method (might not be if this is part of a plugin sequence to, say, new GF)
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: transaction is local -->TRANSACTION COMMIT<--",null,null);
                    sMgr.commitTransaction();
                }
                //CEM - CURSE DCTM - sigh - Post-Transaction plugin layer, since we can't do LC attach/detach inside a transaction...
                List plugins = stInfo.getPostTransactionPromotePlugins();
                if (plugins != null)
                {
    
                    // get a session...
                    IDfDocument newdoc = (IDfDocument)syssession.getObject(newId);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: POST-TRANSACTION plugins defined, executing",null,null);
                    try {
                        HashMap scratchpad = new HashMap();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: executing POST-TRANSACTION plugin list",null,null);
                        for (int i=0; i < plugins.size(); i++)
                        {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: executing plugin #"+i,null,null);
                            MrcsPlugin curplugin = (MrcsPlugin)plugins.get(i);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: getting Class for plugin: "+curplugin.PluginClassName,null,null);
                            Class pluginclass = Class.forName(curplugin.PluginClassName);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: instantiating",null,null);
                            IStateTransitionPlugin plugin = (IStateTransitionPlugin)pluginclass.newInstance();
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: executing current plugin",null,null);
                            plugin.execute(sMgr,config.getApplicationDocbase(appName),stInfo,appName,doc2,newdoc,curplugin.PluginConfiguration, scratchpad);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: current plugin executed",null,null);
                        }
                    } catch (Exception e) {
                        /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteAction: Error in POST-TRANSACTION postpromote plugin execution",null,e);
                        throw e;
                    }
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: POSt-TRANACTION plugins execution complete ---<<<>>>---",null,null);
                }
                //CEM - end post-transaction plugin layer...
                
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteAction: exception/error in post-promote processing, aborting transaction, rolling back...",null,e);
                if (sMgr.isTransactionActive()) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: transaction abort -->ABORT TRANSACTION ABORT<--",null,null);
                    sMgr.abortTransaction();
                }
                // perform mrcs manual rollback
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: executing manual MRCS rollback",null,null);
                MrcsLCRollback rollback = new MrcsLCRollback(doc2,syssession);
                boolean rollbackflag = rollback.rollback();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteAction: rollback successful? "+rollbackflag,null,null);
                throw e;
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: execution done ", null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsPromoteAction:execute error in promote", null, e);
            throw new WrapperRuntimeException("Error in MRCS Promote Action",e);
        }
        return flag;
    }
}