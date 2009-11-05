<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@page import="com.medtronic.ecm.documentum.core.web.install.*"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'inst.jsp' starting page</title>
    
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
    <%
      String user = request.getParameter("user");
      String pass = request.getParameter("pass");
      String base = request.getParameter("base");
      String xmldoc = request.getParameter("xmldoc");
      Map status = DocbaseObjectInstallationService.processObjectScript(user,pass,base,xmldoc);
      List messages = (List)status.get("messages");
      
      for (int i=0; i < messages.size(); i++) {
    %>
       <%=messages.get(i)%><br>
    <%
      }
    %> 
  
  </body>
</html>
