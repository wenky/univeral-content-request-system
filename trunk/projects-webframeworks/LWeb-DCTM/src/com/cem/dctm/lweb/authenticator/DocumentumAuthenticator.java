package com.cem.dctm.lweb.authenticator;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cem.base.EEx;
import com.cem.lweb.core.ThreadData;
import com.cem.lweb.core.interfaces.IAuthenticator;
import com.cem.lweb.core.util.Is;
import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;

public class DocumentumAuthenticator implements IAuthenticator
{
    static IDfClientX clientx = new DfClientX();

	public boolean authenticate(Map authconfig, String actionname, Map actionconfig) {
		String user = null,pass = null,base = null;
		
		// check for extant cookies
		HttpServletRequest req = ThreadData.getHttpRequest();
		Cookie[] cookielist = req.getCookies();
		if (cookielist != null) {
			for (int i=0; i < cookielist.length; i++) {
				Cookie c = cookielist[i];
				if ("zzzu".equals(c.getName())) {
					user = c.getValue();
				}
				if ("zzzp".equals(c.getName())) {
					pass = c.getValue();
				}
				if ("zzzb".equals(c.getName())) {
					base = c.getValue();
				}
			}
		}
		
		// check for "inline" creds (TODO: encryption)
		boolean setcookies = false;
		if (user == null) {
			// check for http params
			setcookies = true;
			Map params = req.getParameterMap();
			if (params.containsKey("user") && params.containsKey("pass") && params.containsKey("base")) {
				user = req.getParameter("user");
				pass = req.getParameter("pass");
				base = req.getParameter("base");
			}
		}
		
		if (user != null) {
			// authenticate vs. dctm
            // store validated credentials and session manager
			IDfSessionManager sMgr = checkUserCredentials(user,pass,base);
			if (sMgr != null) {
		    	req.setAttribute("zzzsmgr", sMgr);
	            req.setAttribute("zzzu", user);
	            req.setAttribute("zzzp", pass);
	            req.setAttribute("zzzb", base);
	            
	            if (setcookies && Is.yes(authconfig.get("usecookies"))) {
	            	// pass authenticated fields back to browser for forthcoming requests
					HttpServletResponse resp = ThreadData.getHttpResponse();
					setAuthCookies(req,resp,user,pass,base);
	            }
	            
	            return true;
			}			
		}		
		return false;
	}
	
	public static void setAuthCookies(HttpServletRequest req,HttpServletResponse resp, String user, String pass, String base) {
		// secure cookies apparently don't work/can't be seen
		// - apparently secure cookies are only sent if using https....
		Cookie c_u = new Cookie("zzzu",user);		//c_u.setSecure(true);  
		c_u.setPath(req.getContextPath());
		c_u.setMaxAge(-1);
		resp.addCookie(c_u);
		Cookie c_p = new Cookie("zzzp",pass);
		c_p.setPath(req.getContextPath());
		c_p.setMaxAge(-1);
		resp.addCookie(c_p);
		Cookie c_b = new Cookie("zzzb",user);
		c_b.setPath(req.getContextPath());
		c_b.setMaxAge(-1);
		resp.addCookie(c_b);		            			
	}

	public static IDfSessionManager checkUserCredentials(String user, String pass, String base) {
		try { 
		    IDfClient client = null;
		    try { 
		    	client = clientx.getLocalClient();
		    } catch (DfException dfe) {
		    	throw EEx.create("DCTM-AUTH-0a", "Unable to get a DCTM client", dfe);
		    }
		    IDfSessionManager sMgr = client.newSessionManager();
		    IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		    loginInfoObj.setUser(user);
		    loginInfoObj.setPassword(pass);
		    loginInfoObj.setDomain(null);
		    sMgr.setIdentity(base, loginInfoObj);
		    IDfSession session = null;
		    try {
		    	session = sMgr.getSession(base);
	            String strMessage = session.getMessage(3);
	            if(strMessage != null && strMessage.indexOf("[DM_SESSION_E_PASSWORD_EXPIRED]") != -1) {
	            	// password has expired
	            }	            
	            return sMgr;
		    } catch (DfException dfe) {
		    	return null;
		    } finally {
		    	try {sMgr.release(session);}catch(Exception e){}
		    }
		} catch (DfException dfe) {
			throw EEx.create("DCTM-AUTH-0b", "DFException while attempting authentication");
		}
		
	}
}
