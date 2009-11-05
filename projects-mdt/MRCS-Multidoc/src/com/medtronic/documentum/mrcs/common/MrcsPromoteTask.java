/*
 * Created on Apr 18, 2005
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

 Filename       $RCSfile: MrcsPromoteTask.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/12/05 20:52:29 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


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
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.client.MrcsLCRollback;
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
import com.medtronic.documentum.mrcs.sbo.dto.WFTaskInfo;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsPromoteTask implements MrcsWFTaskActions {

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.common.MrcsWFTaskActions#executeWFAction(java.util.Map)
     */
    public void executeWFAction(Map params) throws DfException 
    {
       try{ 
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : get config", null, null);
           StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction:execute : config " + config, null, null);

           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: Class Name" + getClass().getName(), null, null);
           Map configParams = (Map)params.get("MrcsConfig");
           // params.get("IDfSession",getDfSession());
           IDfDocument docObject1 = (IDfDocument) params.get("IDfDocument");
           String docid = docObject1.getObjectId().toString();
           String appName = docObject1.getString("mrcs_application");
           WFTaskInfo tskInfo = (WFTaskInfo) params.get("WfTask");
           StateInfo stInfo = (StateInfo) params.get("StateInfo");
           IDfSession session = (IDfSession) params.get("IDfSession");
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: DOCUMENT VERSION SUMMARY " + docObject1.getVersionPolicy().getVersionSummary(","), null, null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: tskInfo.getAction() " +tskInfo.getAction(), null, null);
           MrcsLCStateActions spclPromote = null;
           MrcsPlugin promotePlugin = stInfo.getMrcsNPPManualPromoteInfo();

           // create promote-specific snapshot for the promote
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: begin transactionalized promote" , null, null);
           // backup the current document in a snapshot in case we need to roll back. 
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteAction: backing up doc with mrcs document snapshot to prep for rollback" , null, null);
           IDfPersistentObject attachment = session.newObject("mrcs_document_snapshot");
           attachment.setId("doc_chronicle_id", docObject1.getChronicleId());
           attachment.setString("doc_version_summary", docObject1.getVersionPolicy().getVersionSummary(","));
           attachment.setId("doc_object_id", docObject1.getObjectId());
           attachment.setId("doc_acl_id", docObject1.getACL().getObjectId());
           attachment.setTime("doc_creation_date", docObject1.getCreationDate());
           attachment.setId("doc_policy_id", docObject1.getPolicyId());
           attachment.setString("doc_state", docObject1.getCurrentStateName());
           attachment.setString("transaction_type","lifecycle");
           attachment.save();
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction :~~ Document backup snapshot saved ~~   ", null,null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup ChronicleId() " + docObject1.getChronicleId(), null,null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup Version Summary  " + docObject1.getVersionPolicy().getVersionSummary(","), null,null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup ObjectId()  " +  docObject1.getObjectId(), null,null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup CreationDate() " + docObject1.getCreationDate(), null,null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup PolicyId() " + docObject1.getPolicyId(), null,null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsPromoteAction : -->backup CurrentStateName() " + docObject1.getCurrentStateName(), null,null);            
           
           
           if(tskInfo.getAction().equalsIgnoreCase("promote"))
           {
               // CEM: tranactionalize!
               // - we need to create a new session as the system user
               // - since this is a WF-based promote, there should already be a mrcs_document_snapshot for rollback purposes...
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: creating new system-user session manager" , null, null);
               IDfClientX clientx = new DfClientX();
               IDfClient client = clientx.getLocalClient();
               IDfSessionManager sMgr = client.newSessionManager();
               IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
               loginInfoObj.setUser(config.getSystemUsername(appName));
               loginInfoObj.setPassword(config.getSystemPassword(appName));
               loginInfoObj.setDomain(null);
               sMgr.setIdentity(config.getApplicationDocbase(appName), loginInfoObj);
               IDfSession syssession = sMgr.getSession(config.getApplicationDocbase(appName));
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: new system-user session manager created" , null, null);
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: promote to be performed" , null, null);
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: getting session manager" , null, null);
                   
               // re-retrieve the document object with the sysuser session
               IDfDocument docObject = (IDfDocument)syssession.getObject(new DfId(docid));
           
               //docObject.promote("", true, false);
               /***********************Plugin instead of TBO ***************************/
               IDfId newId = null;
               try {
                   //Create New Version
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: begin pre-promote versioning" , null, null);
                   IVersionChangePlugin verPlugin = new MrcsVersionChange();
                   Map verPluginParams = new HashMap();
                   verPluginParams.put("StateInfo", stInfo);
                   verPluginParams.put("ApplyToObject", docObject);
                   newId = verPlugin.performVersionChange(sMgr,appName,verPluginParams);
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: VersionChange done!! " , null, null);
       
                   //Promote new document
                   // - due to deadlock issues (this creates its own transaction on this document), this cannot be inside a transaction
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: begin document promotion" , null, null);
                   docObject = (IDfDocument) syssession.getObject(newId);            
                   IMrcsPromotePlugin pmtePlugin = new MrcsPromote();
                   Map pmtePluginParams = new HashMap();
                   pmtePluginParams.put("DocToPromote", docObject);
                   pmtePluginParams.put("override", new Boolean (false));
                   pmtePluginParams.put("testOnly", new Boolean (false));          
                   pmtePlugin.mrcsPromote(pmtePluginParams);       
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: Document Promoted!! " , null, null);
               } catch (Exception e) {
                   /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteTask: exception/error in versioning or promotion, rolling back...",null,e);
                   // perform mrcs manual rollback
                   MrcsLCRollback rollback = new MrcsLCRollback(docObject,syssession);
                   boolean rollbackflag = rollback.rollback();
                   throw e;
               }
                                           
               boolean bNewtran = false;
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: begin transactionalized post-promote" , null, null);
               try {
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: check if transaction already active" , null, null);
                   if (!sMgr.isTransactionActive())
                   {
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: -->TRANSACTION BEGIN<--",null,null);
                       bNewtran = true;
                       sMgr.beginTransaction();
                   }
                   
                   //Get the Updated Document Properties
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: config " + config, null, null);
                   StateInfo newStInfo = config.getStateInfo(appName, docObject.getCurrentStateName());
                   StateInfo newPrevStInfo = config.getStateInfo(appName, newStInfo.getDemoteState());
                   
                   //Attach Label
                   IAttachLabelPlugin attchPlugin = new MrcsAttachLabel();
                   Map attchPluginParams = new HashMap();
                   attchPluginParams.put("ApplyToObject", docObject);
                   attchPluginParams.put("LabelToAttach", newStInfo.getLabel());
                   attchPluginParams.put("LabelToIgnore", newPrevStInfo.getLabel());
                   attchPluginParams.put("MakeCurrent", new Boolean (newStInfo.getCurrentLabel()));   
                   attchPlugin.attachlabel(sMgr, appName, attchPluginParams);
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: Document Symbolic label attached!! " , null, null);
                   
                   //Set the ACL
                   IACLPlugin aclPlugin = new MrcsACLPlugin();
                   Map aclPluginParams = new HashMap();
                   aclPluginParams.put("ApplyToObject", docObject);
                   aclPluginParams.put("ACLName", newStInfo.getDocACL());
                   aclPlugin.applyDocACL(sMgr, appName, aclPluginParams);
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: Document ACL updated!! " , null, null);
                    
                   //Create the Rendition
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask :execute : checking if rendition is to be generated - newStInfo.SuppressRendition: "+newStInfo.SuppressRendition , null, null);
                   if (!("true".equalsIgnoreCase(newStInfo.SuppressRendition) || "yes".equalsIgnoreCase(newStInfo.SuppressRendition)))
                   {
                       // see if we're using the default (original) rendition processor. If a custom one is defined, use that one
                       if (newStInfo.RenditionPluginClass == null)
                       {
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask :execute : checking if doc already has pdf renditions before we request them" , null, null);
                           PdfRenditionAlreadyPresent prap = new PdfRenditionAlreadyPresent();
                           boolean check = prap.hasRendition("pdf",docid,syssession);
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask :execute : PRAP.hasRendition returned: "+check , null, null);
                           if (check)
                           {
                               /*-WARN-*/DfLogger.warn(this, "MRCS:MrcsPromoteTask :execute : bypassing rendition generation (default implementation) - rendition already present" , null, null);
                           } else {                               
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask :execute : generating rendition (default implementation)" , null, null);
                               IRenditionPlugin renditionPlugin = new MrcsCreateRendition(); 
                               Map renditionPluginParams = new HashMap();
                               renditionPluginParams.put("DocObject", docObject);
                               renditionPlugin.createRendition(renditionPluginParams);
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: Pdf Rendition of Document created!! " , null, null);
                           }
                       }
                       else
                       {
                           // instantiate the Rendition plugin
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executeWFAction: custom rendition plugin registered" , null, null);
                           String renditionpluginclass = newStInfo.RenditionPluginClass;
                           Map renditionpluginconfig = newStInfo.RenditionPluginConfig;
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: instantiating rendition plugin "+newStInfo.RenditionPluginClass, null, null);
                           IMrcsRenditionPlugin renditionPlugin = (IMrcsRenditionPlugin)Class.forName(renditionpluginclass).newInstance();
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: executing rendition plugin ", null, null);
                           renditionPlugin.render(syssession,docObject,renditionpluginconfig);                        
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask: rendition plugin exec complete", null, null);
                       }
                   }
                   
                   //Create Copy of the Document [TBD]
                   /***********************Plugin instead of TBO ***************************/
       
                   if (promotePlugin != null) {
                       spclPromote = (MrcsLCStateActions) Class.forName(promotePlugin.PluginClassName).newInstance();
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask:execute: Mrcs Promote class " + spclPromote, null, null);
                       Map copyparams = new HashMap();
                       copyparams.put("DFSession", syssession);
                       //params.put("StateInfo", stInfo);
                       copyparams.put("StateInfo", newStInfo);
                       //params.put("DocId", id);
                       copyparams.put("DocId", newId.getId());
                       boolean flag = spclPromote.executeLCAction(params);
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsPromoteTask :execute after promote " + flag, null, null);
                   }
                   
                   // CEM - post-promote plugin layer            
                   List transactionplugins = stInfo.getPostPromotePlugins();
                   if (transactionplugins != null)
                   {
       
                       // get a session...
                       IDfDocument newdoc = (IDfDocument)syssession.getObject(newId);
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: INTRA-TRANACTION plugins defined, executing",null,null);
                       try {
                           HashMap scratchpad = new HashMap();
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: executing INTRA-TRANACTION plugin list",null,null);
                           for (int i=0; i < transactionplugins.size(); i++)
                           {
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: executing plugin #"+i,null,null);
                               MrcsPlugin curplugin = (MrcsPlugin)transactionplugins.get(i);
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: getting Class for plugin: "+curplugin.PluginClassName,null,null);
                               Class pluginclass = Class.forName(curplugin.PluginClassName);
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: instantiating",null,null);
                               IStateTransitionPlugin plugin = (IStateTransitionPlugin)pluginclass.newInstance();
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: executing current plugin",null,null);
                               plugin.execute(sMgr,config.getApplicationDocbase(appName),stInfo,appName,docObject,newdoc,curplugin.PluginConfiguration, scratchpad);
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: current plugin executed",null,null);
                           }
                       } catch (Exception e) {
                           /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteTask: Error in INTRA-TRANACTION postpromote plugin execution",null,e);
                           throw e;
                       }
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: INTRA-TRANACTION plugins execution complete ---<<<>>>---",null,null);
                   }
                   // CEM - end post-promote plugin layer
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: check if we need to commit (tranaction is local to this method)",null,null);
                   if (bNewtran && sMgr.isTransactionActive()) {
                       // commit if the transaction is local to this method (might not be if this is part of a plugin sequence to, say, new GF)
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: transaction is local -->TRANSACTION COMMIT<--",null,null);
                       sMgr.commitTransaction();
                   }
                   
                   //CEM - CURSE DCTM - sigh - Post-Transaction plugin layer, since we can't do LC attach/detach inside a transaction...
                   List plugins = stInfo.getPostTransactionPromotePlugins();
                   if (plugins != null)
                   {
       
                       // get a session...
                       IDfDocument newdoc = (IDfDocument)syssession.getObject(newId);
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: POST-TRANSACTION plugins defined, executing",null,null);
                       try {
                           HashMap scratchpad = new HashMap();
                           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: executing POST-TRANSACTION plugin list",null,null);
                           for (int i=0; i < plugins.size(); i++)
                           {
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: executing plugin #"+i,null,null);
                               MrcsPlugin curplugin = (MrcsPlugin)plugins.get(i);
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: getting Class for plugin: "+curplugin.PluginClassName,null,null);
                               Class pluginclass = Class.forName(curplugin.PluginClassName);
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: instantiating",null,null);
                               IStateTransitionPlugin plugin = (IStateTransitionPlugin)pluginclass.newInstance();
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: executing current plugin",null,null);
                               plugin.execute(sMgr,config.getApplicationDocbase(appName),stInfo,appName,docObject,newdoc,curplugin.PluginConfiguration, scratchpad);
                               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: current plugin executed",null,null);
                           }
                       } catch (Exception e) {
                           /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteTask: Error in POST-TRANSACTION postpromote plugin execution",null,e);
                           throw e;
                       }
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: POSt-TRANACTION plugins execution complete ---<<<>>>---",null,null);
                   }
                   //CEM - end post-transaction plugin layer...
               } catch (Exception e) {
                   /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteTask: exception/error in post-promote processing, aborting transaction, rolling back...",null,e);
                   if (sMgr.isTransactionActive()) {
                       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: transaction abort -->ABORT TRANSACTION ABORT<--",null,null);
                       sMgr.abortTransaction();
                   }
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: executing manual MRCS rollback",null,null);
                   MrcsLCRollback rollback = new MrcsLCRollback(docObject,syssession);
                   boolean rollbackflag = rollback.rollback();
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsPromoteTask: rollback successful? "+rollbackflag,null,null);
                   throw e;
               }
           }
       }catch(Exception e ){
           /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsPromoteTask: executeWFAction: Exception occurred",null, e);
           throw new RuntimeException("executeWFAction threw exception",e);
       }
    }

}
