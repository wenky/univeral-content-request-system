/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   LocalSession.java

package com.medtronic.documentum.ajax;

import java.applet.Applet;
import java.util.HashMap;
import java.util.Map;

public class LocalSession extends Applet
{

    public LocalSession()
    {
    }

    public Map getGlobalSession()
    {
        return localsession;
    }

    public String getGlobalKey(String key)
    {
        return (String)localsession.get(key);
    }

    public void setGlobalKey(String key, String value)
    {
        localsession.put(key, value);
    }

    public Map getSession(String server)
    {
        return (Map)localsession.get(server);
    }

    public String getKey(String server, String key)
    {
        Map session = (Map)localsession.get(server);
        return (String)session.get(key);
    }

    public void setKey(String server, String key, String value)
    {
        if(!localsession.containsKey(server))
            localsession.put(server, new HashMap());
        Map session = (Map)localsession.get(server);
        session.put(key, value);
    }

    public void init()
    {
    }

    public void start()
    {
    }

    public static final long serialVersionUID = 1L;
    static Map localsession = new HashMap();

}
