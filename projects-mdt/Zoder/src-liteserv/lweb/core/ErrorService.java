package lweb.core;

import javax.servlet.http.HttpServletRequest;

import lbase.EEx;

import com.liteserv.core.LSActionContext;
import com.liteserv.core.LSRequestContext;

public class ErrorService 
{
    public static EEx packageActionError(String action,int index,ActionContext context,String errorcode, String errormessage,Object...args)
    {
        EEx lse = EEx.create(errorcode, errormessage, args);
        HttpServletRequest req = LSRequestContext.getHttpRequest();
        req.setAttribute("ActionError.Error", lse.getCause());
        req.setAttribute("ActionError.Action",action);
        req.setAttribute("ActionError.ActionContext", context);
        req.setAttribute("ActionError.ProgitIndex", index);
        return lse;
    }
    
}
