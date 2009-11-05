/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.2
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: MrcsMinorVersion.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/09/21 19:23:21 $

***********************************************************************
*/
package com.medtronic.documentum.mrcs.module.lifecycle;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.lifecycle.IDfLifecycleUserEntryCriteria;
import com.documentum.fc.common.IDfId;

public class MrcsMinorVersion implements IDfLifecycleUserEntryCriteria {

	public boolean userEntryCriteria(IDfSysObject obj, String username, String targetState)
			throws DfException {
        IDfVersionPolicy verPolicy = obj.getVersionPolicy();
		if (!obj.isCheckedOut()) obj.checkout();
		IDfId id = obj.checkin(false,verPolicy.getNextMinorLabel());
		return id.isNull();
	}

}
