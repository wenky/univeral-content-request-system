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

 Filename       $RCSfile: importFileSelection.jsp,v $
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
<%@ taglib uri="/WEB-INF/tlds/dmcontentxfer_1_0.tld" prefix="dmxfer" %>
<%@ page import="com.documentum.web.form.Form,
com.documentum.web.formext.component.DialogContainer"%>
<%@ page import="com.documentum.webcomponent.library.importcontent.ImportContainer"%>
<dmf:html>
<dmf:head>
<dmf:webform/>
<% Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);%>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
</dmf:head>
<dmf:body cssclass='contentBorder' marginheight='0' marginwidth='12'
topmargin='0' bottommargin='0' leftmargin='12' rightmargin='0'>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr><td valign='top'>
<dmf:form>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr height='5'><td valign='top'></td></tr>
<tr height='1'><td valign='top'>
<dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE_IMPORT_FILE_SELECTION'/>:&nbsp;
<dmf:label cssclass='dialogFileName' nlsid='MSG_OBJECT'/>
</td></tr>
<tr height='5'><td valign='top'></td></tr>
<tr class='contentBackground'><td align='left' valign='top'>
<table border="0" cellpadding="5" cellspacing="5" width='100%'>
<tr><td>
<b><dmf:label nlsid="MSG_SELECTED_FILES"/>:</b>
</td></tr>
<tr>
<td>
<dmf:fileselectorapplet height="250" width="500" name="<%=ImportContainer.FILE_SELECTOR_APPLET_CONTROL%>" />
</td>
</tr>
</table>
</td>
</tr>
<tr height='4'><td></td></tr>
<tr><td>
<br>
<table border='0' cellpadding='0' cellspacing='0'>
<tr>
<td>
<dmf:button name='prev' style='color:#000000' cssclass="buttonLink" nlsid='MSG_PREV' onclick='onPrev'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_PREV_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='next' cssclass='buttonLink' nlsid='MSG_NEXT' onclick='onNext'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_NEXT_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='ok' cssclass="buttonLink" nlsid='MSG_OK' onclick='onOk'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_OK_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='cancel' cssclass='buttonLink' nlsid='MSG_CANCEL' onclick='onCancel'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='<%=DialogContainer.CONTROL_HELPBUTTON %>' cssclass="buttonLink" nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_HELP_TIP'/>
</td>
</tr>
</table>
</td></tr>
<tr height='4'><td valign='bottom'></td></tr>
</table>
</dmf:form>
</td></tr>
</table>
</dmf:body>
</dmf:html>
