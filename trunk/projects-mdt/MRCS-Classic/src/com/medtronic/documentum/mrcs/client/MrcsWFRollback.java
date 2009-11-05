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
 Version        4.2.2
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: MrcsWFRollback.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2007/12/20 22:43:31 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.common.DocumentSnapshot;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class MrcsWFRollback {

    IDfDocument document = null;
    IDfSession session = null;
    DocumentSnapshot snapshot = null;
    private String appName = null;
    private IDfSessionManager _sMgr = null;
    private IDfSession _session = null;

    /**
     *
     */
    public MrcsWFRollback(IDfDocument doc, IDfSession sess) {
        try {
            document = doc;
            session = sess;
            appName = document.getString("mrcs_application");
        } catch (DfException e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: Exception @ MrcsWFRollback ", null, e);
            throw new RuntimeException("MrcsWFRollback instantiation error",e);
        }
    }

    public boolean rollback() throws DfException {
        boolean rollback = false;
        try {
            //Obtain snapshot
            initSnapshot();
            //ROLLBACK

			/* MRCS 4.2.2
			 * MJH: 12/20/2007
			 * Let's try to avoid the null pointer exception of a quickflow, shall we.
			 */
            if (snapshot.getDocObjectId() != null) {
				rollbackNewVersions();
				//Obtain and make Set back the properties - State, ACl, LABEL (including CURRENT)
				rollbackToInitialState();
				//the docid is Nothing but the one in snapshot object
				//Delete the snapshot
				delSnapshot();
				rollback = true;
			} else if (snapshot.getDocObjectId() == null) {
				//it must be a quickflow, let them abort it
				rollback = true;
			}
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: Exception @ rollback ", null, e);
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: -- docid: "+document.getObjectId().getId(), null, null);
            rollback = false;
            throw new RuntimeException("MrcsWFRollback error for document ID "+document.getObjectId().getId(),e);
        }
        return rollback;
    }


    public void initSnapshot() {
        snapshot = new DocumentSnapshot();
        try {
            //The docId of "document" is the doc ID of the current document (after any post promote changes)
            //If this is the same version then there may be other post promote changes (ACL, Label,
            // Copies).Obtain
            //snapshot based on Object_id
            //String qualification = "select * from mrcs_document_snapshot where doc_object_id
            // ='" +docId+"')";
            //else If there were any post promote version changes then obtain them from snapshot
            //    based on the Chronicle ID of the Current document after version change
            //qualification = "select * from mrcs_document_snapshot where doc_chronicle_id ='"
            // +docId+"')";
            //  => So always obtain the snapshot based on chronicle ID ??

            //String qualification = "select * from mrcs_document_snapshot where doc_chronicle_id ='"+ document.getChronicleId() + "'";
            //String qualification = "select * from mrcs_document_snapshot where doc_chronicle_id ='"+ document.getChronicleId() + "' and doc_object_id='" +document.getObjectId()+"'";

            String qualification = "select * from mrcs_document_snapshot where doc_chronicle_id ='" +document.getChronicleId() +"' and transaction_type  = 'workflow' ORDER BY r_object_id DESC";

            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: initSnapshot: qualification : " + qualification, null, null);
            qry.setDQL(qualification);

            IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: initSnapshot: myObj1 : " + myObj1, null, null);
            while (myObj1.next()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: initSnapshot: `w` " , null, null);
                for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
                    IDfAttr attr = myObj1.getAttr(i);
                    if (attr.getName().equalsIgnoreCase("doc_acl_id")) {
                        snapshot.setDocAclId(myObj1.getId(attr.getName()));
                    }
                    if (attr.getName().equalsIgnoreCase("r_object_id")) {
                        snapshot.setDSObjectId(myObj1.getId(attr.getName()));
                    }
                    if (attr.getName().equalsIgnoreCase("doc_chronicle_id")) {
                        snapshot.setDocChronicleId(myObj1.getId(attr.getName()));
                    }
                    if (attr.getName().equalsIgnoreCase("doc_object_id")) {
                        snapshot.setDocObjectId(myObj1.getId(attr.getName()));
                    }
                    if (attr.getName().equalsIgnoreCase("doc_creation_date")) {
                        snapshot.setDocCreationDate(myObj1.getTime(attr.getName()));
                    }
                    if (attr.getName().equalsIgnoreCase("doc_version_summary")) {
                        snapshot.setDocVersionSummary(myObj1.getString(attr.getName()));
                    }
                    if (attr.getName().equalsIgnoreCase("doc_policy_id")) {
                        snapshot.setDocPolicyId(myObj1.getId(attr.getName()));
                    }
                    if (attr.getName().equalsIgnoreCase("doc_state")) {
                        snapshot.setDocState(myObj1.getString(attr.getName()));
                    }
                }
                break;
            }
            myObj1.close();

        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: initSnapshot: Exception occurred ", null, e);
            throw new RuntimeException("MrcsWFRollback snapshot init error",e);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: initSnapshot:  Snapshot before return:  " + snapshot, null, null);
    }


    public void rollbackToInitialState() {
        try {
            setSysSession();
            //IDfDocument doc = (IDfDocument) _session.getObject(document.getObjectId());
            String qualification = "dm_sysobject where i_chronicle_id='" +snapshot.getDocChronicleId() +"'";
            IDfId id = session.getIdByQualification( qualification );
            if( id == null ) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Document Object can not be found.", null, null);
             }
            IDfDocument doc = (IDfDocument) _session.getObject(id);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Current Document : "+doc, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Current Document Id : "+id, null, null);
            //set ACL
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  set ACL....", null, null);
            String systemdomain = _session.getServerConfig().getString("operator_name");
            IDfACL docACL = (IDfACL) _session.getObject(snapshot.getDocAclId());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Snapshot docACL " + docACL, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Snapshot docACLName " + docACL.getObjectName(), null, null);
            document.setACL(docACL);
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  ACL reverted to: " + doc.getACLName(), null, null);
            // setLabel
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  setLabel...", null, null);
            String label = "";
            String verSummary = doc.getVersionPolicy().getVersionSummary(",");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Document Version Summary before attaching label : " + verSummary, null, null);
            for (int i = 0; i < doc.getVersionLabelCount(); i++) {
                label = doc.getVersionLabel(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Label " + i + " : " + label, null, null);
                if (i > 0) {
                    if (!label.equalsIgnoreCase("CURRENT"))
                        try {
                            doc.unmark(label);
                        } catch (DfException e1) {
                            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  DfException ;Labels (Unmark) " + e1, null, null);
                        }
                }
            }
            doc.mark("CURRENT");
            doc.save();
            verSummary = doc.getVersionPolicy().getVersionSummary(",");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  AttachLabel : doc verSummary" + verSummary, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  AttachLabel : doc snapshot.getDocVersionSummary() "+ snapshot.getDocVersionSummary(), null, null);
            StringTokenizer strTokens = new StringTokenizer(snapshot.getDocVersionSummary(), ",");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  AttachLabel : doc strTokens" + strTokens, null, null);
            int i = 0;
            while (strTokens.hasMoreTokens()) {
                label = strTokens.nextToken();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  strTokens : Label  : " + label, null, null);
                if (i > 0) {
                    if (label.equalsIgnoreCase("CURRENT")) {
                        if (verSummary.indexOf("CURRENT") == -1)
                            doc.mark("CURRENT");
                    } else {
                        try {
                            doc.mark(label);
                        } catch (DfException e1) {
                            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  DfException ;Labels (mark) " + e1, null, null);
                        }
                    }
                }
                i = i + 1;
            }
            doc.save();
            verSummary = doc.getVersionPolicy().getVersionSummary(",");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Document version Summary after attaching Label " + verSummary, null, null);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Applying LC and setting State ...", null, null);
            //Apply LC and set State
            doc.detachPolicy();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  revert to lifecycle: " + snapshot.getDocPolicyId(), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  revert to lifecycle State: " + snapshot.getDocState(), null, null);
            try{
            doc.attachPolicy(snapshot.getDocPolicyId(), snapshot.getDocState(), "");
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Current doc Policy : " + doc.getPolicyName(), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState:  Current doc State : " + doc.getCurrentStateName(), null, null);
            }catch(Exception e){
                /*
                 * Note that Policy ID for the default Lifecycle is generally '0000000000000000'.
                 * This means that no lifecycle is applied to the document which should not be the case wrt MRCS.
                 * However if the workflows are started without applying any MRCS Lifecycle, then upon Abort,
                 * the lifecycle has to be reset to default which is as good as just dettaching the Policy.
                 * Also note that the attachpolicy cannot be done using rhe  default Lifecycle ID.
                 */
                /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: rollbackToInitialState: : Need to Apply Default lifecycle>> "+e.getMessage(), null, e);
                throw new RuntimeException("MrcsWFRollback rolling back to initial state failed on lifecycle defaulting",e);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackToInitialState: --- rollbackToInitialState DONE!!! ----", null, null);

        } catch (DfException excep) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: rollbackToInitialState: Exception Occurred : ", null, excep);
            throw new RuntimeException("MrcsWFRollback rollbackToInitialState had a DFC error",excep);
        } finally {
            releaseSysSession();
        }
    }


    public List getNewVersions() {
        IDfId newDocID = null;
        List newVersions = new ArrayList();
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: getNewVersions : snapshot.getDocObjectId() " + snapshot.getDocObjectId(), null, null);
            // Obtain and Destroy any new versions and its copies created - doc_chronicle_id

            String date = snapshot.getDocCreationDate().asString("mm/dd/yyyy hh:mi:ss AM/PM");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: getNewVersions : getNewVersions : snapshot.getDocCreationDate " + date, null, null);


            String qualification = "select r_object_id from dm_sysobject (ALL) " + "where i_chronicle_id='"
                    + snapshot.getDocChronicleId() + "'  " + "AND r_creation_date > DATE ('" + date
                    + "', 'mm/dd/yyyy hh:mi:ss AM/PM')";



            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: getNewVersions :  qualification : " + qualification, null, null);
            qry.setDQL(qualification);

            IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: getNewVersions : myObj1 : " + myObj1, null, null);
            while (myObj1.next()) {
                for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
                    IDfAttr attr = myObj1.getAttr(i);
                    //if (attr.getDataType() == attr.DM_STRING) {
                    if (attr.getName().equalsIgnoreCase("r_object_id")) {
                        //String id = myObj1.getId(attr.getName());
                        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "---getNewVersions id : --->>" + id + "<<");
                        newDocID = myObj1.getId(attr.getName()); //new DfId(id);
                        newVersions.add(newDocID);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: getNewVersions :  newDocID " + newDocID, null, null);
                    }
                }
            }
            myObj1.close();

        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: getNewVersions : Exception occurred ", null, e);
            throw new RuntimeException("Error in MrcsWFRollback retrieval of new versions",e);
        }
        return newVersions;
    }


    public void rollbackNewVersions() {
        try {
            List lst = getNewVersions();
            IDfId newDocId = null;
            for (int t = 0; t < lst.size(); t++) {
                newDocId = (IDfId) lst.get(t);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackNewVersions : newDocId --->>" + newDocId, null, null);
                rollbackCopies(newDocId);
                deleteNewVersion(newDocId);
            }
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: rollbackNewVersions :Exception occurred ", null, e);
            throw new RuntimeException("Error in MrcsWFRollback rollback/deletion of new versions",e);
        }
    }


    public void deleteNewVersion(IDfId newdocID) {
        // destroy the document
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: deleteNewVersion : Destroying new versions... for id "+newdocID.getId() , null, null);
            setSysSession();
            IDfDocument doc = (IDfDocument) _session.getObject(newdocID);
            doc.destroy();
        } catch (DfException excep) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: deleteNewVersion : Exception Occurred @ deleteNewVersion : ", null, excep);
            throw new RuntimeException("Error in MrcsWFRollback deletion of new version "+newdocID.getId(),excep);
        } finally {
            releaseSysSession();
        }
    }


    public void rollbackCopies(IDfId newdocID) {
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : rolling back copies for docid : >> " + newdocID.getId() + " <<", null, null);
            setSysSession();
            IDfDocument doc = (IDfDocument) _session.getObject(newdocID);

            //GEt relation,get copy
            IDfDocument childDoc = null;
            IDfCollection chldRltvs = doc.getChildRelatives("ReferToOriginalRelationShip");
            IDfId childId = null;
            IDfId relationId = null;
            IDfRelation docRel = null;

            while (chldRltvs.next()) {
                for (int i = 0; i < chldRltvs.getAttrCount(); i++) {
                    IDfAttr attr = chldRltvs.getAttr(i);
                    if (attr.getName().equalsIgnoreCase("r_object_id")) {
                        String relid = chldRltvs.getString(attr.getName());
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : relationID " + relid , null, null);
                        relationId = new DfId(relid);
                    }
                    if (attr.getName().equalsIgnoreCase("child_id")) {
                        String id = chldRltvs.getString(attr.getName());
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : chld id : --->>" + id + "<<", null, null);
                        childId = new DfId(id);
                        break;
                    }
                }
            }
            chldRltvs.close();

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : relationId : >>" + relationId + "<<", null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : childId : >>" + childId + "<<", null, null);

            // destroy copy
            if (childId != null) {
                try{
                //destroy this version of copy,
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : deleting of Child Doc ID: "+childId.getId(), null, null);
                childDoc = (IDfDocument) _session.getObject(childId);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : Version Summary of Child Doc: "+childDoc.getVersionPolicy().getVersionSummary(","), null, null);
                childDoc.destroy();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: rollbackCopies : Copy Deleted!!", null, null);
                }catch(Exception e){
                    /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: rollbackCopies : Exception occurred :Cannot Delete the Copy : " + e, null, e);
                    throw new RuntimeException("Error in MrcsWFRollback deletion of copy "+childId.getId(),e);
                }
            }
            /*//destroy the relationship object for this version of document
            if (relationId != null) {
                try{
                    //destroy this version of copy,
                docRel = (IDfRelation) _session.getObject(relationId);
                /*-DEBUG-if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " IDfRelation docRel : "+docRel, null, null);
                docRel.destroy();
                /*-DEBUG-if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "IDfRelation Deleted!!", null, null);
                }catch(Exception e){
                    /*-DEBUG-if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "---Cannot Delete the DM Relation --->>" + e);
                }
            }*/
        } catch (DfException excep) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: rollbackCopies : Exception Occurred : ", null, excep);
            throw new RuntimeException("Error in MrcsWFRollback - rolling back of copies for id "+newdocID.getId(),excep);
        } finally {
            releaseSysSession();
        }

    }


    public void delSnapshot() {
        try {
            setSysSession();
            IDfPersistentObject mrcsDocSnapshot = _session.getObject(snapshot.getDSObjectId());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: delSnapshot : mrcsDocSnapshot : " + mrcsDocSnapshot, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: delSnapshot : snapshot id : " + mrcsDocSnapshot.getObjectId().getId(), null, null);
            mrcsDocSnapshot.destroy();

        } catch (DfException excep) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: delSnapshot : Exception Occurred : ", null, excep);
            throw new RuntimeException("MrcsWFRollback snapshot cleanup failed",excep);
        } finally {
            releaseSysSession();
        }
    }


    private void releaseSysSession() {
        try {
            _sMgr.release(_session);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: releaseSysSession : syssession released!! ", null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback: Error @ ReleaseSysSession   ", null, e);
            throw new RuntimeException("MrcsWFRollback could not release sys session",e);
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
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsWFRollback:  - Error encountered while getting System user info from MRCS config", null, e);
            throw new RuntimeException("MrcsWFRollback error in retrieving MRCS config",e);
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: setSysSession : appName : " + appName, null, null);
        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = new DfLoginInfo();
        loginInfoObj.setUser(config.getSystemUsername(appName));
        loginInfoObj.setPassword(config.getSystemPassword(appName));
        loginInfoObj.setDomain(null);

        String _docBase = config.getApplicationDocbase(appName);

        //bind the Session Manager to the login info
        _sMgr.setIdentity(_docBase, loginInfoObj);

        _session = _sMgr.getSession(_docBase);
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: setSysSession : NEWSESSION created!! ", null, null);
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: setSysSession : _sMgr : " + _sMgr, null, null);
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsWFRollback: setSysSession : session : " + _session, null, null);
    }

}