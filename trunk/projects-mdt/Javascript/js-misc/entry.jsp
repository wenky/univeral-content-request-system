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

 Filename       $RCSfile: entry.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:43 $

***********************************************************************
--%>

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<html>
<head><title>ePCR Test Page</title></head>
<body>
<%@ page import="com.medtronic.message.encoding.EncodingUtils" %>

<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

<h3>MRCS Anonymous Access Test Page</h3>

<a href='http://localhost:8080/mrcs41/component/anonymous?ticket=<%=EncodingUtils.getTicket("NPP")%>'>Log in anonymously.</a>

</body>
</html>
