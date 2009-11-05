<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.web.form.control.databound.Datagrid" %>
<%@ page import="com.documentum.web.form.control.databound.DataProvider" %>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmanager.RejectWorkflowTask" %>
<%@ page import="com.medtronic.ecm.documentum.mdtworkflow.webtop.MdtRejectTask" %>
<dmf:webform/>
<dmf:form>
<table width='100%'>
<tr><td class="taskManagerPagePadding">
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<tr>
<td colspan='2' class='taskmgrSectionHeading' height='24'>
<dmf:label name='<%=RejectWorkflowTask.MSG_REJECT_CONTROL_NAME%>'/>
</td>
</tr>
<tr>
<td colspan='2'>
<dmf:datagrid name='<%=RejectWorkflowTask.NEXT_TASKS_GRID_CONTROL_NAME%>' paged='false'
preservesort='false' cellspacing='0' cellpadding='0' bordersize='0'
style='padding:0px' cssclass='doclistbodyDatagrid'>
<%
Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
DataProvider provider = ((Datagrid)form.getControl(RejectWorkflowTask.NEXT_TASKS_GRID_CONTROL_NAME)).getDataProvider();
//
%>
<td nowrap width='100' height='24'>
<u><dmf:label nlsid='MSG_REJECT_CHECK_HDR'/></u>
</td>
<td nowrap width='200'>
<dmf:datasortlink name='sort_object_name' nlsid='MSG_TASK_NAME_HDR' column='object_name' />
</td>
<td>
<dmf:datasortlink name='sort_performer_name' nlsid='MSG_NEXT_TASK_PERFORMER_NAME' column='performer_name' />
</td>
<dmf:datagridRow name='nexttaskscolumns' height='24' cssclass='contentBackground'>
<td>
<dmf:checkbox name='<%=RejectWorkflowTask.NEXT_TASKS_CHECKBOX_CONTROL_NAME + provider.getDataField("r_object_id")%>'/>
</td>
<td scope="row">
<dmf:label datafield='object_name'/>
</td>
<td>
<dmf:label datafield='performer_name'/>
</td>
</dmf:datagridRow>
</dmf:datagrid>
</td>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
<dmf:panel name='<%=RejectWorkflowTask.SIGNOFF_PANEL_CONTROL_NAME%>'>
<tr>
<td colspan='2' height='24' class='taskmgrSectionHeading' style='padding-top: 10px; padding-bottom: 5px'>
<dmf:label nlsid='MSG_SIGNOFF_REQUIRED_HDR'/>
</td>
</tr>
<tr>
<!--  begin custom reject reason text entry (modified with a table to line up reason and password fields btw -->
<td>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<tr>
  <td><dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_DOCREJECT_REASON"/></td>
  <td>
    <dmf:text name="<%=MdtRejectTask.DOCREJECT_RSNTXT_CONTROL_NAME%>"  id = "<%=MdtRejectTask.DOCREJECT_RSNTXT_CONTROL_NAME%>" size="40" tooltipnlsid='MSG_DOCREJECT_REASON' />
    <dmf:requiredfieldvalidator name="rjtrsnval" controltovalidate= "<%=MdtRejectTask.DOCREJECT_RSNTXT_CONTROL_NAME%>" nlsid= "MSG_DOCREJECT_REASON_REQ" />
  </td>
</tr>
<tr>
  <td><dmf:label nlsid='MSG_PASSWORD_FIELD_LABEL'/></td>
  <td>
    <dmf:password name='<%=RejectWorkflowTask.PASSWORD_CONTROL_NAME%>' defaultonenter='true' tooltipnlsid='MSG_PASSWORD_FIELD_LABEL'/>
    <%-- invisible button used here as a proxy for handler invocation--%>
    <dmf:button label='default' visible='false' enabled='true' default='true' onclick='onAttemptFinish'/>
  </td>
</tr>
</table>
</td>
<!--  end custom reject reason text entry and layout -->

<%--  comment out old control
<td>
<dmf:label nlsid='MSG_PASSWORD_FIELD_LABEL'/>
&nbsp;&nbsp;<dmf:password name='<%=RejectWorkflowTask.PASSWORD_CONTROL_NAME%>' defaultonenter='true' tooltipnlsid='MSG_PASSWORD_FIELD_LABEL'/>
<!-- invisible button used here as a proxy for handler invocation-->
<dmf:button label='default' visible='false' enabled='true' default='true' onclick='onAttemptFinish'/>
</td>
--%>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
</dmf:panel>
</table>
</td></tr>
</table>
</dmf:form>
