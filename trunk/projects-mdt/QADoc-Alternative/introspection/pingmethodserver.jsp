<%@ page import="com.medtronic.ecm.documentum.introspection.*"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

String user = request.getParameter("u");
String pass = request.getParameter("p");
String base = request.getParameter("d");

PingMethodServer ping = new PingMethodServer();

String result = ping.ping(user,pass,base,"QADocPing");

%>

<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>Introspection - <%=request.getParameter("page")%></title>
    
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="This is my page">
    
    <!--
    <link rel="stylesheet" type="text/css" href="styles.css">
    -->
  </head>
  
  <body>
    <hr>
    RESPONSE: <%=result%>
    <hr>
  </body>
</html>
