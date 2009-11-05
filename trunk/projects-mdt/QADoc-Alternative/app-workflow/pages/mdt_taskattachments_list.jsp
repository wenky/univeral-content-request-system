<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.services.workflow.inbox.IInbox" %>
<%@ page import="com.documentum.web.form.control.databound.Datagrid"%>
<%@ page import="com.documentum.webcomponent.library.workflow.attachment.TaskAttachment" %>
<%@ page import="com.documentum.web.form.control.databound.IDataboundParams" %>
<dmf:webform/>
<dmf:form>
<%
TaskAttachment form = (TaskAttachment)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
//
%>
<dmfx:actionmultiselect name='multiselect'>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>

<!-- CEM: BEGIN form header -->
<tr class="pagerBackground">
<th scope="col" align="center" height="24">
<dmf:label nlsid='MSG_FORM_HEADER' cssclass="subTitle"/>
<%
if (form.getTaskType() == IInbox.DF_WORKFLOWTASK && form.isAddWfAttachmentSupported() == true)
{
//
%>
&nbsp;
<!-- CEM: removing add link -->
<!-- dmfx:actionlink name='addWfAttachmentImg' nlsid='MSG_ADD_ATTACHMENT' action='addwfattachment' oncomplete='onComplete' showifdisabled='false' tooltipnlsid='MSG_ADD_ATTACHMENT'/ -->
<%
}
//
%>
</th>
</tr>
<!-- CEM: END form header -->


