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

 Filename       $RCSfile: verifyFullContentXferInstall.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:43 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form,
com.documentum.web.contentxfer.common.IContentXferConstants" %>
<dmf:webform/>
<dmf:html>
<dmf:head>
<script src="<%=request.getContextPath()%>/webcomponent/library/contentxfer/checkContentXferAppletInstall.js"></script>
<%
Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
//
%>
<script language='JavaScript'>
function verifyInstall()
{
checkFullInstall("<%=form.getString("MSG_INSTALL_FULL_SUCCESS_MESSAGE")%>","<%=form.getString("MSG_INSTALL_FULL_FAILURE_MESSAGE")%>");
postServerEvent(null, null, null, "onVerifyInstallFullComplete");
}
</script>
</dmf:head>
<dmf:body onload='verifyInstall();'>
<dmf:form>
<applet code='com.documentum.web.applet.clientdetect.CheckContentXferInstall' id='checkInstallApplet' name='checkInstallApplet'  alt='checkInstallApplet' width='0' height='0'
archive='<%=request.getContextPath() + IContentXferConstants.DEFAULT_CODEBASE_URL + "clientDetect.jar"%>'>
<param name='cache_option' value='Plugin'>
<param name='cache_archive' value='<%=request.getContextPath() + IContentXferConstants.DEFAULT_CODEBASE_URL + "clientDetect.jar"%>'>
</applet>
</dmf:form>
</dmf:body>
</dmf:html>
