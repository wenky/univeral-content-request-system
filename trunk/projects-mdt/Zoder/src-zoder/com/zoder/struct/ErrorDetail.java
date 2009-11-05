package com.zoder.struct;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDetail 
{
    
    public String message;
    public Throwable t;
    public Object reference;
    
    public String stacktrace;
    public String stacktrace2;
    public String stacktrace3;
    public int procindex;
    
    public static String stacktrace(Throwable t)
    {
        if (t != null) {
            StringWriter wrt = new StringWriter();
            PrintWriter p = new PrintWriter(wrt);
            t.printStackTrace(p);
            p.flush();
            p.close();
            return wrt.toString();
        }
        return null;
    }

}
