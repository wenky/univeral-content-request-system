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

 Filename       $RCSfile: MrcsQuickflow.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:39 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form"%>
<%@ page import="com.documentum.webcomponent.library.workflow.sendto.SendToDistributionList"%>
<dmf:webform/>
<dmf:form>
<table border="0" cellpadding="0" cellspacing="0" height='100%' width='100%'>
<tr><td valign=top style='padding-left:10px;padding-right:10px' width='120px' >
<table cellpadding='0'>
<tr><th scope="col" style='padding-bottom:10px;padding-top:10px'>
<dmf:link nlsid='MSG_SELECT_FILES_LINK' onclick='onSelectDocuments'/>
</th></tr>
<tr><td>
<dmf:datagrid name='<%=SendToDistributionList.SELECTED_FILES_DATAGRID_CONTROL_NAME%>' paged='false'
cellpadding='0' cellspacing='0' bordersize='0'>
<dmf:datagridRow>
<td nowrap width='16' valign=top style='padding-right:1px;padding-bottom:3px'>
<dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type'
linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' size='16'/>
</td>
<td scope="row">
<dmf:stringlengthformatter maxlen='20' postfixnlsid='MSG_TRUNCATE_FILENAME_POSTFIX' wrapped='true'>
<dmf:label style='font-size:9px' datafield='object_name'/>
</dmf:stringlengthformatter>
</td>
</dmf:datagridRow>
</dmf:datagrid>
</td></tr>
</table>
</td>
<td style='width: 1px;' class='rowSeparator' width='1px'>
<dmf:image src='images/space.gif' alttext=''/>
</td>
<td valign=top style='padding-left:10px'>
<table border="0" cellpadding="10" cellspacing="0" width='100%'>
<tr>
<td scope="row" valign='top' class='taskmgrSectionHeading'>
<dmf:label nlsid='MSG_SENDTO_HEADER'/>
</td>
</tr>
<tr><td colspan='2'>
<table cellpadding='0' cellspacing='0'>
<tr>
<td scope="row" valign=top>
<dmf:label nlsid='MSG_TO_LABEL'/>
</td>
<td style='padding-left:10px'>
<dmf:label name='<%=SendToDistributionList.RECIPIENTS_LABEL_CONTROL_NAME%>'/>
</td>
<td valign=top style='padding-left:10px'>
<dmf:link nlsid='MSG_USER_SELECT_LINK' onclick='onSelectRecipients'/>
&nbsp;<dmf:requiredfieldvalidator name="<%=SendToDistributionList.RECIPIENTS_VALIDATOR%>" controltovalidate="<%=SendToDistributionList.RECIPIENTS_LABEL_CONTROL_NAME%>" nlsid="MSG_MUST_HAVE_RECIPIENTS"/>
</td>
</tr>
</table>
</td></tr>
<tr><td>
<table cellpadding='0' cellspacing='0'>
<tr>
<td scope="row" valign=top style='padding-right:10px'>
<dmf:label nlsid='MSG_PRIORITY_LABEL'/>
</td>
<td align=left>
<dmf:dropdownlist name='<%=SendToDistributionList.PRIORITY_DROPDOWN_CONTROL_NAME%>' value='<%=SendToDistributionList.PRIORITY_VALUE_MEDIUM%>' tooltipnlsid='MSG_PRIORITY_LABEL'>
<dmf:option value='<%=SendToDistributionList.PRIORITY_VALUE_HIGH%>' nlsid='MSG_PRIORITY_HIGH'/>
<dmf:option value='<%=SendToDistributionList.PRIORITY_VALUE_MEDIUM%>' nlsid='MSG_PRIORITY_MEDIUM'/>
<dmf:option value='<%=SendToDistributionList.PRIORITY_VALUE_LOW%>' nlsid='MSG_PRIORITY_LOW'/>
</dmf:dropdownlist>
</td>
</tr>
<tr>
<td scope="row" valign=top style='padding-right:10px'>
<dmf:label nlsid='MSG_INSTRUCTIONS_LABEL'/>
</td>
<td align=left>
<dmf:textarea cols='50' rows='7' name='<%=SendToDistributionList.INSTRUCTIONS_TEXTAREA_CONTROL_NAME%>' tooltipnlsid='MSG_INSTRUCTIONS_LABEL'/>
</td>
</tr>
<tr>
<td>
&nbsp;
</td>
<td>
<dmf:regexpvalidator name="<%=SendToDistributionList.INSTRUCTIONS_VALIDATOR_CONTROL_NAME%>" controltovalidate="<%=SendToDistributionList.INSTRUCTIONS_TEXTAREA_CONTROL_NAME%>" expression=".{0,127}" nlsid="MSG_INSTRUCTIONS_LENGTH_ERROR"/>
</td>
</tr>
<dmf:panel name='<%=SendToDistributionList.SEQUENTIAL_PANEL_CONTROL_NAME%>'>
<tr>
<td valign=top>
&nbsp;
</td>
<td scope="row" align=left style='padding-top:10px'>
<dmf:checkbox nlsid='MSG_SEQUENTIAL_LABEL' name='<%=SendToDistributionList.SEQUENTIAL_CHECKBOX_CONTROL_NAME%>' onclick='onCheckSequential'/>
</td>
</tr>
<tr>
<td valign=top>
&nbsp;
</td>
<td scope="row" align=left style='padding-left:20px'>
<dmf:checkbox nlsid='MSG_REJECT_INITIATOR_LABEL' name='<%=SendToDistributionList.REJECT_INITIATOR_CHECKBOX_CONTROL_NAME%>' enabled='false'/>
</td>
</tr>
<tr>
<td valign=top>
&nbsp;
</td>
<td scope="row" align=left style='padding-left:20px'>
<dmf:checkbox nlsid='MSG_REJECT_PREVIOUS_LABEL' name='<%=SendToDistributionList.REJECT_PREVIOUS_CHECKBOX_CONTROL_NAME%>' enabled='false'/>
</td>
</tr>
</dmf:panel>
<tr>
<td valign=top>
&nbsp;
</td>
<td scope="row" align=left style='padding-top:10px'>
<dmf:checkbox nlsid='MSG_END_NOTIFICATION_LABEL' name='<%=SendToDistributionList.NOTIFICATION_CHECKBOX_CONTROL_NAME%>'/>
</td>
</tr>
<!--
<dmf:panel name='<%=SendToDistributionList.SIGNOFF_PANEL_CONTROL_NAME%>'>
<tr>
<td valign=top>
&nbsp;
</td>
<td scope="row" align=left style='padding-top:10px'>
<dmf:checkbox nlsid='MSG_REQUIRE_SIGNOFF_LABEL' name='<%=SendToDistributionList.SIGNOFF_CHECKBOX_CONTROL_NAME%>'/>
</td>
</tr>
</dmf:panel>
-->
</table>
</td></tr>
</table>
</td></tr>
</table>
</dmf:form>
