package lweb.core.util;

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
            if (Character.isDigit(curkey.charAt(0))) {
                // assume list
                int index = -1;
                try {
                    index = Integer.parseInt(curkey);
                    // assume a list
                    List curlist = (List)retval;
                    retval = curlist.get(index);
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
        }
        return retval;
    }
    
}
