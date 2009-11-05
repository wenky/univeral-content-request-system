package lweb.core.util;

public class Is {
    public static boolean empty(String s)
    {
        if (s == null) return true;
        if ("".equals(s.trim())) return true;
        return false;
    }
    

}
