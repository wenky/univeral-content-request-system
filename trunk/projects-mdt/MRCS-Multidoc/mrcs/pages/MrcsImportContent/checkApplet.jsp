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

 Filename       $RCSfile: checkApplet.jsp,v $
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
<%@ page import="com.documentum.web.contentxfer.common.IContentXferConstants" %>
<dmf:webform/>
<dmf:macclientdetect/>
<dmf:html>
<dmf:head>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
<script src="<%=request.getContextPath()%>/webcomponent/library/contentxfer/checkContentXferAppletInstall.js"></script>
<script>
function finish()
{
var bInstalled = checkContentXferAppletInstall("<%=IContentXferConstants.VERSION_NUMBER%>", false);
if (bInstalled == true)
{
postServerEvent(null, null, null, "onCheckAppletComplete");
}
else
{
postServerEvent(null, null, null, "onNeedInstall");
}
}
</script>
</dmf:head>
<dmf:body onload='finish()' cssclass='contentBackground' marginheight='0' marginwidth='12'
topmargin='0' bottommargin='0' leftmargin='12' rightmargin='0'>
<dmf:form>
<%
if (Browser.isIE(request) && Browser.isWin(request))
{
%>
<applet code='com.documentum.web.applet.clientdetect.CheckVM' id='vmDetectApplet' name='vmDetectApplet' alt='vmDetectApplet' width='0' height='0'
archive='<%=request.getContextPath() + IContentXferConstants.DEFAULT_CODEBASE_URL + "clientDetect.jar"%>'>
<param name='cache_option' value='Plugin'>
<param name='cache_archive' value='<%=request.getContextPath() + IContentXferConstants.DEFAULT_CODEBASE_URL + "clientDetect.jar"%>'>
</applet>
<applet code='com.documentum.web.applet.clientdetect.CheckContentXferInstall' id='showVersion' name='showVersion' alt='showVersionApplet' width='0' height='0'
archive='<%=request.getContextPath() + IContentXferConstants.DEFAULT_CODEBASE_URL + "clientDetect.jar"%>'>
<param name='cache_option' value='Plugin'>
<param name='cache_archive' value='<%=request.getContextPath() + IContentXferConstants.DEFAULT_CODEBASE_URL + "clientDetect.jar"%>'>
</applet>
<%
}
%>
</dmf:form>
</dmf:body>
</dmf:html>
