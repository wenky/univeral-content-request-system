/*
 * Created on Jan 18, 2005
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

 Filename       $RCSfile: IESignServiceSBO.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:00 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.services.workflow.inbox.ITask;
import com.medtronic.documentum.mrcs.sbo.dto.ESignDTO;


/**
 * @author prabhu1
 *
 * The ESignature SBO Contract
 */
public interface IESignServiceSBO extends IDfService {

	
	/**
	 * e-sign the Document
	 * 
	 * @param sign
	 * @param itask
	 * @param session
	 * @param taskInfo
	 * @return
	 * @throws DfException
	 */
	public boolean signDocument(IDfDocument docObject, ESignDTO sign) throws DfException;
		
}
