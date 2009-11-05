/*
 * Created on Apr 21, 2005
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

 Filename       $RCSfile: NPPPromoteToEffective.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:50 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


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
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class NPPPromoteToEffective implements MrcsWFTaskActions, MrcsLCStateActions {

    private IDfSessionManager _sMgr = null;
    private IDfId docId = null;
    private String appName = null;
    private IDfSession _session = null;
    private IDfDocument docInWf = null;
    private String MrcsLCToApply = null;
    private IDfSession origSession = null;
    private IDfDocument docObject = null;
    private StateInfo stInfo = null;

    /**
     *
     */
    public NPPPromoteToEffective() {
        super();
    }


    /*
     * (non-Javadoc)
     *
     * @see com.medtronic.documentum.mrcs.common.MrcsWFTaskActions#executeWFAction(java.util.Map)
     */
    public void executeWFAction(Map params) throws DfException {
        //Use this method only while promoting from WF.
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective.executeWFAction : Class Name" + getClass().getName(), null, null);
        executeLCAction(params);
    }


    //Use this method while performing Manual promotes
    //Session is required
    public boolean executeLCAction(Map params) throws DfException {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective.executeLCAction: Class Name" + getClass().getName(), null, null);
        origSession = (IDfSession) params.get("DFSession");
        stInfo = (StateInfo) params.get("StateInfo");
        docId = new DfId((String) params.get("DocId"));
        boolean flag = false;
        docObject = (IDfDocument) origSession.getObject(docId);
        appName = docObject.getString("mrcs_application");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective.executeLCAction: ExecutePromoteOnVersionTree : "+ stInfo.getPromoteAllPreviousVersionsToNextState(), null, null);
        if (stInfo.getPromoteAllPreviousVersionsToNextState()) {
            flag = promoteVTToNextState();
        }
        return flag;
    }


    private void changeLC(IDfId docObjId, String LCName, String state) throws DfException {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective :changeLC : attach and detach new lifecycle", null, null);
        try {
            setSysSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective :changeLC : docObjId: " + docObjId, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective :changeLC : LCName: " + LCName, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective :changeLC : state: " + state, null, null);

            IDfDocument docForLC = (IDfDocument) _session.getObject(docObjId);
            docForLC.setString("r_immutable_flag", "FALSE");
            docForLC.save();
            docForLC.detachPolicy();
            IDfSysObject lifecycle = (IDfSysObject) origSession
                    .getObjectByQualification("dm_sysobject where object_name ='" + LCName + "'");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : changeLC :  lifecycle: " + lifecycle, null, null);
            docForLC.attachPolicy(lifecycle.getObjectId(), state, "");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : changeLC : .applyLifecycle done", null, null);
            docForLC.save();
            attachLabelAfterLC(docForLC, state);
            // user should be SUPERUSER in order to play with r_immutable_flag
            docForLC.setString("r_immutable_flag", "TRUE");
            docForLC.save();
        } catch (DfException e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:NPPPromoteToEffective : changeLC :Exception Occurred: " + e, null, e);
            throw new RuntimeException("NPPPromoteToEffective error in changeLC",e);
        } finally {
            releaseSysSession();
        }
    }


    public boolean promoteVTToNextState() throws DfException {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState... ", null, null);

        String MrcsLCStateToApply = stInfo.getNextState();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState: MrcsLCStateToApply " + MrcsLCStateToApply, null, null);
        String MrcsLCToApply = docObject.getPolicyName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState:  MrcsLCToApply " + MrcsLCToApply, null, null);

        IDfDocument docInVT = null;
        IDfId docVTId = null;

        try {


            String qualification = "select r_object_id from dm_sysobject (ALL) " + "where NOT  r_object_id = '" + docId
                    + "' AND i_chronicle_id in (select i_chronicle_id from dm_sysobject " + "where r_object_id = '"
                    + docId + "')";

            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState: qualification : " + qualification, null, null);
            qry.setDQL(qualification);

            IDfCollection myObj1 = (IDfCollection) qry.execute(origSession, IDfQuery.DF_READ_QUERY);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState: myObj1 : " + myObj1, null, null);
            while (myObj1.next()) {
                for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
                    IDfAttr attr = myObj1.getAttr(i);
                    if (attr.getDataType() == attr.DM_STRING) {
                        String id = myObj1.getString(attr.getName());
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState: Object id : --->>" + id + "<<", null, null);
                        docVTId = new DfId(id);
                        docInVT = (IDfDocument) origSession.getObject(docVTId);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState: Document Version Summary : "+ docInVT.getVersionPolicy().getVersionSummary(">"), null, null);
                        changeLC(docVTId, MrcsLCToApply, MrcsLCStateToApply);
                        //get current state of the latest document.
                        //get currentLC of document
                        //Attach the current LC and the promote all prev versions in VT to the current state
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState: Document In Version Tree  --" + docInVT, null, null);
                    }
                }
            }
            myObj1.close();
            //makeLatestCurrent();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:NPPPromoteToEffective : promoteVTToNextState: Exception occurred ", null, e);
            throw new RuntimeException("NPPPromoteToEffective error in PromoteVTTtoNextState",e);
        }
        return true;
    }



    private void attachLabelAfterLC(IDfDocument docForLabelAttach, String state) throws DfException {

        try {
            //setSysSession();
            String label = "";
            String labeltoAttach = "";
            //docInWf = (IDfDocument) _session.getObject(_newObjId);

            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: state   " + state, null, null);
            StateInfo newStInfo = config.getStateInfo(appName, state);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: newStInfo   " + newStInfo, null, null);
            // Attach Lifecycle State label
            // boolean makeCurrent = newStInfo.getCurrentLabel();
            if(newStInfo != null)labeltoAttach = newStInfo.getLabel();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: labeltoAttach   " + labeltoAttach, null, null);

            for (int i = 0; i < docForLabelAttach.getVersionLabelCount(); i++) {
                label = docForLabelAttach.getVersionLabel(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: Label " + i + " : " + label, null, null);
                if (i > 0) {
                    if (!label.equalsIgnoreCase("CURRENT"))
                        try {
                            docForLabelAttach.unmark(label);
                        } catch (DfException e1) {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: Document in version tree Label does not contain obsolete LC State Labels ", null, null);
                        }
                }else{
                    if(newStInfo != null){
                        labeltoAttach = label+"_"+labeltoAttach;
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: labeltoAttach :  "+newStInfo.getLabel(), null, null);
                    }
                }
            }

            docForLabelAttach.mark(labeltoAttach);
            docForLabelAttach.save();

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: Doc Version Summary" + docForLabelAttach.getVersionPolicy().getVersionSummary(","), null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:NPPPromoteToEffective : AttachLabelAfterLC: Exception occurred: " + e, null, e);
            throw new RuntimeException("NPPPromoteToEffective error in attachLabelAfterLC",e);
        } finally {
            // releaseSysSession();
        }
    }


    private void releaseSysSession() {
        try {
            _sMgr.release(_session);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective : releaseSysSession: sysSession released!! " , null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:NPPPromoteToEffective : releaseSysSession: Error Occurred : ", null, e);
            throw new RuntimeException("NPPPromoteToEffective error in releaseSysSession",e);
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
            /*-ERROR-*/DfLogger.error(this, "MRCS:NPPPromoteToEffective.setSysSession - Error encountered while getting System user info ", null, e);
            throw new RuntimeException("NPPPromoteToEffective error in setSysSession in StateTransition config load",e);            
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
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective. setSysSession: NEWSESSION Created!! ", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective. setSysSession: _sMgr : " + _sMgr, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:NPPPromoteToEffective. setSysSession: session : " + _session, null, null);
    }
}