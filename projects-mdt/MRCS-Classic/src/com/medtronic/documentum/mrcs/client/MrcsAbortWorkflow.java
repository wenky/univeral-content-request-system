/*
 * Created on Apr 6, 2005
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

 Filename       $RCSfile: MrcsAbortWorkflow.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2007/11/15 21:42:44 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.webcomponent.library.workflow.abort.AbortWorkflow;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class MrcsAbortWorkflow extends AbortWorkflow {

    /**
     *
     */
    public MrcsAbortWorkflow() {
        super();
        // TODO Auto-generated constructor stub
    }


    public void onInit(ArgumentList argumentlist) {
        super.onInit(argumentlist);
        try {
            String s = argumentlist.get("objectId");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : onInit : WorkflowId " + s, null,null);
        } catch (Exception dfexception) {
            /*-ERROR-*/DfLogger.error(this, "MrcsAbortWorkflow : onInit : Exception Occurred ", null,dfexception);
            throw new RuntimeException("MrcsAbortWorkflow initialization error - could not get objectid",dfexception);
        }
    }




    private  IDfId getDocID (String wfId) throws DfException {

        IDfId docId = null;
        try{
            String qualification = "select r_component_id from dmi_package where r_workflow_id = '" + wfId +"'";

            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : getDocID: qualification : "+qualification, null,null);
            qry.setDQL(qualification);

    		IDfCollection myObj1 = (IDfCollection)qry.execute(getDfSession(),IDfQuery.DF_READ_QUERY);
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : getDocID: myObj1 : "+myObj1, null,null);
			while(myObj1.next()) {
			    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
					IDfAttr attr = myObj1.getAttr(i);
					    if (attr.getName().equalsIgnoreCase("r_component_id")) {
                            String id = myObj1.getString(attr.getName());
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : getDocID:  id : --->>" + id + "<<", null,null);
                            docId = new DfId(id);
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : getDocID: docID --->>" + docId, null,null);
                        }
			    }
			}
			myObj1.close();

        }catch(Exception e){
            /*-ERROR-*/DfLogger.error(this, "Mrcs:MrcsAbortWorkflow : getDocID: Exception Occurred ", null,e);
            throw new RuntimeException("MrcsAbortWorkflow error - DQL Query could not locate attached document",e);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : getDocID: (Before return)docID : " + docId , null,null);
        return docId;
    }



    public boolean onCommitChanges() {
        boolean commitAbort = true;
        try {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Roll Back the changes", null,null);
                IDfSession idfsession = getDfSession();
                IDfWorkflow idfworkflow = (IDfWorkflow) idfsession.getObject(new DfId(super.m_strWorkflowId));
                // IDfProcess idfprocess =
                // (IDfProcess)idfsession.getObject(idfworkflow.getProcessId());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges : WorkflowId " + super.m_strWorkflowId, null,null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges : ProcessId " + idfworkflow.getProcessId(), null,null);


                IDfId docID = getDocID (super.m_strWorkflowId);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges : about to perform MRCS rollback of docID " + docID, null,null);
                try{
                    IDfDocument doc = (IDfDocument) getDfSession().getObject(docID);
                    MrcsWFRollback rlbck = new MrcsWFRollback(doc, getDfSession());
                    commitAbort = rlbck.rollback();
                }catch (DfException excep1) {
                    /*-ERROR-*/DfLogger.error(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges :  Error occurred Cannot fetch attachment  ", null,excep1);
                    throw new RuntimeException("MrcsAbortWorkflow error - exception in attempted document rollback for doc "+docID,excep1);
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges : MRCS rollback of performed", null,null);

                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges : invoking super.onCommitChanges which will call WorkflowService.abort", null,null);
                if (commitAbort) {
                    commitAbort = super.onCommitChanges();
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges : returned from super.onCommitChanges", null,null);

                //check for additional cleanup
                double numWF = 0;
                String qualification = "select count(r_workflow_id) from dmi_package " +
                        "where any r_component_id in " +
                        "(select r_object_id from dm_sysobject where i_chronicle_id in " +
                        "(select i_chronicle_id from dm_sysobject where r_object_id = '" +docID +"'))";

                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsAbortWorkflow.onCommitChanges: leftover WF check qualification : "+qualification, null,null);

                IDfQuery qry = new DfQuery();
                qry.setDQL(qualification);
                IDfCollection myObj1 = (IDfCollection)qry.execute(getDfSession(),IDfQuery.DF_READ_QUERY);

                while(myObj1.next()) {
                    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
                        IDfAttr attr = myObj1.getAttr(i);
                        if (attr.getDataType() == IDfAttr.DM_DOUBLE) {
                            numWF = myObj1.getDouble(attr.getName());
                            /*-DEBUG-*/if(DfLogger.isDebugEnabled(this))DfLogger.debug(this,"Mrcs:MrcsAbortWorkflow.onCommitChanges: No of WF involved :" +numWF, null,null);
                        }
                    }
                }
                myObj1.close();

                /*
                 * StartWorkflowState m_AbortWfState = new StartWorkflowState(getDfSession(),
                 * idfworkflow.getProcessId().getId()); java.util.List docList =
                 * m_AbortWfState.getAttachments(); System.out.println( "MrcsStartWorkflowContainer :
                 * onCommitChanges - docList : " + docList); IDfDocument doc =
                 * (IDfDocument)docList.get(0); System.out.println("MrcsStartWorkflowContainer :
                 * onCommitChanges - doc : " + doc); //appName =
                 * ((IMrcsDocumentTBO)doc).getAppName();
                 *
                 */
        } catch (DfException excep) {
            /*-ERROR-*/DfLogger.error(this, "Mrcs:MrcsAbortWorkflow : onCommitChanges :  Exception Occurred ", null,excep);
            throw new RuntimeException("MrcsAbortWorkflow error - error in performing workflow abort",excep);
        }
        return commitAbort;
    }

}