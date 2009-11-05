package com.zoder.util;

// logging utils

public class Lg 
{
    
    static String getCurrentThreadClassNameInternal() {
        String curclass = Thread.currentThread().getStackTrace()[4].getClassName();
        return curclass;
    }
    
    static String getCurrentThreadMethodNameInternal() {
        String curclass = Thread.currentThread().getStackTrace()[4].getMethodName();
        return curclass;
    }
    
    // simple stdout logging
    public static void log(String msg,Object... args)
    {
        try { 
            System.out.println((new StringBuffer()).append(getCurrentThreadClassNameInternal())
                                                   .append('-') 
                                                   .append(getCurrentThreadMethodNameInternal())
                                                   .append(':') 
                                                   .append(msg));
        } catch (Exception e) {}
    }
    

}
