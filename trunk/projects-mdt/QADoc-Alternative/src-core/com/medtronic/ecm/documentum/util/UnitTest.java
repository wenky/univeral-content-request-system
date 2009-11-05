package com.medtronic.ecm.documentum.util;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.IDfLoginInfo;

public class UnitTest {
    
    public static IDfSessionManager getSessionManager(String u, String p, String d) throws Exception
    {
        IDfClientX clientx = new DfClientX();
        IDfClient client = clientx.getLocalClient();
        IDfSessionManager sMgr = client.newSessionManager();
        IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser(u);
        loginInfoObj.setPassword(p);
        loginInfoObj.setDomain(null);
        sMgr.setIdentity(d, loginInfoObj);
        return sMgr;        
    }


}
