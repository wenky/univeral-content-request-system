package com.medtronic.ecm.documentum.common.unittest;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.Lg;

public class TestMdtLog 
{
	public static void main(String[] args)
	{
		TestMdtLog tml = new TestMdtLog();
		
		/*-FATAL-*/Lg.ftl("test (static) a simple msg");
		/*-FATAL-*/Lg.ftl("test (static) a simple substitution: %s","a-string");
		Exception e = new Exception("BigBadError");
		/*-FATAL-*/Lg.ftl("test (static) sub+stacktrace: %d",4,e);

    	IDfSysObject s = null;
    	try { 
        IDfClientX clientx = new DfClientX();
       	IDfClient client = clientx.getLocalClient();
       	IDfSessionManager sMgr = client.newSessionManager();
       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser("ecsadmin");
        loginInfoObj.setPassword("spring2005");
        loginInfoObj.setDomain(null);
        sMgr.setIdentity("sandbox6", loginInfoObj);        	    
        IDfSession sess = sMgr.getSession("sandbox6");
        
        
        s = (IDfSysObject)sess.getObject(new DfId("091e666380004c80"));
        
        s.attachPolicy(new DfId("461e666380004c79"), "In-Progress", "alpha1alias");
        
        s.save();
        
        sMgr.release(sess);
    	} catch (Exception ez) {
    		int i = 1;
    		i++;
    		
    	}
    	

		
	}
	
	void runtest()
	{
		/*-FATAL-*/Lg.ftl("test (instance) a simple substitution: %s","inst-string");
		
	}

}
