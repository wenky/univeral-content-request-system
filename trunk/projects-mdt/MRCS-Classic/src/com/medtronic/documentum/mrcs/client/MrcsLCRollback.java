package com.medtronic.documentum.mrcs.client;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.medtronic.documentum.mrcs.common.DocumentSnapshot;

public class MrcsLCRollback extends MrcsWFRollback 
{
    public MrcsLCRollback(IDfDocument doc, IDfSession sess) {
    	super(doc,sess);
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

            String qualification = "select * from mrcs_document_snapshot where doc_chronicle_id ='" +document.getChronicleId() +"' and transaction_type  = 'lifecycle' ORDER BY r_object_id DESC";

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
    

}
