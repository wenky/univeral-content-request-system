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

 Filename       $RCSfile: warning.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:42 $

***********************************************************************
--%>

<%@ page import="com.documentum.webcomponent.library.checkout.CheckoutContainer,
com.documentum.web.form.control.databound.IDataboundParams"%>
<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
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
<dmf:label cssclass='dialogTitle' nlsid='MSG_WARNING_HEADER'/>
</td></tr>
<tr height='5'><td valign='top'></td></tr>
<tr class='contentBackground'><td align='left' valign='top'>
<table style='padding-top:10;padding-left:10'>
<tr>
<td>
<dmf:label nlsid='MSG_WARNING_TEXT'/>
</td>
</tr>
<tr>
<td>
<dmf:datagrid name="<%=CheckoutContainer.WARNING_GRID_CONTROL_NAME%>" paged="false" bordersize="0" cellspacing="0" cellpadding="5"
sortcolumn="warning" sortdir="<%=Integer.toString(IDataboundParams.SORTDIR_REVERSE)%>" >
<dmf:datagridRow>
<td>
<dmf:label datafield="warning" />
</td>
<td nowrap>
<dmf:multilinestringlengthformatter wrapped="false" showastooltip="true" maxlen="100" lineseparator="<br>" postfix="...">
<dmf:htmlsafetextformatter>
<dmf:label datafield="warning_details" />
</dmf:htmlsafetextformatter>
</dmf:multilinestringlengthformatter>
</td>
</dmf:datagridRow>
</dmf:datagrid>
</td>
</tr>
</table>
<br>
</td>
</tr>
<tr><td>
<br>
<table border='0' cellpadding='0' cellspacing='0'>
<tr>
<td>
<dmf:button name='continue_button' cssclass="buttonLink" nlsid='MSG_CONTINUE' onclick='onContinueAfterWarning'
height='16' imagefolder='images/dialogbutton'  tooltipnlsid='MSG_CONTINUE'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='cancel' cssclass='buttonLink' nlsid='MSG_CANCEL' onclick='onCancel'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL'/>
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
