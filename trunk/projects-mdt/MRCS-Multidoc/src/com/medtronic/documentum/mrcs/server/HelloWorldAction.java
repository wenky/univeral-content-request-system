package com.medtronic.documentum.mrcs.server;

import com.documentum.fc.client.IDfModule;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.lifecycle.IDfLifecycleUserAction;

public class HelloWorldAction implements IDfLifecycleUserAction, IDfModule {

	public void userAction(IDfSysObject doc, String username, String targetstate) throws DfException 
	{
		// 1. Do-nothing
		int i=0;
		i++;
		//2. log something
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"useraction - Loading StateTransitionConfig",null,null);
		// 2. set hello world
		//doc.setString("subject","Hello World Action");
        

	}
	
	public String test() { return "itworked"; }

}
