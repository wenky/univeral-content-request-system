package com.cem.contextmap.web;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.cem.contextmap.IContextItem;

public class MapWrapper implements IContextItem 
{
    Map map = null;
    
    public MapWrapper(Map maparg) 
    {
        map = maparg;        
    }
	
	public Enumeration enumerate() 
	{
		return null;
	}

	public boolean isEnumerable() {
		return false;
	}

	public boolean isIterable() {
		if (map == null) return false;
		return true;
	}

	public Iterator iterate() {
		if (map == null) return null;
		return map.keySet().iterator();
	}

	public Object get(Object key) {
		if (map == null) return null;
		return map.get(key);
	}

	public Object set(Object key, Object value) {
		if (map == null) return null;
		Object o = map.put(key,value);
		return o;
	}

}
