package com.liteserv.plugins.authenticators;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.liteserv.plugindefs.ILSAuthenticator;

public class PublicAccess implements ILSAuthenticator {

    public boolean authenticate(HttpServletRequest req, Map config, Map context) 
    {
        // no restriction
        return true;
    }

}
