package com.liteserv.plugins.actionhandlers;

import javax.servlet.http.HttpServletRequest;

import com.liteserv.config.LSPlugin;
import com.liteserv.core.LSActionContext;
import com.liteserv.plugindefs.ILSProgit;

public class Redirect implements ILSProgit
{

    public String execute(final LSPlugin progitdef, HttpServletRequest req, LSActionContext context, String response) 
    {
        context.setRedirect((String)context.get("Home"));
        return response;
    }


}
