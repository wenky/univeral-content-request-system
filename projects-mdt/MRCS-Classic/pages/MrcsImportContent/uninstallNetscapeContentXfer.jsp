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

 Filename       $RCSfile: uninstallNetscapeContentXfer.jsp,v $
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
<%@ page import="com.documentum.web.util.Browser" %>
<dmf:html>
<dmf:head>
<dmf:title>Uninstall Content Transfer Registry library</dmf:title>
<script src="<%=request.getContextPath()%>/wdk/include/browser.js"></script>
<script language='JavaScript'>
function finish()
{
fIsNetscape = isNetscape();
if (fIsNetscape == true)
{
trigger = InstallTrigger;
jarPath = "../../../wdk/contentXfer/UninstallContentXfer.xpi";
xpi = {'Documentum/WDK/Content Transfer': jarPath};
iresult = trigger.install(xpi);
}
}
</script>
</dmf:head>
<dmf:body topmargin='20' leftmargin='0' rightmargin='0' bottommargin='0' marginheight='20' marginwidth='0'>
<%
if (Browser.isNetscape(request))
{
%>
<script language='JavaScript'>
finish();
</script>
<%
}
else
{ // It's IE
%>
With Internet Explorer, please use the Tools|Internet Options to uninstall Documentum Content Transfer.
<%
}
%>
</dmf:body>
</dmf:html>
<%
//
%>
