package com.liteserv.dctm;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.documentum.common.EnrichableException;

public class LSDctmUtils 
{
    static IDfClientX clientx = new DfClientX();
    static IDfClient client;
    
    public LSDctmUtils()
    {
        synchronized (clientx) {
            try {
                // complete lazy init
                if (client == null) {
                    client = clientx.getLocalClient(); // does this "wear out" or "time out"?
                }
            } catch (DfException dfe) {
                throw EnrichableException.create("LSDctmUtils-new","Unable to create DFC local client ",dfe);
            }
        }
    }
    
    public static IDfClientX getClientX() {return clientx;}
    public static IDfClient getClient() {return client;}
    
    public static IDfSessionManager createSessionManager(String user, String pass, String docbase)
    {
        // do dctm login attempt
        IDfSessionManager sMgr = null;
        try { 
            sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser(user);
            loginInfoObj.setPassword(pass);
            loginInfoObj.setDomain(null);
            sMgr.setIdentity(docbase, loginInfoObj);
            sMgr.authenticate(docbase);            
            return sMgr;
        } catch (DfAuthenticationException badauth) {
            return null;
        } catch (DfException dfe) {
            throw EnrichableException.create("LSDctmUtils-CreateSessMgr","Error in creating sessionmanager",dfe);
        }
    }
    
}
