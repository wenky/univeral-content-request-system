<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form, com.documentum.web.formext.component.DialogContainer" %>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmgrcontainer.TaskMgrContainer" %>
<dmf:html>
<dmf:head>
<dmf:webform/>

<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
</dmf:head>
<dmf:body id='contentArea' marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>
<dmf:form>
<dmf:paneset name="taskmanagercontainerPane" rows="144,*,36"  toppadding="0" leftpadding="0"
rightpadding="0" bottompadding="0" minheight="300" minwidth="500" cssclass="taskmanagerborder">
<dmf:pane name="taskmanagercontainer_headerarea" overflow="hidden">
<div class="modalnavbg tskMgrContainerLeftPadding" style="padding-top:8px;width:100%">
<dmf:tabbar name='tabs' align='center' tabposition='top' onclick='onTabSelected' />
</div>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr>
<td style='padding-top:5px;'>
<dmfx:componentinclude component='taskheader' name='taskheader'/>
</td>
</tr>
<tr height='5'><td>&nbsp;</td></tr>
</table>
</dmf:pane>
<dmf:pane name="taskmanagercontainer_contentarea">
<table border='0' cellpadding='0' cellspacing='0' width='100%'>
<tr valign='top'>
<td valign='top' class="leftAlignment" width='100%'>
<blink><dmf:label name="mdtvalidationerror" cssclass="validatorMessageStyle"/></blink><BR>
<dmfx:containerinclude/>
</td>
</tr>
</table>
</dmf:pane>
<dmf:pane name="taskmanagercontainer_buttonarea" overflow="hidden">
<table width='100%' cellspacing='0' cellpadding='0' border='0' class="modalnavbg"
style="padding-top:10px;padding-bottom:10px;">
<tr>
<td width="99%">&nbsp;</td>
<dmf:panel name='<%=TaskMgrContainer.WF_TASK_ACTION_BUTTONS_PANEL%>'>
<dmf:panel name='<%=TaskMgrContainer.PULLTASK_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtPulltaskButton">
  <dmfx:actionbutton nlsid='MSG_PULLTASK_BUTTON' tooltipnlsid='MSG_PULLTASK_BUTTON_MOUSEOVER_INFO'
  action='pull_queued_task' name='pulltask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
    <dmfx:argument name='performer' contextvalue='performer'/>
    <dmfx:argument name='queueName' contextvalue='queueName'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.ASSIGN_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtAssignButton">
  <dmfx:actionbutton nlsid='MSG_ASSIGN_BUTTON' tooltipnlsid='MSG_ASSIGN_BUTTON_MOUSEOVER_INFO'
  action='assign_queued_task' name='reassigntask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
    <dmfx:argument name='performer' contextvalue='performer'/>
    <dmfx:argument name='queueName' contextvalue='queueName'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.ACCEPT_BUTTON_PANEL_CONTROL_NAME%>'>
<td class="leftAlignment" nowrap>
<span id="MdtAcceptButton">
  <dmfx:actionbutton nlsid='MSG_ACCEPT_BUTTON' tooltipnlsid='MSG_ACCEPT_BUTTON_MOUSEOVER_INFO'
  action='acceptworkflowtask' name='acceptworkflowtask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton' oncomplete='onAcceptReturn'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='canAcquireTask' contextvalue='canAcquireTask'/>
    <dmfx:argument name='isGroupOwned' contextvalue='isGroupOwned'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.FINISH_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtFinishButton">
  <dmfx:actionbutton nlsid='MSG_FINISH_BUTTON' tooltipnlsid='MSG_FINISH_BUTTON_MOUSEOVER_INFO'
  action='finishworkflowtask' name='finishworkflowtask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
    <dmfx:argument name='isManual' contextvalue='isManual'/>
    <dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
    <dmfx:argument name='hasEmptyMandatoryPkg' contextvalue='hasEmptyMandatoryPkg'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.FORWARD_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtForwardButton">
  <dmfx:actionbutton nlsid='MSG_FORWARD_BUTTON' tooltipnlsid='MSG_FORWARD_BUTTON_MOUSEOVER_INFO'
  action='forwardworkflowtask' name='forwardworkflowtask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
    <dmfx:argument name='isManual' contextvalue='isManual'/>
    <dmfx:argument name='hasForwardPaths' contextvalue='hasForwardPaths'/>
    <dmfx:argument name='hasEmptyMandatoryPkg' contextvalue='hasEmptyMandatoryPkg'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.REJECT_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtRejectButton">
  <dmfx:actionbutton nlsid='MSG_REJECT_BUTTON' tooltipnlsid='MSG_REJECT_BUTTON_MOUSEOVER_INFO'
  action='rejectworkflowtask' name='rejectworkflowtask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
    <dmfx:argument name='isManual' contextvalue='isManual'/>
    <dmfx:argument name='hasRejectPaths' contextvalue='hasRejectPaths'/>
    <dmfx:argument name='hasEmptyMandatoryPkg' contextvalue='hasEmptyMandatoryPkg'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.DELEGATE_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtDelegateButton">
  <dmfx:actionbutton nlsid='MSG_DELEGATE_BUTTON' tooltipnlsid='MSG_DELEGATE_BUTTON_MOUSEOVER_INFO'
  action='delegateworkflowtask' name='delegateworkflowtask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
    <dmfx:argument name='isDelegable' contextvalue='isDelegable'/>
    <dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.REPEAT_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtRepeatButton"></span>
  <dmfx:actionbutton nlsid='MSG_REPEAT_BUTTON' tooltipnlsid='MSG_REPEAT_BUTTON_MOUSEOVER_INFO'
  action='repeatworkflowtask' name='repeatworkflowtask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
    <dmfx:argument name='isRepeatable' contextvalue='isRepeatable'/>
    <dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
  </dmfx:actionbutton>
