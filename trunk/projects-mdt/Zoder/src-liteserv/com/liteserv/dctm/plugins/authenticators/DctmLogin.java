package com.liteserv.dctm.plugins.authenticators;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.DfServiceException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.IDfLoginInfo;
import com.liteserv.core.LSRequestContext;
import com.liteserv.dctm.LSDctmUtils;
import com.liteserv.plugindefs.ILSAuthenticator;

public class DctmLogin implements ILSAuthenticator 
{

    // this authenticator validates the passed in parameters AND SETS THE COOKIES if successful. 
    
    public boolean authenticate(HttpServletRequest req, Map config, Map context)
    {        
        IDfClient client = LSDctmUtils.getClient();
        IDfClientX clientx = LSDctmUtils.getClientX();
        
        String user = req.getParameter("user");
        String pass = req.getParameter("pass");
        String base = req.getParameter("base");
        
        try { 
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser(user);
            loginInfoObj.setPassword(pass);
            loginInfoObj.setDomain(null);
            sMgr.setIdentity(base, loginInfoObj);
            sMgr.authenticate(base);
            // if success, add credentials to request scope
            req.setAttribute("DCTM.SessionManager", sMgr);
            req.setAttribute("DCTM.Docbase",base);            
        } catch (DfAuthenticationException badauth) {
            return false; 
        } catch (DfServiceException dse) {
            req.setAttribute("DCTM.AuthError", dse);
            return false; 
        }
        
        // it's okay! set the cookies!
        HttpServletResponse httpresponse = LSRequestContext.getHttpResponse();
        Cookie ucook = new Cookie("dctm.u",user);
        Cookie pcook = new Cookie("dctm.p",pass);
        Cookie bcook = new Cookie("dctm.b",base);
        httpresponse.addCookie(ucook);
        httpresponse.addCookie(pcook);
        httpresponse.addCookie(bcook);
        
        return true;
        
    }

}
