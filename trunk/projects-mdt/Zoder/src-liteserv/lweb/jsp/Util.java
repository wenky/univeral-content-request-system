package lweb.jsp;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

public class Util 
{
    public static Map getActionContext(HttpServletRequest request)
    {
        return (Map)request.getAttribute("ActionContext");
    }
    
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

}
