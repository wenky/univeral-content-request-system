package com.medtronic.ecm.documentum.introspection;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class WebUtils {

    public static String getQueryString(HttpServletRequest request)
    {
        // &#*&#$ing Tomcat destroys the query string at some point....reconstruct it
        Map params = request.getParameterMap();
        Iterator i = params.keySet().iterator();
        StringBuffer querystring = new StringBuffer("");
        boolean first = true;
        while (i.hasNext()) {
            String name = (String)i.next();
            Object value = params.get(name);
            if (value instanceof String) {
                if (first) first = false; else querystring.append('&');
                querystring.append(URLEncoder.encode(name)).append('=').append(URLEncoder.encode((String)value));
            } else if (value instanceof String[]) {
                String[] values = (String[])value;
                for (int ii=0; ii < values.length; ii++) {
                    if (first) first = false; else querystring.append('&');
                    querystring.append(URLEncoder.encode(name)).append('=').append(URLEncoder.encode(values[ii]));
                }
            }
        }
        return querystring.toString();
    }
    
    public static String encodeParameters(String... args)
    {
        StringWriter swrt = new StringWriter();
        boolean first = true;
        for (int i=0; i < args.length/2; i++)
        {            
            if (first) first=false; else swrt.append('&');
            swrt.append(URLEncoder.encode(args[i*2])).append('=').append(URLEncoder.encode(args[i*2+1]));
        }
        return swrt.toString();
    }
    
    public static String dumpHtmlStackTrace(Throwable t)
    {
        StringWriter s = new StringWriter();
        t.printStackTrace(new PrintWriter(s));
        String trace = s.toString();
        //return trace.replace("\tat", "<BR>");
        return trace.replace("\n", "<BR>");
    }

    public static String encode(String s) { return URLEncoder.encode(s);}

    public static String makeXMLdisplayableInHTML(String dump)
    {
        dump = dump.replaceAll("<","&lt;");
        dump = dump.replaceAll(">","&gt;");
        dump = dump.replaceAll(" ","&nbsp;");
        dump = dump.replaceAll("\n","<br>");
        return dump;
    }

}
