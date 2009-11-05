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

 Filename       $RCSfile: MrcsTaskManagerContainer.jsp,v $
 Author         $Author: wallam1 $
 Revision       $Revision: 1.4 $
 Modified on    $Date: 2007/03/22 16:44:05 $

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
com.documentum.web.formext.component.DialogContainer" %>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmgrcontainer.TaskMgrContainer" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
</dmf:head>
<dmf:body cssclass='contentBorder' marginheight='0' marginwidth='12'
topmargin='0' bottommargin='0' leftmargin='12' rightmargin='0'>
<dmf:form>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr><td valign='top'>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr height='5'><td valign='top' colspan='2'></td></tr>
<tr height='1'><td valign='top' colspan='2'>
<dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE'/>
</td></tr>
<tr><td colspan='2'>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr>
<td style='padding-top : 5px;'>
<dmfx:componentinclude component='taskheader' name='taskheader'/>
</td>
</tr>
<tr height='5'><td>&nbsp;</td></tr>
</table>
</td></tr>
<tr>
<td valign=top>
<table width='100%' cellspacing='0' cellpadding='0' border='0' class='contentBackground'>
<tr><td align=center>
<dmf:tabbar name='tabs' tabposition='left' onclick='onTabSelected'/>
</td></tr>
</table>
</td>
<td valign='top' width='100%'>
<table width='100%' class='contentBackground'>
<tr><td valign=top>
<!-- TOP of white part of screen --><!-- MRCS error messages: --><blink><dmf:label name="mrcsvalidationerror" cssclass="validatorMessageStyle"/></blink><BR>
<BR>
<dmf:label name="mrcsanalysismessage" cssclass="validatorMessageStyle"/>
<table border="0"><tr><td><b><dmf:label name="EffectiveDateLabel" nlsid="EFFECTIVE_DATE"/></b></td><td><dmf:datetime name="EffectiveDate" enabled="true"/></td>
<td><dmf:button name='SaveNewEffDate' cssclass='buttonLink' nlsid='MSG_SAVE' onclick='onSaveDate' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_SAVE_EFF_TIP'/></td>
<td><dmf:button name='ReleaseImmediately' cssclass='buttonLink' nlsid='MSG_RELEASE' onclick='onReleaseImmediately' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_RELEASE_IMM_TIP'/></td>
</tr></table>
<dmfx:containerinclude/>
<!-- BOTTOM of white part of screen -->
<br>
</td></tr>
</table>
</td>
</tr>
<tr>
<td>&nbsp;</td>
<td><br>
<table align=left border='0' cellpadding='0' cellspacing='0'>
<tr>
<dmf:panel name='<%=TaskMgrContainer.WF_TASK_ACTION_BUTTONS_PANEL%>'>
<dmf:panel name='<%=TaskMgrContainer.ACCEPT_BUTTON_PANEL_CONTROL_NAME%>'>
<td>
<dmfx:actionbutton nlsid='MSG_ACCEPT_BUTTON' tooltipnlsid='MSG_ACCEPT_BUTTON_MOUSEOVER_INFO'
action='acceptworkflowtask' name='acceptworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton' oncomplete='onAcceptReturn'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canAcquireTask' contextvalue='canAcquireTask'/>
<dmfx:argument name='isGroupOwned' contextvalue='isGroupOwned'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.FINISH_BUTTON_PANEL_CONTROL_NAME%>'>
<td>
<dmfx:actionbutton nlsid='MSG_FINISH_BUTTON' tooltipnlsid='MSG_FINISH_BUTTON_MOUSEOVER_INFO'
action='finishworkflowtask' name='finishworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isManual' contextvalue='isManual'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.FORWARD_BUTTON_PANEL_CONTROL_NAME%>'>
<td>
<dmfx:actionbutton nlsid='MSG_FORWARD_BUTTON' tooltipnlsid='MSG_FORWARD_BUTTON_MOUSEOVER_INFO'
action='forwardworkflowtask' name='forwardworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isManual' contextvalue='isManual'/>
<dmfx:argument name='hasForwardPaths' contextvalue='hasForwardPaths'/>
<%-- Added by Sujeet on 03/22/2007 to fix the Defect 379 --%>
<dmfx:argument name='hasEmptyMandatoryPkg' contextvalue='hasEmptyMandatoryPkg'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.REJECT_BUTTON_PANEL_CONTROL_NAME%>'>
<td>
<dmfx:actionbutton nlsid='MSG_REJECT_BUTTON' tooltipnlsid='MSG_REJECT_BUTTON_MOUSEOVER_INFO'
action='rejectworkflowtask' name='rejectworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isManual' contextvalue='isManual'/>
<dmfx:argument name='hasRejectPaths' contextvalue='hasRejectPaths'/>
<%-- Added by Sujeet on 03/22/2007 to fix the Defect 379 --%>
<dmfx:argument name='hasEmptyMandatoryPkg' contextvalue='hasEmptyMandatoryPkg'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.DELEGATE_BUTTON_PANEL_CONTROL_NAME%>'>
<td>
<dmfx:actionbutton nlsid='MSG_DELEGATE_BUTTON' tooltipnlsid='MSG_DELEGATE_BUTTON_MOUSEOVER_INFO'
action='delegateworkflowtask' name='delegateworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isDelegable' contextvalue='isDelegable'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.REPEAT_BUTTON_PANEL_CONTROL_NAME%>'>
<td>
<dmfx:actionbutton nlsid='MSG_REPEAT_BUTTON' tooltipnlsid='MSG_REPEAT_BUTTON_MOUSEOVER_INFO'
action='repeatworkflowtask' name='repeatworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isRepeatable' contextvalue='isRepeatable'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.WF_AUTO_TASK_ACTION_BUTTONS_PANEL%>'>
<td>
<dmfx:actionbutton nlsid='MSG_RERUN_BUTTON' tooltipnlsid='MSG_RERUN_BUTTON_MOUSEOVER_INFO'
action='rerunfailedautoworkflowtask' name='rerunfailedautoworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
<td>
<dmfx:actionbutton nlsid='MSG_FORCE_COMPLETE_BUTTON' tooltipnlsid='MSG_FORCE_COMPLETE_BUTTON_MOUSEOVER_INFO'
action='completefailedautoworkflowtask' name='completefailedautoworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
<td>
<dmfx:actionbutton nlsid='MSG_ABORT_WORKFLOW_BUTTON' tooltipnlsid='MSG_ABORT_WORKFLOW_BUTTON_MOUSEOVER_INFO'
action='abortfailedautoworkflowtask' name='abortfailedautoworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.ROUTER_TASK_ACTION_BUTTONS_PANEL%>'>
<td>
<dmfx:actionbutton nlsid='MSG_FORWARD_BUTTON' tooltipnlsid='MSG_FORWARD_ROUTER_BUTTON_MOUSEOVER_INFO'
action='forwardroutertask' name='forwardworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
<td>
<dmfx:actionbutton nlsid='MSG_REJECT_BUTTON' tooltipnlsid='MSG_REJECT_ROUTER_BUTTON_MOUSEOVER_INFO'
action='rejectroutertask' name='rejectworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.NOTIFICATION_ACTION_BUTTONS_PANEL%>'>
<td>
<dmfx:actionbutton nlsid='MSG_DELETE_BUTTON' tooltipnlsid='MSG_DELETE_BUTTON_MOUSEOVER_INFO'
action='deletenotification' name='deletenotification_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='routerId' contextvalue='routerId'/>
</dmfx:actionbutton>
</td>
<td width=5></td>
</dmf:panel>
<td>
<dmf:button name='cancel' cssclass="buttonLink" nlsid='MSG_CLOSE' onclick='onClose'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CLOSE_TIP'/>
</td>
<td width=5></td>
<td>
<dmf:button name='<%=DialogContainer.CONTROL_HELPBUTTON %>' cssclass="buttonLink" nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_HELP_TIP'/>
</td>
</tr>
</table>
</td></tr>
<tr height='4'><td valign='bottom' colspan='2'></td></tr>
</table>
</td></tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
