package com.cem.contextmap.web;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.cem.contextmap.IContextItem;

public class HttpRequestAttributes implements IContextItem 
{
    HttpServletRequest req = null;
    
    public HttpRequestAttributes(HttpServletRequest request) 
    {
        req = request;        
    }
	
	public Enumeration enumerate() 
	{
		return req.getAttributeNames();
	}

	public boolean isEnumerable() {
		if (req == null) return false;
		else return true;
	}

	public boolean isIterable() {
		return false;
	}

	public Iterator iterate() {
		return null;
	}

	public Object get(Object key) {
		if (req == null) return null;
		return req.getAttribute(key.toString());
	}

	public Object set(Object key, Object value) {
		if (req == null) return null;
		Object o = req.getAttribute(key.toString());
		req.setAttribute(key.toString(), value);
		return o;
	}

}
