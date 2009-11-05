package com.zoder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// map structure that represents a basic XML document
// remember tag order (except like tags are always grouped together)

// ...need to think about this...

// returns keyset.iterator that represents the order the keys were explicitly putted

// don't use putall
// XStream probably won't work right either, if it uses putall

public class XmlMap extends HashMap
{
    List keyorder = new ArrayList();
    
    HashSet s;
    public Iterator keyorder()
    {
        //List active = new ArrayList(keys.size());
        //for (int i=0; i < keys.size(); i++) 
        //    if (containsKey(keys.get(i)))
        //        active.add(keys.get(i));
        //return active.iterator();
        return keyorder.iterator();
    }
    
    public Object put(Object key, Object value)
    {
        if (!containsKey(key)) {
            keyorder.add(key);
        }
        if (value instanceof String) {
            String s = (String)value;
            // xml encode
            StringBuffer buf = new StringBuffer(s.length());
            for (int i=0; i < s.length(); i++) {
                switch (s.charAt(i)) {
                case '&' : buf.append("&amp;"); break;
                case '>' : buf.append("&gt;"); break;
                case '<' : buf.append("&lt;"); break;
                //case '"' : buf.append("&quot;"); break;
                //case '\'' : buf.append("&apos;"); break;
                default: buf.append(s.charAt(i)); break;
                }
            } return super.put(key,buf.toString());
        } else return super.put(key,value);
        
    }

    public Object putNoEncode(Object key, Object value)
    {
        if (!containsKey(key)) {
            keyorder.add(key);
        }
        return super.put(key,value);
    }

    
    public XmlMap getMap(String key)
    {
        // ensure the key is there
        if (!containsKey(key)) {
            put(key,new XmlMap());
        }
        return (XmlMap)get(key);
    }

    public List getList(String key)
    {
        // ensure the key is there
        if (!containsKey(key)) {
            put(key,new ArrayList());
        }
        return (List)get(key);
    }

}

