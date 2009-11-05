package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;

public class PutConversionUserInACLs 
{
	
	public static void main(String[] args)
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		try {
			
			// get list of acls that match mrcs%
			List acllist = DqlQueryIdentifier.executeNameIdQuery(session,"select r_object_id,object_name from dm_acl where object_name like 'mrcs%'");
			//BaseClass.writeDocumentList(BaseClass.conversiondirectory+"acllist.xml",acllist);
			
			putUserInACLs(session,session.getLoginUserName(),acllist);

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"putuserinacllist-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}
	
	public static void putUserInACLs(IDfSession session, String user, List acllist) throws Exception
	{
		// get sysdomain (we assume all the acls are in the sysdomain)
		String systemdomain = session.getServerConfig().getString("operator_name");
		// iterate through acllist and put user in the acl at DELETE permit
		for (int i=0; i< acllist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)acllist.get(i);
			IDfACL acl = session.getACL(systemdomain,dor.name);
			acl.grant(session.getLoginUserName(),IDfACL.DF_PERMIT_DELETE,null);
			acl.save();
			System.out.print("put user into acl "+acl.getObjectName());
		}
	}
	
	public static void removeUserInACLs(IDfSession session, String user, List acllist) throws Exception
	{
		// get sysdomain (we assume all the acls are in the sysdomain)
		String systemdomain = session.getServerConfig().getString("operator_name");
		// iterate through acllist and put user in the acl at DELETE permit
		for (int i=0; i< acllist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)acllist.get(i);
			IDfACL acl = session.getACL(systemdomain,dor.name);
			acl.revoke(session.getLoginUserName(),null);
			acl.save();
		}
		
		
	}


}
