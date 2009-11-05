package com.medtronic.ecm.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.common.IDfId;

public class CollUtils 
{
    public static List getList(Map coll, Object key) {
        List list = (List)coll.get(key);
        if (list == null) {
            list = new ArrayList();
            coll.put(key,list);            
        }
        return list;            
    }

    public static Map getMap(Map coll, Object key) {
        Map map = (Map)coll.get(key);
        if (map == null) {
            map = new HashMap();
            coll.put(key,map);    
        }
        return map;            
    }
    
    public static Map createMap(Object... items)
    {
        Map returnmap = new HashMap();
        return fillMap(returnmap,items);
    }

    public static Map fillMap(Map map,Object... items)
    {
        for (int i=0; i < items.length/2; i++)
        {
            Object key = items[i*2];
            Object value = items[i*2+1];
            map.put(key, value);
        }
        return map;
    }
    
    public static Object lookup(Object c, Object... accesskeys) 
    {
        Object retval = c;
        for (int i=0; i < accesskeys.length; i++) {
            Object curkey = accesskeys[i];
            if (retval instanceof Map) {
                retval = ((Map)retval).get(curkey);
            } else if (retval instanceof List) {
                retval = ((List)retval).get((Integer)curkey);
            } else if (retval instanceof Array) {
                retval = Array.get(retval, (Integer)curkey);
            } 
        }
        return retval;
        
    }

    public static Object lookupExpression(Object c, String exp) {
        Object retval = c;
        int i=0; 
        while (true) {
            int curindex = exp.indexOf('[',i);
            if (curindex == -1) break;
            int nextindex = exp.indexOf(']', i+1);
            String curkey = exp.substring(curindex+1,nextindex);
            try { 
                if (Character.isDigit(curkey.charAt(0))) {
                    // assume list
                    int index = -1;
                    try {
                        index = Integer.parseInt(curkey);
                        if (retval instanceof Array) {
                            retval = Array.get(retval, index);
                        } else {
                            // assume a list
                            List curlist = (List)retval;
                            retval = curlist.get(index);
                        }
                    } catch (NumberFormatException nfe) {
                        // must be a key after all
                        Map curmap = (Map)retval;
                        retval = curmap.get(curkey);
                    }
                } else {
                    // assume map
                    Map curmap = (Map)retval;
                    retval = curmap.get(curkey);
                }
                i = nextindex+1;
            } catch (Exception e) {
                throw new RuntimeException("Lookup expression "+exp+" failed on key "+curkey,e);
            }
        }
        return retval;
    }
    
}
