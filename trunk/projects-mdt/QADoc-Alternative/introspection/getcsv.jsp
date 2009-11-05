<%@page import="java.io.*" %><%@page import="com.medtronic.ecm.documentum.util.*" %><%@page import="com.medtronic.ecm.documentum.introspection.*"%><%
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
        throw new NullPointerException("no credentials for query execution");           
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
    String query = request.getParameter("query");
    response.setContentType("text/csv");
    
    String csv = CsvUtils.execQueryCSV(access.accessSession(),query);
    
    PrintWriter po = new PrintWriter(o);
    po.write(csv);
    po.flush();
    po.close();    
} finally {
  try {access.releaseSession();} catch (Exception ee) {}
}
return;
%>