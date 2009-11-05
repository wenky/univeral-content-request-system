package com.liteserv.dctm.plugins.authenticators;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.documentum.fc.client.IDfSessionManager;
import com.liteserv.dctm.LSDctmUtils;
import com.liteserv.plugindefs.ILSAuthenticator;

public class DocumentumAuthenticator implements ILSAuthenticator 
{

    public boolean authenticate(HttpServletRequest req, Map config, Map context) 
    {
        // get cookies for user, pass, base
        Cookie[] cookies = req.getCookies();
        
        String user = null, pass = null, base = null;
        for (int i = 0; i < cookies.length; i++)
        {
            if ("dctm.u".equals(cookies[i].getName())) {
                user = cookies[i].getValue();
            }
            if ("dctm.p".equals(cookies[i].getName())) {
                pass = cookies[i].getValue();
            }
            if ("dctm.d".equals(cookies[i].getName())) {
                base = cookies[i].getValue();
            }
        }
        
        LSDctmUtils dctmutils = new LSDctmUtils();
        IDfSessionManager sMgr = dctmutils.createSessionManager(user, pass, base); 
                
        // if failure, return false
        if (sMgr == null) {
            return false;
        }
        
        // if success, add credentials to request scope
        req.setAttribute("DCTM.SessionManager", sMgr);
        req.setAttribute("DCTM.Docbase",base);
        return true;
        
    }

}
