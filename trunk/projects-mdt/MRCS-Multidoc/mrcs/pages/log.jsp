<%--
***********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.2.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: log.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2007/03/28 21:14:02 $

***********************************************************************
--%>
<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<dmf:title>MRCS Log Fetcher</dmf:title>

<style type="text/css">
.centered {
position: absolute;
top: 0;
right: 0;
bottom: 0;
left: 0;
width: 100%;
height: 100%;
margin: auto;
}
</style>
</dmf:head>
<dmf:body cssclass='contentBackground' topmargin='40' bottommargin='0'
leftmargin='0' rightmargin='0' marginheight='40' marginwidth='0'>
<dmf:form>
<table>
<tr><td valign="middle" align="center"><dmf:label name='log' /></td></tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
