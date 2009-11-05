package com.zoder.access;

import lbase.Lg;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfIdentityException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;

public class DctmAccess 
{
    static IDfClientX clientx = new DfClientX();
    public String username, password, docbase;
    IDfSessionManager sessionmanager = null;    
    
    public DctmAccess(String user,String pass,String base) {
        username = user; password = pass; docbase = base;
    }
    
    public IDfSession getSession()
    {
        if (sessionmanager == null) {
            /*-trc-*/Lg.trc("smgr lazy init");
            sessionmanager = createSessionManager(username,password,docbase);
        }
        
        IDfSession session = null;
        try {
            session = sessionmanager.getSession(docbase);
            /*-trc-*/Lg.trc("getsession acquired sessionid: %s",(session == null ? "-1" : session.getSessionId()));            
        } catch (DfIdentityException dfie) {
            try { 
                /*-trc-*/Lg.trc("logininfo TIMEOUT, redo smgr this...");
                sessionmanager = createSessionManager(username,password,docbase);
                session = sessionmanager.getSession(docbase);
            } catch (Exception e) {
                // give up
                /*-ERROR-*/Lg.err("giving up gettting session after DfIdEx",e);
                throw new RuntimeException("Unable to get session",e);
            }
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("giving up acquiring session",e);
            throw new RuntimeException("Unable to get session",e);            
        }
        return session;
    }
    
    public void releaseSession(IDfSession session)
    {
        sessionmanager.release(session);
    }
    
    public IDfSessionManager getCurrentSessionManager()
    {
        return sessionmanager;
    }

    public static IDfLoginInfo getLoginInfo(String user, String pass, String base)
    {
        IDfLoginInfo loginInfo = clientx.getLoginInfo();
        loginInfo.setUser(user);
        loginInfo.setPassword(pass);
        loginInfo.setDomain(null);
        return loginInfo;
    }
    
    public static IDfSessionManager createSessionManager(String user, String pass, String docbase)
    {
        IDfSessionManager sMgr = null;
        
        try {
            /*-trc-*/Lg.trc("get localclient");
            IDfClient client = clientx.getLocalClient();
            
            /*-trc-*/Lg.trc("create smgr");
            sMgr = client.newSessionManager();
            /*-trc-*/Lg.trc("getlogininfo");
            IDfLoginInfo loginInfo = getLoginInfo(user,pass,docbase);
            /*-trc-*/Lg.trc("set identity");
            sMgr.setIdentity(docbase, loginInfo);
            /*-trc-*/Lg.trc("done");
        } catch (DfException dfe){
            /*-ERROR-*/Lg.err("EXCEPTION",dfe);
            throw new RuntimeException(dfe);
        }
        return sMgr;
        
    }

}
