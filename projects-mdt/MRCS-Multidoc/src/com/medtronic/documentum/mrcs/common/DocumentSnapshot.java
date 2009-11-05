/*
 * Created on May 3, 2005
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

 Filename       $RCSfile: DocumentSnapshot.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:49 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DocumentSnapshot {

    private IDfId DSObjectId;
    private IDfId DocChronicleId;
    private String DocVersionSummary ;
    private IDfId DocObjectId ;
    private IDfId DocAclId ;
    private IDfId DocPolicyId ;
    private String DocState ;
    private IDfTime DocCreationDate ;
    
    /**
     * 
     */
    public DocumentSnapshot() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @return Returns the docAclId.
     */
    public IDfId getDocAclId() {
        return DocAclId;
    }
    /**
     * @param docAclId The docAclId to set.
     */
    public void setDocAclId(IDfId docAclId) {
        DocAclId = docAclId;
    }
    /**
     * @return Returns the docChronicleId.
     */
    public IDfId getDocChronicleId() {
        return DocChronicleId;
    }
    /**
     * @param docChronicleId The docChronicleId to set.
     */
    public void setDocChronicleId(IDfId docChronicleId) {
        DocChronicleId = docChronicleId;
    }
    /**
     * @return Returns the docCreationDate.
     */
    public IDfTime getDocCreationDate() {
        return DocCreationDate;
    }
    /**
     * @param docCreationDate The docCreationDate to set.
     */
    public void setDocCreationDate(IDfTime docCreationDate) {
        DocCreationDate = docCreationDate;
    }
    /**
     * @return Returns the docObjectId.
     */
    public IDfId getDocObjectId() {
        return DocObjectId;
    }
    /**
     * @param docObjectId The docObjectId to set.
     */
    public void setDocObjectId(IDfId docObjectId) {
        DocObjectId = docObjectId;
    }
    /**
     * @return Returns the docVersionSummary.
     */
    public String getDocVersionSummary() {
        return DocVersionSummary;
    }
    /**
     * @param docVersionSummary The docVersionSummary to set.
     */
    public void setDocVersionSummary(String docVersionSummary) {
        DocVersionSummary = docVersionSummary;
    }
    /**
     * @return Returns the dSObjectId.
     */
    public IDfId getDSObjectId() {
        return DSObjectId;
    }
    /**
     * @param objectId The dSObjectId to set.
     */
    public void setDSObjectId(IDfId objectId) {
        DSObjectId = objectId;
    }
    /**
     * @return Returns the docPolicyId.
     */
    public IDfId getDocPolicyId() {
        return DocPolicyId;
    }
    /**
     * @param docPolicyId The docPolicyId to set.
     */
    public void setDocPolicyId(IDfId docPolicyId) {
        DocPolicyId = docPolicyId;
    }
    /**
     * @return Returns the docState.
     */
    public String getDocState() {
        return DocState;
    }
    /**
     * @param docState The docState to set.
     */
    public void setDocState(String docState) {
        DocState = docState;
    }
}
