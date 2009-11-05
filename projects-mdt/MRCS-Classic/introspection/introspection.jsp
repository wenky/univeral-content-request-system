
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.medtronic.documentum.mrcs.introspection.*"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

MrcsIntrospection mi = new MrcsIntrospection();
String htmldump;
try { 
    htmldump = mi.perform(request);
} catch (Exception e) {
    htmldump = e.toString();
}

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
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
    <%=htmldump%>
  </body>
</html>
