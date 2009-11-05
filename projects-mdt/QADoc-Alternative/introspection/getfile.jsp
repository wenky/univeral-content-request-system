<%@page import="java.io.*" %><%@page import="java.net.*" %><%@page import="com.medtronic.ecm.documentum.introspection.*"%><%
OutputStream o = response.getOutputStream();

DctmAccess access = null;
String creds = request.getParameter("c");
if (creds == null) {
    String u = request.getParameter("u");
    String p = request.getParameter("p");
    String b = request.getParameter("b");
    if (u!=null && p!=null && b!= null) {
        access = new DctmAccess(u,p,b);
    } else {
        throw new NullPointerException("no credentials for file retrieval");           
    }
} else {
    creds = DctmAccess.dfcDecrypt(creds);
    String u = creds.substring(0,creds.indexOf('|'));
    creds = creds.substring(creds.indexOf('|')+1);
    String b = creds.substring(0,creds.indexOf('|'));
    String p = creds.substring(creds.indexOf('|')+1);
    access = new DctmAccess(u,p,b);            
}


try {
    access.initSession();
    String id = request.getParameter("id");
    String format = request.getParameter("format");
    response.setContentType(access.getFormatMimeType(format));
    InputStream is = access.getContent(id,format);
    byte[] buf = new byte[32 * 1024];
    int nRead = 0;
    while( (nRead=is.read(buf)) != -1 ) {
        o.write(buf, 0, nRead);
    }
} finally {
  try {access.releaseSession();} catch (Exception ee) {}
}
o.flush();
o.close();
return;
%>