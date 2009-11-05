import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cem.dctm.lweb.authenticator.DocumentumAuthenticator;
import com.cem.lweb.core.ThreadData;
import com.cem.lweb.core.util.Is;
import com.documentum.fc.client.IDfSessionManager;


      HttpServletRequest req = ThreadData.getHttpRequest();
      HttpServletResponse resp = ThreadData.getHttpResponse();
      Map context = ThreadData.getActionContext();

      String user = req.getParameter("user");
      String pass = req.getParameter("pass");
      String base = req.getParameter("base");
      String passthru = req.getParameter("passthru");
      
      boolean success = false;
      if (user != null && base != null) {
        IDfSessionManager sMgr = DocumentumAuthenticator.checkUserCredentials(user, pass, base);
        if (sMgr != null) {
          req.setAttribute("loginsuccess","true");
          DocumentumAuthenticator.setAuthCookies(req, resp, user, pass, base);
        }
      } 
    