<tr>
<td class="contentBackground spacer">
&nbsp;
</td>
</tr>
<tr>
<td width='100%'>
<dmf:datagrid name='<%=TaskAttachment.ATTACHMENT_GRID_CONTROL_NAME%>' paged='true' pagesize='10'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0'
cssclass='doclistbodyDatagrid'>
<%
if(((Datagrid)form.getControl(TaskAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getResultsCount() != 0)
{
// CEM: has workflow packages (i.e. the form doctype)
%>
  <%
  // avoid rendering the row if the pager doesn't appear
  if(form.getAttachmentCount() > ((Datagrid)form.getControl(TaskAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getPageSize())
  {
  %>
    <tr class='pagerBackground'><td colspan='20' height="24" align='center'><dmf:datapaging name='attachpager'/></td></tr>
    <tr><td colspan="20" class="contentBackground spacer">&nbsp;</td></tr>
  <%
  }
%>
<tr height='24' class="colHeaderBackground">
  <th scope='col'  nowrap width='16' class="doclistcheckbox leftAlignment"><dmfx:actionmultiselectcheckall/></th>
  <th scope='col' class="leftAlignment" nowrap width="20">&nbsp;</th>
  <th scope='col' class="leftAlignment" nowrap width="20">&nbsp;</th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid leftAlignment'><dmf:datasortlink name='sort_object_name' nlsid='MSG_DOC_NAME' column='object_name' /></th>
  <th scope='col' class="leftAlignment" nowrap><dmf:image name='prop' nlsid='MSG_PROPERTIES' src='images/space.gif'/></th>
  <th scope='col'  class='doclistfilenamedatagrid leftAlignment' nowrap><dmf:datasortlink name='sort_a_content_type' nlsid='MSG_DOC_FORMAT' column='a_content_type' /></th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid leftAlignment'><dmf:datasortlink name='sort_r_modify_date' nlsid='MSG_DOC_MODIFIED' column='r_modify_date' /></th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid rightAlignment' width="99%"><dmf:label nlsid='MSG_PACKAGE_ACTIONS_HDR'/></th>
</tr>
<%
}
else
{
// CEM: no workflow packages (i.e. no form object attached)
%>
<tr height='24' class="colHeaderBackground">
  <th scope='col' class="leftAlignment" nowrap width='16'>&nbsp;</th>
  <th scope='col' class="leftAlignment" nowrap width='20'>&nbsp;</th>
  <th scope='col' class="leftAlignment" nowrap width='20'>&nbsp;</th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid leftAlignment' width="200"><dmf:label nlsid='MSG_DOC_NAME'/></th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid rightAlignment' width="99%"><dmf:label nlsid='MSG_PACKAGE_ACTIONS_HDR'/></th>
</tr>
<%
}
//
%>
<dmf:datagridRow name='attachcolumns' height='24' cssclass='defaultDatagridRowStyle'
altclass="defaultDatagridRowAltStyle">
<td nowrap width='16' class="doclistcheckbox">
<dmfx:actionmultiselectcheckbox name='check' value='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='lockOwner' datafield='r_lock_owner'/>
<dmf:argument name='ownerName' datafield='owner_name'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmf:argument name='isVirtualDoc' datafield='r_is_virtual_doc'/>
<dmf:argument name='linkCount' datafield='r_link_cnt'/>
<dmf:argument name='startworkflowId' value='startworkflow'/>
<dmf:argument name='isReference' datafield='i_is_reference'/>
<dmf:argument name='isReplica' datafield='i_is_replica'/>
<dmf:argument name='assembledFromId' datafield='r_assembled_from_id'/>
<dmf:argument name='isFrozenAssembly' datafield='r_has_frzn_assembly'/>
<dmf:argument name='compoundArchitecture' datafield='a_compound_architecture'/>
<dmf:argument name='preferForms' datafield='useFormForProperties'/>
<dmf:argument name='templateId' datafield='formTemplateId'/>
<dmf:argument name='events' datafield='events'/>
<dmf:argument name='notificationStatus' datafield='notification_status'/>
</dmfx:actionmultiselectcheckbox>
</td>
<td nowrap width='20'>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
</td>
<td nowrap width='20' class="doclistlocicon">
<dmfx:docbaseicon name='attachment_icon' formatdatafield='a_content_type' typedatafield='r_object_type'
linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc'
assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly'
isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16'/>
</td>
<td scope='row' class='doclistfilenamedatagrid' width="200">
<dmf:stringlengthformatter maxlen='16' wrapped='true' postfix='...'>
<%
if ( ((Datagrid)form.getControl(TaskAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getDataField("r_object_id").equals(form.getShowingFormId()) )
{
//
%>
<dmf:label datafield='object_name' cssclass='drilldownFileName'/>
<%
} else {
%>             <dmf:datagridRowEvent eventname="dblclick">
<dmf:link onclick='onViewAttachment' datafield='object_name' cssclass='drilldownFileName' name='lblObjName'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmf:argument name='isVirtualDoc' datafield='r_is_virtual_doc'/>
<dmf:argument name='linkCount' datafield='r_link_cnt'/>
<dmf:argument name='lockOwner' datafield='r_lock_owner'/>
<dmf:argument name="contentType" datafield='a_content_type'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
<dmf:argument name='assembledFromId' datafield='r_assembled_from_id'/>
<dmf:argument name='isFrozenAssembly' datafield='r_has_frzn_assembly'/>
<dmf:argument name='compoundArchitecture' datafield='a_compound_architecture'/>
<dmf:argument name = 'inline' value='false'/>
</dmf:link>
</dmf:datagridRowEvent>
<%
}
//
%>
</dmf:stringlengthformatter>
</td>
<td width="18">
<dmf:datagridRowModifier>
<dmfx:actionimage name='propertiesimage' nlsid='MSG_PROPERTIES' action='properties' src='icons/info.gif' showifdisabled='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='preferForms' datafield='useFormForProperties'/>
<dmf:argument name='templateId' datafield='formTemplateId'/>
</dmfx:actionimage>
</dmf:datagridRowModifier>
</td>
<td nowrap class='doclistfilenamedatagrid leftAlignment'  width="75">
<dmfx:docformatvalueformatter>
<dmf:label datafield='a_content_type'/>
</dmfx:docformatvalueformatter>
</td>
<td nowrap class='doclistfilenamedatagrid' width="200">
<dmf:datevalueformatter>
<dmf:label datafield='r_modify_date'/>
</dmf:datevalueformatter>
</td>
<td class="rightAlignment" width="99%">
<!-- CEM: removing attachment Remove link/command (For Form table?)
dmfx:actionlink action='removeattachment' nlsid="MSG_REMOVE_ATTACHMENT" name='actionimage_removeattachment' showifdisabled='false' oncomplete="onComplete">
dmfx:argument name='objectId' contextvalue='queueItemId'/>
dmf:argument name='type' value='dmi_package'/>
dmf:argument name='packageName' datafield='packageName'/>
dmf:argument name='attachmentName' datafield='object_name'/>
dmf:argument name='isPackageManufactured' datafield='isPackageManufactured'/>
dmf:argument name='docId' datafield='r_object_id'/>
/dmfx:actionlink
-->

</td>
</dmf:datagridRow>
</dmf:datagrid>
</td>
</tr>

<!-- CEM: BEGIN attachments header -->
<tr class="pagerBackground">
<th scope="col" align="center" height="24">
<dmf:label nlsid='MSG_ATTACHMENTS_HEADER' cssclass="subTitle"/>
<%
if (form.getTaskType() == IInbox.DF_WORKFLOWTASK && form.isAddWfAttachmentSupported() == true)
{
//
%>
&nbsp;
<!-- CEM: removing add link -->
<!-- dmfx:actionlink name='addWfAttachmentImg' nlsid='MSG_ADD_ATTACHMENT' action='addwfattachment' oncomplete='onComplete' showifdisabled='false' tooltipnlsid='MSG_ADD_ATTACHMENT'/ -->
<%
}
//
%>
</th>
</tr>
<!-- CEM: END attachments header -->

<!-- CEM: BEGIN dupe spacer row of form header -->
<tr>
<td class="contentBackground spacer">
&nbsp;
</td>
</tr>
<tr>
<!-- CEM: END dupe spacer row of form header -->

<!-- CEM: BEGIN Don't know what these missing/empty attachment grids are for ?BPM? but we don't care about them -->
<%
if(form.getAttachmentErrorCount() > 0)
{
//
%>
<tr><td colspan='10' class='taskmgrErrorSectionHeading'>
<dmf:label nlsid='MSG_ERROR_ATTACHMENTS_HEADER'/>
</td>
</tr>
<tr><td colspan='10' valign='middle' class='taskmgrErrorSection'>
<dmf:label name='<%=TaskAttachment.ATTACHMENT_ERROR_LABEL_CONTROL_NAME_1%>'/>
&nbsp;
<dmf:label name='<%=TaskAttachment.ATTACHMENT_ERROR_LABEL_CONTROL_NAME_2%>'/>
</td>
</tr>
<%
}
if(form.getTaskType() == IInbox.DF_WORKFLOWTASK && form.getEmptyAttachmentCount() > 0)
{
//
%>
<tr><td width='100%'>
<dmf:datagrid name='<%=TaskAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME%>' paged='false'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0'
sortcolumn='packageName' sortdir='<%=Integer.toString(IDataboundParams.SORTDIR_FORWARD)%>'>
<dmf:datagridRow name='missingattachcolumns' height='24'
cssclass='defaultDatagridRowAltStyle'
altclass="defaultDatagridRowStyle">
<td nowrap width='16'>&nbsp;</td>
<td nowrap width='20'>&nbsp;</td>
<td nowrap width='20'>
<dmfx:docbaseicon name='icon' type='dm_package' size='16'/>
</td>
<td nowrap class='doclistfilenamedatagrid' width="200">
<dmf:datagridRowEvent eventname="dblclick">
<dmf:link onclick='onSelectEmptyAttachment' enabled="<%=form.isEmptyPackageLinkEnabled()%>" datafield='packageName' cssclass='drilldownFileName'>
<dmfx:argument name='objectId' contextvalue='queueItemId'/>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='type' value='dmi_package'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmfx:argument name='formSaveFolderPath' contextvalue='formsavefolderpath'/>
<dmfx:argument name='userDefaultFormACL' contextvalue='defaultformacl'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
<dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
</dmf:link>
</dmf:datagridRowEvent>
&nbsp;
<dmf:label datafield='optionalLabel'/>
</td>
<%
// avoid rendering the row if the pager doesn't appear
String formTemplateId = ((Datagrid)form.getControl(TaskAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getDataField("formTemplateId");
String formForProperties = ((Datagrid)form.getControl(TaskAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getDataField("useFormForProperties");
if (formForProperties != null && formForProperties.equals("false") &&
formTemplateId != null && formTemplateId.length() > 0)
{
//
%>
<td valign='top' class="rightAlignment">
<dmf:image  onclick='onNewFormAttachment' src='icons/edit.gif'>
<dmfx:argument name='objectId' contextvalue='queueItemId'/>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='type' value='dmi_package'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmfx:argument name='formSaveFolderPath' contextvalue='formsavefolderpath'/>
<dmfx:argument name='userDefaultFormACL' contextvalue='defaultformacl'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
</dmf:image>
</td>
<%
}
//
%>
<td width="99%" class="rightAlignment">
<dmfx:actionlink action='addattachment'
name='actionimage_addattachment' showifdisabled='true' nlsid='MSG_ADD_ATTACHMENT_TOOLTIP' tooltipnlsid='MSG_ADD_ATTACHMENT_TOOLTIP'>
<dmfx:argument name='objectId' contextvalue='queueItemId'/>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='type' value='dmi_package'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
<dmfx:argument name='runtimeState' contextvalue='runtimeState'/>
</dmfx:actionlink>
<td>
</dmf:datagridRow>
</dmf:datagrid>
</td></tr>
<%
}
//
%>
<!-- CEM: END Don't know what these missing/empty attachment grids are for ?BPM? but we don't care about them -->


<!-- CEM: BEGIN Attachments grid -->
<%
if (form.getTaskType() == IInbox.DF_WORKFLOWTASK && form.isAddWfAttachmentSupported() == true && form.getWorkflowAttachmentCount() > 0)
{
//
%>
<tr>
<td width='100%'>
<dmf:datagrid name='<%=TaskAttachment.WF_ATTACHMENT_GRID_CONTROL_NAME%>' paged='true' pagesize='10'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0'>

<!--  CEM: BEGIN ATTACHMENTS TABLE HEADER ROW --> 
<%
if(((Datagrid)form.getControl(TaskAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getResultsCount() != 0)
{
// CEM: has workflow packages (i.e. the form doctype)
%>
  <%
  // avoid rendering the row if the pager doesn't appear
  if(form.getAttachmentCount() > ((Datagrid)form.getControl(TaskAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getPageSize())
  {
  %>
    <tr class='pagerBackground'><td colspan='20' height="24" align='center'><dmf:datapaging name='attachpager'/></td></tr>
    <tr><td colspan="20" class="contentBackground spacer">&nbsp;</td></tr>
  <%
  }
%>
<tr height='24' class="colHeaderBackground">
  <th scope='col'  nowrap width='16' class="doclistcheckbox leftAlignment"><dmfx:actionmultiselectcheckall/></th>
  <th scope='col' class="leftAlignment" nowrap width="20">&nbsp;</th>
  <th scope='col' class="leftAlignment" nowrap width="20">&nbsp;</th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid leftAlignment'><dmf:datasortlink name='sort_object_name' nlsid='MSG_DOC_NAME' column='object_name' /></th>
  <th scope='col' class="leftAlignment" nowrap><dmf:image name='prop' nlsid='MSG_PROPERTIES' src='images/space.gif'/></th>
  <th scope='col'  class='doclistfilenamedatagrid leftAlignment' nowrap><dmf:datasortlink name='sort_a_content_type' nlsid='MSG_DOC_FORMAT' column='a_content_type' /></th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid leftAlignment'><dmf:datasortlink name='sort_r_modify_date' nlsid='MSG_DOC_MODIFIED' column='r_modify_date' /></th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid rightAlignment' width="99%"><dmf:label nlsid='MSG_PACKAGE_ACTIONS_HDR'/></th>
</tr>
<%
}
else
{
// CEM: no workflow packages (i.e. no form object attached)
%>
<tr height='24' class="colHeaderBackground">
  <th scope='col' class="leftAlignment" nowrap width='16'>&nbsp;</th>
  <th scope='col' class="leftAlignment" nowrap width='20'>&nbsp;</th>
  <th scope='col' class="leftAlignment" nowrap width='20'>&nbsp;</th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid leftAlignment' width="200"><dmf:label nlsid='MSG_DOC_NAME'/></th>
  <th scope='col'  nowrap class='doclistfilenamedatagrid rightAlignment' width="99%"><dmf:label nlsid='MSG_PACKAGE_ACTIONS_HDR'/></th>
</tr>
<%
}
//
%>
<!--  CEM: END ATTACHMENTS TABLE HEADER ROW --> 


<dmf:datagridRow name='attachcolumns' height='24' cssclass='defaultDatagridRowAltStyle' altclass="defaultDatagridRowStyle">
<td nowrap width='16' class="doclistcheckbox">
<dmfx:actionmultiselectcheckbox name='check' value='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='lockOwner' datafield='r_lock_owner'/>
<dmf:argument name='ownerName' datafield='owner_name'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmf:argument name='isVirtualDoc' datafield='r_is_virtual_doc'/>
<dmf:argument name='linkCount' datafield='r_link_cnt'/>
<dmf:argument name='startworkflowId' value='startworkflow'/>
<dmf:argument name='isReference' datafield='i_is_reference'/>
<dmf:argument name='isReplica' datafield='i_is_replica'/>
<dmf:argument name='assembledFromId' datafield='r_assembled_from_id'/>
<dmf:argument name='isFrozenAssembly' datafield='r_has_frzn_assembly'/>
<dmf:argument name='compoundArchitecture' datafield='a_compound_architecture'/>
</dmfx:actionmultiselectcheckbox>
</td>
<td nowrap width='20' class="doclistlocicon">
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
</td>
<td nowrap width="20" class="doclistlocicon">
<dmfx:docbaseicon name='wfattachment_icon' formatdatafield='a_content_type' typedatafield='r_object_type'
linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc'
assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly'
isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16'/>
</td>
<td scope='row' nowrap class='doclistfilenamedatagrid' width="200">
<dmf:stringlengthformatter maxlen='16' wrapped='true' postfix='...'>
<dmf:datagridRowEvent eventname="dblclick">
<dmf:link onclick='onViewAttachment' datafield='object_name' cssclass='drilldownFileName' name='lblWfObjName'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmf:argument name='isVirtualDoc' datafield='r_is_virtual_doc'/>
<dmf:argument name='linkCount' datafield='r_link_cnt'/>
<dmf:argument name='lockOwner' datafield='r_lock_owner'/>
<dmf:argument name="contentType" datafield='a_content_type'/>
<dmf:argument name='assembledFromId' datafield='r_assembled_from_id'/>
<dmf:argument name='isFrozenAssembly' datafield='r_has_frzn_assembly'/>
<dmf:argument name='compoundArchitecture' datafield='a_compound_architecture'/>
<dmf:argument name = 'inline' value='false'/>
</dmf:link>
</dmf:datagridRowEvent>
</dmf:stringlengthformatter>
</td>
<td width="18">
<dmf:datagridRowModifier>
<dmfx:actionimage name='propertiesimage' nlsid='MSG_PROPERTIES' action='properties' src='icons/info.gif' showifdisabled='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmfx:actionimage>
</dmf:datagridRowModifier>
</td>
<td nowrap class='doclistfilenamedatagrid leftAlignment'  width="75">
<dmfx:docformatvalueformatter>
<dmf:label datafield='a_content_type'/>
</dmfx:docformatvalueformatter>
</td>
<td nowrap class='doclistfilenamedatagrid' width="200">
<dmf:datevalueformatter>
<dmf:label datafield='r_modify_date'/>
</dmf:datevalueformatter>
</td>
<td class="rightAlignment"  width="99%">
<!-- CEM: eliminating remove action (for attachments grid?)  
dmfx:actionlink action='removewfattachment' nlsid="MSG_REMOVE_ATTACHMENT" name='actionimage_removewfattachment' tooltipnlsid='MSG_REMOVE_ATTACHMENT' >
dmfx:argument name='objectId' contextvalue='queueItemId'/>
dmf:argument name='type' value='dmi_wf_attachment'/>
dmf:argument name='wf_attachment_id' datafield='wf_attachment_id'/>
dmf:argument name='attachmentName' datafield='object_name'/>
dmf:argument name='docId' datafield='r_object_id'/>
/dmfx:actionlink>
-->
</td>
</dmf:datagridRow>
</dmf:datagrid>
</td></tr>
<%
}
//
%>
<%
if(form.getEmptyAttachmentCount() == 0 && form.getAttachmentCount() == 0 && form.getWorkflowAttachmentCount() == 0)
{
//
%>
<tr height='24' class='contentBackground'>
<td colspan='20' class="nodata">
<dmf:label nlsid='MSG_NO_ATTACHMENTS'/>
</td>
</tr>
<%
}
//
%>
</table>
</dmfx:actionmultiselect>
</dmf:form>
