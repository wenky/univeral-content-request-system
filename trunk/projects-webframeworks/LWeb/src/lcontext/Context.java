package lcontext;

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

    protected List mapprecedence = null;
    protected Map mapprecedencenames = null;
    int writable = -1;               
    
    // internal accessors 
    List getPrecedence() {
        if (mapprecedence == null) mapprecedence = new ArrayList();
        return mapprecedence;
    }

    Map getPrecedenceNames() {
        if (mapprecedencenames == null) mapprecedencenames = new HashMap();
        return mapprecedencenames;
    }
    
    Map getWritable() {
        if (writable == -1) return null;
        return (Map)mapprecedence.get(writable);
    }


    public void addContext(Object contextitem) {
        getPrecedence().add(contextitem);
    }
    
    public void addNamedContext(Object contextitem, String name) 
    {
        getPrecedence().add(contextitem);
        int idx = getPrecedence().size()-1;
        getPrecedenceNames().put(name,idx);
    }

    public void addContexts(Object... contextitems) {
        for (int i=0; i < contextitems.length; i++) {
            addContext(contextitems[i]);
        }
    }
    
    // name, contextitem, name, contextitem, etc
    public void addNamedContexts(Object... contextitems) 
    {
        for (int i=0; i < contextitems.length/2; i++) {
            addNamedContext(contextitems[i*2+1],(String)contextitems[i*2]);
        }
    }
    
    public void addWritableContext(Object contextitem) {
        getPrecedence().add(contextitem);
        int idx = getPrecedence().size()-1;
        writable = idx;
    }
    
    public void addWritableNamedContext(Object contextitem, String name) 
    {
        getPrecedence().add(contextitem);
        int idx = getPrecedence().size()-1;
        getPrecedenceNames().put(name,idx);
        writable = idx;
    }

    public void setNamedContext(Object contextitem, String name) 
    {
        int idx = (Integer)getPrecedenceNames().get(name);
        getPrecedence().set(idx, contextitem);
    }

    public void setContext(Object contextitem, int idx) 
    {
        getPrecedence().set(idx, contextitem);
    }

    public void clearNamedContext(String name) 
    {
        int idx = (Integer)getPrecedenceNames().get(name);
        getPrecedence().set(idx, null);
    }

    public void clearContext(int idx) 
    {
        getPrecedence().set(idx, null);
    }
    
    public Object getNamedContext(String name) 
    {
        Integer idx = (Integer)getPrecedenceNames().get(name);
        if (idx == null) return null;
        return getPrecedence().get(idx);
    }

    public Object getContext(int idx) 
    {
        return getPrecedence().get(idx);
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
            } else if (map instanceof IContextItem) {
                IContextItem item = (IContextItem)map;                
                Object returnval = item.get(key);
                if (returnval != null)
                    return returnval;
            }
        }
        return null;
    }
        
    public Object put(Object key, Object value)
    {
        return getWritable().put(key, value);
    }

    public void putAll(Map map)
    {
        getWritable().putAll(map);
    }

    public Object remove(Object key)
    {
        return getWritable().remove(key);
    }
    
    public void clear() 
    {
        getWritable().clear();        
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
            } else if (item instanceof IContextItem) {
                IContextItem citem = (IContextItem)item;
                if (citem.isEnumerable()) {
                    Enumeration en = citem.enumerate();
                    while (en.hasMoreElements()) {
                        Object key = en.nextElement();
                        keys.add(key);
                    }
                } else if (citem.isIterable()) {
                    Iterator iter = citem.iterate();
                    while (iter.hasNext()) {
                        Object key = iter.next();
                        keys.add(key);
                    }
                }
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
            } else if (item instanceof IContextItem) {
                IContextItem citem = (IContextItem)item;
                if (citem.isEnumerable()) {
                    Enumeration en = citem.enumerate();
                    while (en.hasMoreElements()) {
                        Object value = citem.get(en.nextElement());
                        if (value != null) return true;
                    }
                } else if (citem.isIterable()) {
                    Iterator iter = citem.iterate();
                    while (iter.hasNext()) {
                        Object value = citem.get(iter.next());
                        if (value != null) return true;
                    }
                }
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
