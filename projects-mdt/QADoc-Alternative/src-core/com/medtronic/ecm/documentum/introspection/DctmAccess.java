package com.medtronic.ecm.documentum.introspection;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfIdentityException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.tools.RegistryPasswordUtils;

public class DctmAccess 
{
    static IDfClientX clientx = new DfClientX();
    
    public String username, password, docbase;
    IDfSessionManager sessionmanager = null; 
    IDfSession session = null;
    
    public DctmAccess(String user,String pass,String base) {
        username = user; password = pass; docbase = base;
    }
    
    public static List getDocbaseList() throws DfException {
        IDfDocbaseMap map = clientx.getDocbrokerClient().getDocbaseMap();
        List baselist = new ArrayList();
        for (int i =0; i < map.getDocbaseCount(); i++)
        {
            String curbase = map.getDocbaseName(i);
            baselist.add(curbase);
        }
        Collections.sort(baselist);
        return baselist;        
    }
    
    // because dfc is weird about being used in jsps from what I've seen, I'll do this:
    public void initSession()
    {
        session = getSession();
    }
    
    public IDfSession accessSession()
    {
        return session;
    }
    
    IDfSession getSession()
    {
        if (sessionmanager == null) {
            sessionmanager = createSessionManager(username,password,docbase);
        }
        
        IDfSession session = null;
        try {
            session = sessionmanager.getSession(docbase);
        } catch (DfIdentityException dfie) {
            try { 
                sessionmanager = createSessionManager(username,password,docbase);
                session = sessionmanager.getSession(docbase);
            } catch (Exception e) {
                throw new RuntimeException("Unable to get session",e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get session",e);            
        }
        return session;
    }

    public void releaseSession()
    {
        try {sessionmanager.release(session);} catch (Exception e){}
    }

    void releaseSession(IDfSession session)
    {
        try {sessionmanager.release(session);} catch (Exception e){}
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
            IDfClient client = clientx.getLocalClient();            
            sMgr = client.newSessionManager();
            IDfLoginInfo loginInfo = getLoginInfo(user,pass,docbase);
            sMgr.setIdentity(docbase, loginInfo);
        } catch (DfException dfe){
            throw new RuntimeException(dfe);
        }
        return sMgr;
        
    }
    
    public static String dfcEncrypt(String s) throws DfException
    {
        String enc = RegistryPasswordUtils.encrypt(s);
        return enc;
    }

    public static String dfcDecrypt(String s) throws DfException
    {
        String dec = RegistryPasswordUtils.decrypt(s);
        return dec;
    }
    
    public String getFormatMimeType(String format) throws Exception
    {
        IDfFormat fmtobj = session.getFormat(format);
        String mimetype = fmtobj.getMIMEType();
        return mimetype;
    }
    
    public ByteArrayInputStream getContent(String id, String format) throws Exception
    {
        IDfSysObject so = (IDfSysObject)session.getObject(new DfId(id));
        return so.getContentEx(format, 0);
    }

}

