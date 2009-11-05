package com.cem.contextmap.web;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletContext;

import com.cem.contextmap.IContextItem;


public class ServletConfigAttributes implements IContextItem 
{
    ServletContext c = null;
    
    public ServletConfigAttributes(ServletContext srvctx) {
        c = srvctx;
    }

    public Object get(Object key) {
        return c.getInitParameter(key.toString());
    }

    public boolean isEnumerable() {
        
        return true;
    }
    
    public Enumeration enumerate() {
        return c.getInitParameterNames();
    }

    public boolean isIterable() {
        // TODO Auto-generated method stub
        return false;
    }

    public Iterator iterate() {
        return null;
    }

    public Object set(Object key, Object value) {
        return null;
    }

}
