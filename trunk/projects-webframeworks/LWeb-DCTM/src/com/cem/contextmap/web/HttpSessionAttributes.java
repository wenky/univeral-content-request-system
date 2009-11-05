package com.cem.contextmap.web;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpSession;

import com.cem.contextmap.IContextItem;

public class HttpSessionAttributes implements IContextItem
{
    HttpSession c = null;
    
    public HttpSessionAttributes(HttpSession session) 
    {
        c = session;        
    }

    public Object get(Object key) {
        if (c == null) return null;
        return c.getAttribute(key.toString());
    }

    public boolean isEnumerable() {
        if (c == null) return false;
        return true;
    }

    public Enumeration enumerate() {
        return c.getAttributeNames();
    }

    public boolean isIterable() {
        return false;
    }

    public Iterator iterate() {
        return null;
    }

    public Object set(Object key, Object value) {
        if (c == null) return null;        
        Object origvalue = c.getAttribute(key.toString());
        c.setAttribute(key.toString(), value);        
        return origvalue;
    }
    

}
