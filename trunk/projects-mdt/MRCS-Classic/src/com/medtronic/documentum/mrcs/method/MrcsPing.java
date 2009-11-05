package com.medtronic.documentum.mrcs.method;

import java.io.OutputStream;
import java.util.Map;

import com.documentum.fc.common.DfLogger;
import com.documentum.mthdservlet.IDmMethod;

public class MrcsPing implements IDmMethod {

	// implement the default configurable method - execute listed actions in workflow task
	public void execute(Map parameters, OutputStream outputstream) throws Exception
	{
       	/*-INFO-*/DfLogger.info(this, "MrcsPing - ping called" , null, null);
	}
}
