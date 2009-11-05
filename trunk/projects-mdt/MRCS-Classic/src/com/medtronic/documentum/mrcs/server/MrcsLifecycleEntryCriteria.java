package com.medtronic.documentum.mrcs.server;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.lifecycle.IDfLifecycleUserEntryCriteria;

public class MrcsLifecycleEntryCriteria implements
		IDfLifecycleUserEntryCriteria {

	public boolean userEntryCriteria(IDfSysObject arg0, String arg1, String arg2) throws DfException 
	{
		return false;
	}

}
