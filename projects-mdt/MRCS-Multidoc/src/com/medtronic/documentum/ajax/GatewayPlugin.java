package com.medtronic.documentum.ajax;

import java.util.Map;

import com.documentum.fc.client.IDfSession;

public interface GatewayPlugin 
{
	public Map execute(IDfSession session, Map parameters) throws Exception;	

}
