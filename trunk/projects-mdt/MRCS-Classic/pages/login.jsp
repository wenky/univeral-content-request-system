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

 Filename       $RCSfile: login.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:40 $

***********************************************************************
--%>

<%
//    
%> 
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ page import="com.documentum.web.formext.session.Login" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%
String strContextPath = request.getContextPath();
//
%>
<script language='javascript'>
function loginAgain()
{
window.close();
}
</script>
<html>
<head>
<dmf:webform/>
<title><dmf:label nlsid="MSG_TITLE"/></title>
<!--  CEM: cust this -->
<script language="JavaScript" src="<%=strContextPath%>/wdk/include/browserRequirements.js"></script> 
<script>
function onClickHelp()
{
fireClientEvent("InvokeHelp");
}
</script>
</head>
<body class='contentBackground' topmargin='0' bottommargin='0'
leftmargin='0' rightmargin='0' marginheight='0' marginwidth='0'>
<dmf:form>
<dmf:browserrequirements/>
<div style="position:absolute;top:0;right:0;bottom:0;left:0;width:100%;height:100%;margin:auto;">
<table height='100%' width='100%' align='center' cellspacing='0' cellpadding='0' border='0'>
<tr><td valign='middle'>
<table align='center' cellspacing='0' cellpadding='0' border='0'>
<tr class='contentBorderDark'><td>
<table align='center' cellspacing='1' cellpadding='0' border='0'>
<tr class='contentBackground'><td>
<table width='500' align='center' cellspacing='0' cellpadding='0' border='0'>
<tr class='contentBorder' align='left'>
<td valign='middle'>&nbsp;<dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE'/></td>
</tr>
<tr class='contentBackground' align='left'>
<td valign='middle'>
<table cellspacing='0' cellpadding='0' border='0'>
<tr>
<td><dmf:image src='images/documentum.gif'/></td>
</tr>
</table>
</td>
</tr>
<tr class='contentBorder' align='center'>
<td height='3' valign='middle'><dmf:image src='images/space.gif'/></td>
</tr>
<tr><td>&nbsp;&nbsp;</td></tr>
<tr>
<td align='center' valign='middle'>
You have successfully logged off.  To close this window <dmf:link onclick='loginAgain' label="click here" runatclient="true"/>.
</td>
</tr>
<tr><td height='12'><dmf:image src='images/space.gif'/></td></tr>
<tr><td>

<dmf:panel name='<%=Login.CONTROL_ERRMSGPANEL%>'>
<tr><td height='10'><dmf:image src='images/space.gif'/></td></tr>
<tr class='contentBorder' align='center'>
<td height='3' valign='middle'><dmf:image src='images/space.gif'/></td>
</tr>
<tr>
<td align='center'>
<table align='left' cellspacing='0' cellpadding='5' border='0'>
<tr><td>
<dmf:label name='<%=Login.CONTROL_ERRMSG%>'/>
</td></tr>
</table>
</td>
</tr>
</dmf:panel>
<tr><td height='10'><dmf:image src='images/space.gif'/></td></tr>
<tr class='contentBorder' align='center'><td valign='middle'>&nbsp;</td></tr>
</td></tr>
</table>
</table>
</td></tr>
</table>
</td></tr>
</table>
</div>
</dmf:form>
</body>
</html>
