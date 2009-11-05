package com.medtronic.documentum.mrcs.server.interfaces;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;

public interface IMrcsLifecyclePlugin 
{
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context);
}
