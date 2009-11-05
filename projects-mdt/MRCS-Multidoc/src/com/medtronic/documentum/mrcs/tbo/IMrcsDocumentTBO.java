/*
 * Created on Jan 31, 2005
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

 Filename       $RCSfile: IMrcsDocumentTBO.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:01 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.tbo;


import com.documentum.fc.client.IDfBusinessObject;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;

/**
 * @author prabhu1
 *
 * The MRCSDocument TBO
 */
public interface IMrcsDocumentTBO extends IDfBusinessObject {
	public String getAppName() throws DfException;
	public void setAppName(String appName);
	public IDfRelation getRelation();
	public void setRelation(String relation_name, IDfId child_id, IDfId parent_id, String description) throws DfException;
	//public void setRelation(IDfDocument parentDoc, String relation_name, String child_id, String parent_id, String description) throws DfException;
	public boolean isCopy();
	public void setCopy(boolean isCpy);

}
