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

 Filename       $RCSfile: repeatesgn.jsp,v $
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
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmanager.RepeatWorkflowTask" %>
<dmf:webform/>
<dmf:form>
<table width='100%'>
<tr><td style='padding-left:10px'>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<tr>
<td colspan='2' class='taskmgrSectionHeading' height='24'>
<dmf:label nlsid='MSG_REPEAT_ACTION_HDR'/>
</td>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
<tr>
<td>
<table border="0" cellpadding="0" cellspacing="0">
<tr><td scope="row" style='padding-right:10px' valign=top>
<dmf:label nlsid='MSG_REPEAT_PERFORMER_NAME'/>
</td><td valign=top>
<dmf:link onclick='onSelectTaskRepeater' name='<%=RepeatWorkflowTask.REPEAT_PERFORMER_NAME_CONTROL_LABEL%>'/>
</td>
<tr>
</table>
</td>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
<dmf:panel name='<%=RepeatWorkflowTask.SIGNOFF_PANEL_CONTROL_NAME%>'>
<tr>
<td colspan='2' height='24' class='taskmgrSectionHeading' style='padding-top: 10px; padding-bottom: 5px'>
<dmf:label nlsid='MSG_SIGNOFF_REQUIRED_HDR'/>
</td>
</tr>
<tr>
<td>
<jsp:include page="/mrcs/pages/esignPanel.jsp" />
<%-- invisible button used here as a proxy for handler invocation--%>
<dmf:button label='default' visible='false' enabled='true' default='true' onclick='onAttemptFinish'/>
</td>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
</dmf:panel>
</table>
</td></tr>
</table>
</dmf:form>
