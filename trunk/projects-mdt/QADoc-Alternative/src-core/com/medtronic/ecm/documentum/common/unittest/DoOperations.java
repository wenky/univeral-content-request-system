package com.medtronic.ecm.documentum.common.unittest;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;

public class DoOperations {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
        IDfClientX clientx = new DfClientX();
       	IDfClient client = clientx.getLocalClient();
       	IDfSessionManager sMgr = client.newSessionManager();
       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser("ecsadmin");
        loginInfoObj.setPassword("spring2005");
        loginInfoObj.setDomain(null);
        sMgr.setIdentity("sandbox6", loginInfoObj);        	    
        IDfSession sess = sMgr.getSession("sandbox6");
        
        IDfSysObject obj = (IDfSysObject)sess.getObject(new DfId("0b1e666380005d87"));
        obj.destroy();
        
        sMgr.release(sess);
		

	}

}
