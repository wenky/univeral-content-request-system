package com.medtronic.documentum.ajax;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.ucf.server.transport.IServerSession;

public class ImportServicePlugin implements GatewayPlugin 
{
	public Map execute(IDfSession session, Map parameters) throws Exception
	{
		// import service expects as inputs:
		// -- user/pass/base
		// -- list of files to import (filename, docid, local path, anything else?)
		
		// I don't think we need a jobwrapper...
		
		// create a package processor
        IServerSession ucfSession = getUcfSession();
        if(ucfSession == null)
            throw new Exception("Failed to obtain UCF session");
        com.documentum.operations.IDfOperation dfOp = sop.getDfOperation();
        IDfContentPackage pkg = getPackage(); 
        if(dfOp != null && pkg != null)
        {
            IPackageProcessorFactory pf = ucfSession.getPackageProcessorFactory();
            if(dfOp instanceof IDfImportOperation)
                return pf.newImportPackageProcessor((IDfImportOperation)dfOp, (IDfImportPackage)pkg);
        }
        return null;
		
	}
	

}
