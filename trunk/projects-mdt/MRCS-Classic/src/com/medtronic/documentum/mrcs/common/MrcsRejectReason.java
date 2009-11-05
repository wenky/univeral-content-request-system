/*
 * Created on Mar 28, 2005
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

 Filename       $RCSfile: MrcsRejectReason.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:50 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsRejectReason {

    private IDfId DocChronicleId;
    private String DocVersion;
    private String RejectionComment;
    private IDfTime RejectedTime;
    private boolean isRejectedDocActive;
    private IDfId DocObjectId;
    
    /**
     * 
     */
    public MrcsRejectReason() {
        super();
        // TODO Auto-generated constructor stub
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
     * @return Returns the docVersion.
     */
    public String getDocVersion() {
        return DocVersion;
    }
    /**
     * @param docVersion The docVersion to set.
     */
    public void setDocVersion(String docVersion) {
        DocVersion = docVersion;
    }
    /**
     * @return Returns the isRejectedDocActive.
     */
    public boolean isRejectedDocActive() {
        return isRejectedDocActive;
    }
    /**
     * @param isRejectedDocActive The isRejectedDocActive to set.
     */
    public void setRejectedDocActive(boolean isRejectedDocActive) {
        this.isRejectedDocActive = isRejectedDocActive;
    }
    /**
     * @return Returns the rejectedTime.
     */
    public IDfTime getRejectedTime() {
        return RejectedTime;
    }
    /**
     * @param rejectedTime The rejectedTime to set.
     */
    public void setRejectedTime(IDfTime rejectedTime) {
        RejectedTime = rejectedTime;
    }
    /**
     * @return Returns the rejectionComment.
     */
    public String getRejectionComment() {
        return RejectionComment;
    }
    /**
     * @param rejectionComment The rejectionComment to set.
     */
    public void setRejectionComment(String rejectionComment) {
        RejectionComment = rejectionComment;
    }
}
