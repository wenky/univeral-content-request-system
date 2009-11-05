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

 Filename       $RCSfile: restartBrowser.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:42 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<dmf:webform/>
<dmf:html>
<dmf:head>
<script language='JavaScript'>
function ok()
{
postServerEvent(null, null, null, "onComponentReturn");
}
</script>
</dmf:head>
<dmf:body topmargin='20' leftmargin='0' rightmargin='0' bottommargin='0' marginheight='20' marginwidth='0'>
<dmf:form>
<div id='info' style='visibility:visible'>
<table width='100%' cellpadding='0' cellspacing='0'>
<tr>
<td class='heading' align='center'>
<dmf:label nlsid='MSG_RESTART_BROWSER'/>
<br><br>
</td>
</tr>
<tr>
<td align='center'>
<dmf:button nlsid='MSG_OK_BUTTON' runatclient="true" onclick='ok' tooltipnlsid='MSG_OK_BUTTON'/>
</td>
</tr>
</table>
</div>
</dmf:form>
</dmf:body>
</dmf:html>
<%
//
%>
