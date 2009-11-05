package com.cem.lweb.core.util;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class CfgUtils
{    
    public static Map getMap(Map map, Object key) { return (Map)map.get(key); }
    public static List getList(Map map, Object key) { return (List)map.get(key); }
    public static Object lookup(Object c, String exp) {
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
                        // must be a key afterall
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
    
    
}
