<%@page import="com.medtronic.ecm.documentum.introspection.*"%>
<%@page import="groovy.lang.Binding"%>
<%@page import="java.util.*"%>
<html><body>
<%
boolean secure = true;
String htmldump = null;
try { 
    String script = request.getParameter("do");
    String baseurl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/introspection/ig.jsp";    
    if (script == null) throw new NullPointerException("'do' URL parameter missing");
    DctmAccess access = null;
    String credurl = "";
    String credhidden = "";
    String u=null,p=null,b=null;
    if (secure) {
        String creds = request.getParameter("c");
        if (creds == null) {
            u = request.getParameter("u");
            p = request.getParameter("p");
            b = request.getParameter("b");
            if (u!=null && p!=null && b!= null) {
                access = new DctmAccess(u,p,b);
                String enccred = DctmAccess.dfcEncrypt(u+'|'+b+'|'+p+'|');
                credurl = '&'+WebUtils.encodeParameters("c",enccred);
                credhidden= "<INPUT type='hidden' name='c' value='"+enccred+"'>";
            }            
        }
        if (creds != null) {
            String origcreds = creds;
            creds = DctmAccess.dfcDecrypt(creds);
            u = creds.substring(0,creds.indexOf('|'));
            creds = creds.substring(creds.indexOf('|')+1);
            b = creds.substring(0,creds.indexOf('|'));
            p = creds.substring(creds.indexOf('|')+1);
            access = new DctmAccess(u,p,b);            
            credurl = '&'+WebUtils.encodeParameters("c",origcreds);
            credhidden= "<INPUT type='hidden' name='c' value='"+origcreds+"'>";
        }
    } else { 
        u = request.getParameter("u");
        p = request.getParameter("p");
        b = request.getParameter("b");
        if (u!=null && p!=null && b!= null) {
            access = new DctmAccess(u,p,b);
            credurl = '&'+WebUtils.encodeParameters("u",u,"p",p,"b",b);            
            credhidden= "<INPUT type='hidden' name='u' value='"+u+"'><INPUT type='hidden' name='p' value='"+p+"'><INPUT type='hidden' name='b' value='"+b+"'>";
        }
    }
    if (access != null) access.initSession();
    Map map = new HashMap();
    map.put("request",request);
    map.put("response",response);
    map.put("application",application);
    map.put("dctmaccess",access);
    map.put("baseurl",baseurl);
    map.put("dctmuser",u); 
    map.put("dctmpass",p); 
    map.put("dctmbase",b);
    map.put("credurl",credurl);
    map.put("credhidden",credhidden);
    map.put("bindingmap",map);
    
    try {                
        GroovyIntrospection.initGroovy(request);        
        Binding binding = GroovyIntrospection.createBindingFromMap(map);
        htmldump = GroovyIntrospection.callGroovy(script,binding);
    } finally {
        if (access != null) access.releaseSession();    
    }        
      
} catch (Exception e) {    
    htmldump = WebUtils.dumpHtmlStackTrace(e);
}
out.write(htmldump);
%>
</body></html>
