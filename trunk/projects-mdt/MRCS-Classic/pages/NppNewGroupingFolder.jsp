<%--
***********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: NppNewGroupingFolder.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:39 $

***********************************************************************
--%>


<%@ page language="java" import="java.util.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <%// enable webtop/wdk form processing logic %>
    <dmf:webform/>    
    <base href="<%=basePath%>">
    
    <title>Create new NPP Grouping Folder</title>
    
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
    New NPP Grouping Folder<br>
    <dmf:form>
      Folder Name: <dmf:text name="MrcsCustomObjectName"/><br>
      NPP Comment: <dmf:text name="MrcsCustomNPPComment"/><br>
      NPP Reason: <dmf:text name="MrcsCustomNPPReason"/><br>
      <dmf:button name="CarlSubmit" onclick="createMrcsGroupingFolder"  />
    </dmf:form>
    
    <% 
    
    // - event trigger from form submit
    // - dropdown population
    
    %>
  </body>
</html>
