<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.services.workflow.inbox.IInbox" %>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmanager.TaskManager" %>
<dmf:webform/>
<dmf:form>
<%
TaskManager form = (TaskManager)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
int iTaskType = form.getTaskType();
//
%>

<table border='0' cellpadding="0" cellspacing="0">
<tr>
  <dmf:panel name='<%=TaskManager.REQUIREMENTS_HEADER_PANEL_CONTROL_NAME%>'>
    <td style="padding-left: 24px;padding-top: 10px;" class="subTitle"><dmf:label nlsid='MSG_REQUIREMENTS_HEADER'/>:</td>
  </dmf:panel>
  <td style="padding-left: 10px;padding-top: 10px;" class="subTitle"><dmf:label nlsid='MSG_INSTRUCTIONS_HEADER'/>:</td>
</tr>
<tr>
  <dmf:panel name='<%=TaskManager.REQUIREMENTS_PANEL_CONTROL_NAME%>'>
    <td valign="top" style="padding-left: 24px;">
    <dmf:datagrid name='<%=TaskManager.REQUIREMENTS_GRID_CONTROL_NAME%>' paged='false' preservesort='false' cellspacing='0' cellpadding='0' bordersize='0' cssclass='defaultDatagridRowAltStyle'>
      <dmf:datagridRow name='requirementsrow'  cssclass='defaultDatagridRowAltStyle' altclass="defaultDatagridRowAltStyle">
        <td scope="row" valign="top"><dmf:label datafield='requirement' cssclass='defaultDatagridRowAltStyle'/></td>
      </dmf:datagridRow>
      <dmf:nodataRow cssclass='defaultDatagridRowAltStyle' >
        <td><i><dmf:label nlsid='MSG_NO_REQUIREMENTS' cssclass='defaultDatagridRowAltStyle'/></i></td>
      </dmf:nodataRow>
    </dmf:datagrid>
    </td>
  </dmf:panel>
  <td scope="row">
    <%-- the area below will sometimes have scroll bars if it overflows--%>
    <div style="overflow:auto;padding:10px">
      <dmf:htmlsafetextformatter>
        <dmf:label name='<%=TaskManager.TASK_INSTRUCTIONS_CONTROL_NAME%>' cssclass='defaultDatagridRowAltStyle'/>
      </dmf:htmlsafetextformatter>
    </div>
  </td>
</tr>
<%
if (iTaskType == IInbox.DF_WORKFLOWTASK && form.isUserTimeCostSupportedAndUsed() == true)
{
%>
<tr>
  <td colspan="2" style="padding-left: 42px;padding-top: 5px;padding-bottom:5px;">
    <table border="0" cellpadding='0' cellspacing='0'>
    <tr>
     <td class="subTitle" nowrap><dmf:label nlsid='MSG_USER_TIME_HEADER' cssclass="subTitle"/>:&nbsp;</td>
     <td align="left" nowrap style="padding-bottom: 3px;"><dmf:text name="<%=TaskManager.USER_TIME_NAME_TEXT_CONTROL_NAME%>"/></td>
    </tr>
    <tr>
      <td class="subTitle" nowrap><dmf:label nlsid='MSG_USER_COST_HEADER' cssclass="subTitle"/>:</td>
      <td align="left" width="10" nowrap><dmf:text name="<%=TaskManager.USER_COST_NAME_TEXT_CONTROL_NAME%>"/></td>
    </tr>
    </table>
  </td>
</tr>
<%
}
%>
</table>

<table cellpadding="0" cellspacing="0" border="0" width="100%">
<%
if(iTaskType != IInbox.DF_NOTIFICATION)
{
//
%>
<tr>
  <td width="100%">
    <dmfx:xforms name='<%=TaskManager.XFORM_CONTROL_NAME%>'/>
  </td>
</tr>
<tr>
  <td width="100%">
    <dmfx:componentinclude page='list' component='taskattachment' name='<%=TaskManager.ATTACHMENT_COMPONENT_INCLUDE_CONTROL_NAME%>'/>
  </td>
</tr>
<%
}
//
%>
</table>

<!-- /td></tr>
</table  -->
</dmf:form>
