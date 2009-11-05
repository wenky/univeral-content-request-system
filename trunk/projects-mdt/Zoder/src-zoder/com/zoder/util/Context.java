package com.zoder.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contexts are special structures that encapsulate several hashmaps. It is common 
 * in web frameworks that data is stored in several maps: request parameters, a page's
 * "scratchpad" map, a global configuration settings map, the http session to name
 * four common ones. 
 * 
 * A genericized hunk of code may want to access a key piece of data under a specifically
 * named key, but that key may be in parameters, http session, or in the globals 
 * depending on the situation and execution state.
 * 
 * A context is set up by providing a list of hashmaps with a predefined order, or 
 * precedence. A "get" request for a key is executed against the list of hashmaps in
 * the specified order/precedence, and the first one found in a given map has its
 * value returned.
 * 
 * Contexts typically designate a single map that new keys can be dropped into, usually
 * a page-specific map that functions as a scratchpad for the current executed action
 * or page request only. Usually that is the top-priority one. 
 * 
 * @author muellc4
 *
 */

public class Context implements Map
{

    protected List mapprecedence;
    protected List precedencenames;
    Map writablecontext = null;
    
    public List getPrecedenceList() { return mapprecedence; }
    
    public List getList(Object key) {
        List list = (List)get(key);
        if (list == null) {
            list = new ArrayList();
            writablecontext.put(key,list);            
        }
        return list;            
    }

    public Map getMap(Object key) {
        Map map = (Map)get(key);
        if (map == null) {
            map = new HashMap();
            writablecontext.put(key,map);    
        }
        return map;            
    }
    
    public Object getNoNull(Object key) {
        Object retval = get(key);
        if (retval == null) {
            NullPointerException npe = new NullPointerException("Context lookup of key "+key+" returned null");
        }
        return retval;
    }
    
    public void simpleInit(int writableindex, Object... args)
    {
        mapprecedence = new ArrayList();
        precedencenames = new ArrayList();
        for (int i=0; i < args.length; i=i+2) {
            addContextMap((Map)args[i+1],(String)args[i]);
        }
        writablecontext = (Map)args[writableindex*2+1];
    }

    public void initContext() {
        mapprecedence = new ArrayList();
        precedencenames = new ArrayList();        
        writablecontext = new HashMap();
        mapprecedence.add(writablecontext);        
    }

    public void initContext(List orderofmaps, List mapnames) {
        mapprecedence = orderofmaps;
        precedencenames = mapnames;
        writablecontext = (Map)orderofmaps.get(0);
    }

    public void initContext(List orderofmaps, List mapnames, int writableindex) {
        mapprecedence = orderofmaps;
        precedencenames = mapnames;
        writablecontext = (Map)orderofmaps.get(writableindex);
    }
    
    public void addContextMap(Map map, String mapname) {
        mapprecedence.add(map);
        precedencenames.add(mapname);
    }

    public void addContextMap(Map map, String mapname, int index) {
        mapprecedence.add(index,map);
        precedencenames.add(index,mapname);
    }

    public void setContextMap(Map map, String mapname, int index) {
        mapprecedence.set(index,map);
        precedencenames.set(index,mapname);
    }

    public void setWritableContext(String mapname) {
        writablecontext = getContextMap(mapname);
    }

    public void setSpecificContextValue(String mapname, Object key, Object value) {
        Map map = getContextMap(mapname);
        map.put(key, value);
    }


    public void setContextMap(Map map, String mapname) 
    {
        if (mapname == null) return;
        for (int i=0; i < precedencenames.size(); i++) {
            String curname = (String)precedencenames.get(i);
            if (mapname.equals(curname)) {
                mapprecedence.set(i, map);
                return;
            }
        }
    }

    public Map getContextMap(String mapname) {
        if (mapname == null) return null;
        for (int i=0; i < precedencenames.size(); i++) {
            String name = (String)precedencenames.get(i);
            if (mapname.equals(name)) 
                return (Map)mapprecedence.get(i); 
        }
        return null;
    }
    
    public Object get(Object key) {
        if (mapprecedence == null) return null;
        for (int i=0; i < mapprecedence.size(); i++)
        {
            Object map = mapprecedence.get(i);
            if (map == null) {
                // ignore
            } else if (map instanceof Map) {
                if (((Map)map).containsKey(key))
                    return ((Map)map).get(key);
            } 
        }
        return null;
    }
        
    public Object put(Object key, Object value)
    {
        return writablecontext.put(key, value);
    }

    public void putAll(Map map)
    {
        writablecontext.putAll(map);
    }

    public Object remove(Object key)
    {
        return writablecontext.remove(key);
    }
    
    public void clear() 
    {
        writablecontext.clear();        
    }
    
    public Set keySet()
    {
        HashSet keys = new HashSet();
        for (int i=0; i < mapprecedence.size(); i++) {
            Object item = mapprecedence.get(i);
            if (item == null) 
                /* ignore*/;
            else if (item instanceof Map) {
                keys.addAll(((Map)mapprecedence.get(i)).keySet());
            } 
        }
        return keys;
    }
    
    public Set entrySet() {
        return null;
    }
    
    public Collection values() {        
        return null;
    }

    
    public boolean containsKey(Object key)
    {
        for (int i=0; i < mapprecedence.size(); i++) {
            Object item = mapprecedence.get(i);
            if (item == null)
                /*ignore*/;
            else if (item instanceof Map) {
                if (((Map)item).containsKey(key)) return true;
            } 
        }
        return false;
    }

    public boolean containsValue(Object value)
    {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public int size() {
        return 0;
    }

}
