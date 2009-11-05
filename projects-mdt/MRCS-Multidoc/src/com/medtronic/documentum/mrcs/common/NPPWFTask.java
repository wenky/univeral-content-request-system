/*
 * Created on Apr 18, 2005
 *
 * SYSTEM ACCOUNT MUST BE SUPERUSER IN ORDER TO MUCK WITH THE r_immutable_flag property!
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

 Filename       $RCSfile: NPPWFTask.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:50 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.IACLPlugin;
import com.medtronic.documentum.mrcs.plugin.IAttachLabelPlugin;
import com.medtronic.documentum.mrcs.plugin.IMrcsPromotePlugin;
import com.medtronic.documentum.mrcs.plugin.IRenditionPlugin;
import com.medtronic.documentum.mrcs.plugin.IVersionChangePlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsACLPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsAttachLabel;
import com.medtronic.documentum.mrcs.plugin.MrcsCreateRendition;
import com.medtronic.documentum.mrcs.plugin.MrcsPromote;
import com.medtronic.documentum.mrcs.plugin.MrcsVersionChange;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.sbo.dto.WFTaskInfo;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class NPPWFTask implements MrcsWFTaskActions {

    private IDfSessionManager _sMgr = null;

    private IDfId docId = null;

    private IDfId _newObjId = null;

    private String appName = null;

    private IDfSession _session = null;

    private IDfDocument docInWf = null;

    String MrcsLCToApply = null;

    String VersionToNext = "SAME";

    boolean makeAllVersionsObsolete = false;

    private IDfSession origSession = null;

    private IDfDocument docObject = null;

    private WFTaskInfo tskInfo = null;

    private StateInfo stInfo = null;

    private ArrayList allVTIds = new ArrayList();


    //String MrcsLCStateToApply = null;

    /**
     *
     */
    public NPPWFTask() {
        super();
        // TODO Auto-generated constructor stub
    }


    /*
     * (non-Javadoc)
     *
     * @see com.medtronic.documentum.mrcs.common.MrcsWFTaskActions#executeWFAction(java.util.Map)
     */
    public void executeWFAction(Map params) throws DfException {
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction.executeWFAction " + getClass().getName(), null, null);
            Map configParams = (Map) params.get("MrcsConfig");
            origSession = (IDfSession) params.get("IDfSession");
            docObject = (IDfDocument) params.get("IDfDocument");
            docId = docObject.getObjectId();
            appName = docObject.getString("mrcs_application");
            tskInfo = (WFTaskInfo) params.get("WfTask");
            stInfo = (StateInfo) params.get("StateInfo");

            MrcsLCToApply = (String) configParams.get("MrcsLCToApply");
            VersionToNext = (String) configParams.get("VersionToNext");
            makeAllVersionsObsolete = Boolean.valueOf((String) configParams.get("MakeAllVersionsObsolete")).booleanValue();

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction.applyLifecycle - MrcsLCToApply: " + MrcsLCToApply, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction.applyLifecycle - VersionToNext: " + VersionToNext, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction.applyLifecycle - makeAllVersionsObsolete: "+ makeAllVersionsObsolete, null, null);

            //Update the document properties
            //Change the version of the document
            //if ((VersionToNext != null) && (VersionToNext.length() > 0))
            versionToNext();
            //Attach and detach the Lifecycle
            if ((MrcsLCToApply != null) && (MrcsLCToApply.length() > 0)) {
                changeLC(_newObjId, MrcsLCToApply, "", false);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction : applyLifecycle - done, > attachLabelAfterLC", null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction : MakeLatestCurrent:(A) DOCUMENT VERSION SUMMARY "+ docObject.getVersionPolicy().getVersionSummary(","), null, null);
            }

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction : Before makeVTObsolete - DOCUMENT VERSION SUMMARY "+ docObject.getVersionPolicy().getVersionSummary(","), null, null);
            //Also perform special promote??
            makeVTObsolete();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction : After makeVTObsolete - DOCUMENT VERSION SUMMARY "+ docObject.getVersionPolicy().getVersionSummary(","), null, null);
            docObject = (IDfDocument) origSession.getObject(_newObjId);
            String state = docObject.getCurrentStateName();
            StateTransitionConfigFactory sts = StateTransitionConfigFactory.getSTConfig();        
            String statename = sts.getStateInfo(docObject.getString("mrcs_application"),state).getLabel(); 
            docObject.mark(statename);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.executeWFAction : (docObject) DOCUMENT VERSION SUMMARY "+ docObject.getVersionPolicy().getVersionSummary(","), null, null);
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.executeWFAction : Exception occurred : " + e, null, null);
        }
    }


    private void versionToNext() throws DfException {
        try {
            setSysSession();
            docInWf = (IDfDocument) _session.getObject(docId);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.versionToNext: docInWf " + docInWf, null, null);

            String verLabel = null;
            IDfVersionPolicy verPolicy = docInWf.getVersionPolicy();
            if (VersionToNext.equalsIgnoreCase("MAJOR")) {
                verLabel = verPolicy.getNextMajorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.versionToNext: Next MAJOR verLabel  " + verLabel, null, null);
                if (!docInWf.isCheckedOut())
                    docInWf.checkout();
                _newObjId = docInWf.checkin(false, verLabel);
                docInWf = (IDfDocument) _session.getObject(_newObjId);
                docInWf.mark("CURRENT");
                docInWf.mark(docInWf.getCurrentStateName());                
                docInWf.save();
            } else if (VersionToNext.equalsIgnoreCase("MINOR")) {
                verLabel = verPolicy.getNextMinorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.versionToNext: Next MINOR verLabel  " + verLabel, null, null);
                if (!docInWf.isCheckedOut())
                    docInWf.checkout();
                _newObjId = docInWf.checkin(false, verLabel);
                docInWf = (IDfDocument) _session.getObject(_newObjId);
                docInWf.mark("CURRENT");
                docInWf.mark(docInWf.getCurrentStateName());                
                docInWf.save();
            } else if (VersionToNext.equalsIgnoreCase("BRANCH")) {
                verLabel = verPolicy.getBranchLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.versionToNext: Next BRANCH verLabel  " + verLabel, null, null);
                if (!docInWf.isCheckedOut())
                    docInWf.checkout();
                _newObjId = docInWf.checkin(false, verLabel);
                docInWf = (IDfDocument) _session.getObject(_newObjId);
                docInWf.mark("CURRENT");
                docInWf.mark(docInWf.getCurrentStateName());                
                docInWf.save();
            } else if (VersionToNext.equalsIgnoreCase("SAME")) {
                verLabel = verPolicy.getSameLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.versionToNext: Next SAME verLabel  " + verLabel, null, null);
                _newObjId = docInWf.getObjectId();
                docInWf = (IDfDocument) _session.getObject(_newObjId);
                // user should be SUPERUSER in order to play with r_immutable_flag
                docInWf.setString("r_immutable_flag", "FALSE");
                docInWf.save();
            }
        } catch (DfException e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.versionToNext: Exception occurred : " + e, null, null);
        } finally {
            releaseSysSession();
        }
    }


    private void changeLC(IDfId docObjId, String LCName, String state, boolean immutable) throws DfException {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : attach and detach new lifecycle...", null, null);
        try {
            setSysSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : docObjId: " + docObjId, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : LCName: " + LCName, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : state: " + state, null, null);

            IDfDocument docForLC = (IDfDocument) _session.getObject(docObjId);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : (B)DOCUMENT VERSION SUMMARY "+ docForLC.getVersionPolicy().getVersionSummary(","), null, null);
            // user should be SUPERUSER in order to play with r_immutable_flag
            if (immutable) {
                docForLC.setString("r_immutable_flag", "FALSE");
                docForLC.save();
            }
            docForLC.detachPolicy();
            IDfSysObject lifecycle = (IDfSysObject) origSession
                    .getObjectByQualification("dm_sysobject where object_name ='" + LCName + "'");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : lifecycle: " + lifecycle, null, null);

            docForLC.attachPolicy(lifecycle.getObjectId(), state, "");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : ApplyLifecycle done!!", null, null);
            docForLC.save();

            // user should be SUPERUSER in order to play with r_immutable_flag
            if (immutable) {
                docForLC.setString("r_immutable_flag", "TRUE");
                docForLC.save();
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.changeLC : (A) DOCUMENT VERSION SUMMARY "+ docForLC.getVersionPolicy().getVersionSummary(","), null, null);

        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.changeLC : Exception occurred :" + e, null, null);
        } finally {
            releaseSysSession();
        }

        attachLabelAfterLC(docObjId, state, immutable);

    }


    public boolean makeVTObsolete() throws DfException {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : executePromoteOnVersionTree... ", null, null);
        try{
        docInWf = (IDfDocument) origSession.getObject(_newObjId);
        if (tskInfo.getAction().equalsIgnoreCase("Promote"))
        {
            IDfSessionManager sMgr = _session.getSessionManager();
            boolean bNewtran = false;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:makeVTObsolete: begin transactionalized promote" , null, null);
            try {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:makeVTObsolete: check if transaction already active" , null, null);
                if (!sMgr.isTransactionActive())
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:makeVTObsolete: -->TRANSACTION BEGIN<--",null,null);
                    bNewtran = true;
                    sMgr.beginTransaction();
                }
                
                //docInWf.promote("", true, false);
                /***********************Plugin instead of TBO ***************************/
                //Create New Version (CEM: adding transactification)
                IVersionChangePlugin verPlugin = new MrcsVersionChange();
                Map verPluginParams = new HashMap();
                verPluginParams.put("StateInfo", stInfo);
                verPluginParams.put("ApplyToObject", docObject);
                IDfId newId = verPlugin.performVersionChange(sMgr, appName, verPluginParams);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : VersionChange done!! " , null, null);
                //Promote new document
                docObject = (IDfDocument) origSession.getObject(newId);            
                IMrcsPromotePlugin pmtePlugin = new MrcsPromote();
                Map pmtePluginParams = new HashMap();
                pmtePluginParams.put("DocToPromote", docObject);
                pmtePluginParams.put("override", new Boolean (false));
                pmtePluginParams.put("testOnly", new Boolean (false));          
                pmtePlugin.mrcsPromote(pmtePluginParams);       
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : Document Promoted!! " , null, null);
                
                //Get the Updated Document Properties
                StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : config " + config, null, null);
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
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : Document Symbolic label attached!! " , null, null);
                
                //Set the ACL
                IACLPlugin aclPlugin = new MrcsACLPlugin();
                Map aclPluginParams = new HashMap();
                aclPluginParams.put("ApplyToObject", docObject);
                aclPluginParams.put("ACLName", newStInfo.getDocACL());
                aclPlugin.applyDocACL(sMgr, appName, aclPluginParams);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : Document ACL updated!! " , null, null);
                 
                //Create the Rendition
                IRenditionPlugin renditionPlugin = new MrcsCreateRendition(); 
                Map renditionPluginParams = new HashMap();
                renditionPluginParams.put("DocObject", docObject);
                renditionPlugin.createRendition(renditionPluginParams);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : Pdf Rendition of Document created!! " , null, null);
                
                //Create Copy of the Document [TBD]
                /***********************Plugin instead of TBO ***************************/
                // CEM - end post-promote plugin layer
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:makeVTObsolete: check if we need to commit (tranaction is local to this method)",null,null);
                if (bNewtran && sMgr.isTransactionActive()) {
                    // commit if the transaction is local to this method (might not be if this is part of a plugin sequence to, say, new GF)
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:makeVTObsolete: transaction is local -->TRANSACTION COMMIT<--",null,null);
                    sMgr.commitTransaction();
                }
            } catch (Exception e) {
                if (sMgr.isTransactionActive()) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:makeVTObsolete: transaction abort -->ABORT TRANSACTION ABORT<--",null,null);
                    sMgr.abortTransaction();
                }
                throw e;
            }

        }
            
        String MrcsLCStateToApply = docInWf.getCurrentStateName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : MrcsLCStateToApply " + MrcsLCStateToApply, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : DOCUMENT VERSION SUMMARY "+ docInWf.getVersionPolicy().getVersionSummary(","), null, null);

        allVTIds.add(_newObjId);
        if (makeAllVersionsObsolete) {
            IDfDocument docInVT = null;
            IDfId docVTId = null;
            try {

                String qualification = "select r_object_id from dm_sysobject (ALL) " + "where NOT  r_object_id = '"
                        + _newObjId + "' AND i_chronicle_id in (select i_chronicle_id from dm_sysobject "
                        + "where r_object_id = '" + _newObjId + "')";

                IDfQuery qry = new DfQuery();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : qualification : " + qualification, null, null);
                qry.setDQL(qualification);

                IDfCollection myObj1 = (IDfCollection) qry.execute(origSession, IDfQuery.DF_READ_QUERY);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete :  myObj1 : " + myObj1, null, null);
                while (myObj1.next()) {
                    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
                        IDfAttr attr = myObj1.getAttr(i);
                        if (attr.getDataType() == attr.DM_STRING) {
                            String id = myObj1.getString(attr.getName());
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : Object id : --->>" + id + "<<", null, null);
                            docVTId = new DfId(id);
                            allVTIds.add(docVTId);
                            docInVT = (IDfDocument) origSession.getObject(docVTId);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : Document Version Summary ---"+ docInVT.getVersionPolicy().getVersionSummary(","), null, null);
                            if ((MrcsLCToApply == null) || (MrcsLCToApply.length() <= 0))
                                MrcsLCToApply = docObject.getPolicyName();
                            changeLC(docVTId, MrcsLCToApply, MrcsLCStateToApply, true);
                            //get current state of the latest document.
                            //get currentLC of document
                            //Attach the current LC and the promote all prev versions in VT to the current state
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : (After) DOCUMENT VERSION SUMMARY "+ docInVT.getVersionPolicy().getVersionSummary(","), null, null);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.makeVTObsolete : Document In Version Tree  --->>"+ docInVT, null, null);
                        }
                    }
                }
                myObj1.close();
            } catch (Exception e) {
                /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.makeVTObsolete : Exception occurred " + e, null, null);
            }
        }
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.makeVTObsolete : Exception occurred " + e, null, null);
        }
        return true;
    }


    private void attachLabelAfterLC(IDfId docObjId, String state, boolean immutable) throws DfException {
        try {
            setSysSession();
            String label = "";
            String labeltoAttach = "";

            IDfDocument docForLabelAttach = (IDfDocument) _session.getObject(docObjId);

            // user should be SUPERUSER in order to play with r_immutable_flag
            if (immutable) {
                docForLabelAttach.setString("r_immutable_flag", "FALSE");
                docForLabelAttach.save();
            }else{
                if(state.length() == 0){
                    docInWf = (IDfDocument) _session.getObject(_newObjId);
                    state = docInWf.getCurrentStateName();
                }
            }

            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.attachLabelAfterLC : config > " + config, null, null);
            StateInfo newStInfo = config.getStateInfo(appName, state);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.attachLabelAfterLC : labeltoAttach newStInfo " + newStInfo, null, null);
            // Attach Lifecycle State label
            // boolean makeCurrent = newStInfo.getCurrentLabel();

            if (newStInfo != null)
                labeltoAttach = newStInfo.getLabel();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.attachLabelAfterLC : labeltoAttach " + labeltoAttach, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.attachLabelAfterLC : Version Summary BEFORE LabelAttach "+ docForLabelAttach.getVersionPolicy().getVersionSummary(","), null, null);
            for (int i = 0; i < docForLabelAttach.getVersionLabelCount(); i++) {
                label = docForLabelAttach.getVersionLabel(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Label " + i + " : " + label, null, null);
                if (i > 0) {
                    if (!label.equalsIgnoreCase("CURRENT"))
                        try {
                            docForLabelAttach.unmark(label);
                        } catch (DfException e1) {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.attachLabelAfterLC : Document in version tree. Label does not contain obsolete LC State Labels ", null, null);
                        }
                }else{
                    if((labeltoAttach.length() >0)&&(immutable))labeltoAttach = label+"_"+labeltoAttach;
                }
            }

            docForLabelAttach.mark(labeltoAttach);
            docForLabelAttach.save();

            // user should be SUPERUSER in order to play with r_immutable_flag
            if (immutable) {
                docForLabelAttach.setString("r_immutable_flag", "TRUE");
                docForLabelAttach.save();
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.attachLabelAfterLC : Version Summary of Document AFTER LabelAttach: " + docForLabelAttach.getVersionPolicy().getVersionSummary(","), null, null);
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.attachLabelAfterLC : Exception occurred : " + e, null, null);
        } finally {
            releaseSysSession();
        }
    }


    private void releaseSysSession() {
        try {
            _sMgr.release(_session);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.releaseSysSession : sysSession released!!" , null, null);
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.releaseSysSession : Error occurred " + e, null, null);
        }

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
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:NPPWFTask.setSysSession : Error encountered while obtaining System user info "+e, null, null);
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
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.setSysSession : NEWSESSION created!!", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.setSysSession : _sMgr : " + _sMgr, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPWFTask.setSysSession : session : " + _session, null, null);
    }

}