</span>  
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.SUSPEND_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtSuspendButton">
  <dmfx:actionbutton nlsid='MSG_SUSPEND_BUTTON' tooltipnlsid='MSG_SUSPEND_BUTTON_MOUSEOVER_INFO'
  action='suspend_queued_task' name='suspendtask_button' showifdisabled='false'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.UNSUSPEND_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtUnsuspendButton">
  <dmfx:actionbutton nlsid='MSG_UNSUSPEND_BUTTON' tooltipnlsid='MSG_UNSUSPEND_BUTTON_MOUSEOVER_INFO'
  action='unsuspend_queued_task' name='unsuspendtask_button' showifdisabled='false'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.REASSIGN_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtReassignButton">
  <dmfx:actionbutton nlsid='MSG_REASSIGN_BUTTON' tooltipnlsid='MSG_REASSIGN_BUTTON_MOUSEOVER_INFO'
  action='reassign_queued_task' name='reassigntask_button' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
    <dmfx:argument name='performer' contextvalue='performer'/>
    <dmfx:argument name='queueName' contextvalue='queueName'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.UNASSIGN_BUTTON_PANEL_CONTROL_NAME%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtUnassignButton">
  <dmfx:actionbutton nlsid='MSG_UNASSIGN_BUTTON' tooltipnlsid='MSG_UNASSIGN_BUTTON_MOUSEOVER_INFO'
  action='unassign_queued_task' name='unassigntaskbutton' showifdisabled='true'
  cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
    <dmfx:argument name='type' contextvalue='type'/>
    <dmfx:argument name='objectId' contextvalue='objectId'/>
    <dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
    <dmfx:argument name='performer' contextvalue='performer'/>
    <dmfx:argument name='queueName' contextvalue='queueName'/>
  </dmfx:actionbutton>
</span>
</td>
</dmf:panel>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.WF_AUTO_TASK_ACTION_BUTTONS_PANEL%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtAutoRerunButton">
<dmfx:actionbutton nlsid='MSG_RERUN_BUTTON' tooltipnlsid='MSG_RERUN_BUTTON_MOUSEOVER_INFO'
action='rerunfailedautoworkflowtask' name='rerunfailedautoworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</span>
</td>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtAutoForceCompleteButton">
<dmfx:actionbutton nlsid='MSG_FORCE_COMPLETE_BUTTON' tooltipnlsid='MSG_FORCE_COMPLETE_BUTTON_MOUSEOVER_INFO'
action='completefailedautoworkflowtask' name='completefailedautoworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</span>
</td>
<td  nowrap class="buttonbuffer leftAlignment">
<span id="MdtAbortWorkflowButton">
<dmfx:actionbutton nlsid='MSG_ABORT_WORKFLOW_BUTTON' tooltipnlsid='MSG_ABORT_WORKFLOW_BUTTON_MOUSEOVER_INFO'
action='abortfailedautoworkflowtask' name='abortfailedautoworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
<dmfx:argument name='isFailedAutoTask' contextvalue='isFailedAutoTask'/>
</dmfx:actionbutton>
</span>
</td>
</dmf:panel>
<dmf:panel name='<%=TaskMgrContainer.ROUTER_TASK_ACTION_BUTTONS_PANEL%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<span></span>
<dmfx:actionbutton nlsid='MSG_FORWARD_BUTTON' tooltipnlsid='MSG_FORWARD_ROUTER_BUTTON_MOUSEOVER_INFO'
action='forwardroutertask' name='forwardworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
</dmfx:actionbutton>
</td>
<td  nowrap class="buttonbuffer leftAlignment">
<span></span>
<dmfx:actionbutton nlsid='MSG_REJECT_BUTTON' tooltipnlsid='MSG_REJECT_ROUTER_BUTTON_MOUSEOVER_INFO'
action='rejectroutertask' name='rejectworkflowtask_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='canCompleteTask' contextvalue='canCompleteTask'/>
</dmfx:actionbutton>
</td>
</dmf:panel>
<span></span>
<dmf:panel name='<%=TaskMgrContainer.NOTIFICATION_ACTION_BUTTONS_PANEL%>'>
<td  nowrap class="buttonbuffer leftAlignment">
<dmfx:actionbutton nlsid='MSG_DELETE_BUTTON' tooltipnlsid='MSG_DELETE_BUTTON_MOUSEOVER_INFO'
action='deletenotification' name='deletenotification_button' showifdisabled='true'
cssclass="buttonLink" height='16' imagefolder='images/dialogbutton'>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='routerId' contextvalue='routerId'/>
</dmfx:actionbutton>
</td>
</dmf:panel>
<td  class="buttonbuffer lastbutton rightAlignment">
<dmf:button name='cancel' cssclass="buttonLink" nlsid='MSG_CLOSE' onclick='onClose'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CLOSE_TIP'/>
</td>
</tr>
</table>
</dmf:pane>
</dmf:paneset>
</dmf:form>
</dmf:body>
</dmf:html>
