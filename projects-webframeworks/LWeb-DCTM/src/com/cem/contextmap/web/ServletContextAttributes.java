package com.cem.contextmap.web;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletContext;

import com.cem.contextmap.IContextItem;


public class ServletContextAttributes  implements IContextItem 
{
    ServletContext c = null;
    
    public ServletContextAttributes(ServletContext servctx) 
    {
        c = servctx;
    }


    public Object get(Object key) {
        return c.getAttribute(key.toString());
    }

    public boolean isEnumerable() {
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
        Object origvalue = c.getAttribute(key.toString());
        c.setAttribute(key.toString(), value);
        return origvalue;
    }

}
