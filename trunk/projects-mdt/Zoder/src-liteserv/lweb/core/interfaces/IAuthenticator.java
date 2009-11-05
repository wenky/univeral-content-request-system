package lweb.core.interfaces;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface IAuthenticator 
{
    public boolean authenticate(Map authconfig, String actionname, Map actionconfig);
}
