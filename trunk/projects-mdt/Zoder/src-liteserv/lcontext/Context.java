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
    Map writablecontext = null;
        
    public void initContext() {
        mapprecedence = new ArrayList();
        writablecontext = new HashMap();
        mapprecedence.add(writablecontext);        
    }

    public void initContext(List orderofmaps) {
        mapprecedence = orderofmaps;
        writablecontext = (Map)orderofmaps.get(0);
    }

    public void initContext(List orderofmaps, int writableindex) {
        mapprecedence = orderofmaps;
        writablecontext = (Map)orderofmaps.get(writableindex);
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
