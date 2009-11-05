package com.medtronic.ecm.util;

public class Is {
    public static boolean endsIn(String s, String extension) {
        if (extension == null) return false;
        if (s == null) return false;
        if (s.length() < extension.length()) return false;
        if (extension.equalsIgnoreCase(s.substring(s.length()-extension.length()))) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean empty(String s)
    {
        if (s == null) return true;
        if ("".equals(s.trim())) return true;
        return false;
    }
    
    public static boolean yes(Object o) 
    {
        if (o == null) return false; // null is a no...
        
        if (o instanceof String) {
            String s = (String)o;
            if ("".equalsIgnoreCase(s)) return false;
            if ("true".equalsIgnoreCase(s)) return true;
            if ("false".equalsIgnoreCase(s)) return false;
            if ("T".equalsIgnoreCase(s)) return true;
            if ("F".equalsIgnoreCase(s)) return false;
            if ("yes".equalsIgnoreCase(s)) return true;
            if ("no".equalsIgnoreCase(s)) return false;
        }
        
        if (o instanceof Integer) {
            int i = (Integer)o;
            return (i != 0);
        }
        
        if (o instanceof Boolean) {
            boolean b = (Boolean)o;
            return b;
        }
        
        return false;
    }

}
