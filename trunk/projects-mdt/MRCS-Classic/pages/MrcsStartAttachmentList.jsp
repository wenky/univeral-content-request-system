<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.web.form.control.databound.Datagrid" %>
<%@ page import="com.documentum.webcomponent.library.workflow.attachment.StartAttachment" %>
<%@ page import="com.documentum.web.form.control.databound.IDataboundParams" %>
<dmf:webform/>
<dmf:form>
<%
StartAttachment form = (StartAttachment)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
//
%>
<dmfx:actionmultiselect name='multiselect'>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr><td class='taskmgrSectionHeading'>
<dmf:label nlsid='MSG_ATTACHMENTS_HEADER'/>
<%
if (form.isAddWfAttachmentSupported() == true)
{
//
%>
&nbsp;&nbsp;

<!--  dmfx:actionimage name='addWfAttachmentImg' nlsid='MSG_ADD_ATTACHMENT' action='addwfattachment' oncomplete='onComplete' src='icons/add.gif' showifdisabled='false'/ -->

<%
}
//
%>
</td>
</tr>
<tr><td>
<dmf:datagrid name='<%=StartAttachment.ATTACHMENT_GRID_CONTROL_NAME%>' paged='true' pagesize='10'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0'
cssclass='doclistbodyDatagrid'>
<%
if(((Datagrid)form.getControl(StartAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getResultsCount() != 0)
{
//
%>
<tr height='24'>
<th scope='col' align='left' nowrap width='16'>&nbsp;</th>
<th scope='col' align='left' nowrap width='16'>&nbsp;</th>
<th scope='col' align='left' nowrap>
<dmf:datasortlink name='sort_object_name' nlsid='MSG_DOC_NAME' column='object_name' />
</th>
<th scope='col' align='left' nowrap>
<dmf:image name='prop' nlsid='MSG_PROPERTIES' src='images/space.gif'/>
</th>
<th scope='col' align='left' nowrap>
<dmf:datasortlink name='sort_a_content_type' nlsid='MSG_DOC_FORMAT' column='a_content_type' />
</th>
<th scope='col' align='left' nowrap>
<dmf:datasortlink name='sort_r_modify_date' nlsid='MSG_DOC_MODIFIED' column='r_modify_date' />
</th>
<th scope='col' align='right' nowrap>
<dmf:label nlsid='MSG_PACKAGE_ACTIONS_HDR'/>
</th>
<tr>
<%
}
else
{
//
%>
<tr height='24'>
<th scope='col' align='left' nowrap width='16'>&nbsp;</th>
<th scope='col' align='left' nowrap width='16'>&nbsp;</th>
<th scope='col' align='left' nowrap width='16'>&nbsp;</th>
<th scope='col' align='left' nowrap>
<dmf:label nlsid='MSG_DOC_NAME'/>
</th>
<th scope='col' align='right' nowrap>
<dmf:label nlsid='MSG_PACKAGE_ACTIONS_HDR'/>
</th>
<tr>
<%
}
//
%>
<dmf:datagridRow name='attachcolumns' height='24'>
<td nowrap width='16'>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
</td>
<td nowrap width='25' style='padding-right: 5px'>
<dmfx:docbaseicon name='attachment_icon' formatdatafield='a_content_type' typedatafield='r_object_type'
linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc'
assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly'
isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16'/>
</td>
<td scope='row' scope='row'>
<dmf:stringlengthformatter maxlen='18' wrapped='true' postfix='...'>
<%
if ( ((Datagrid)form.getControl(StartAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getDataField("r_object_id").equals(form.getShowingFormId()) )
{
//
%>
<dmf:label datafield='object_name' cssclass='drilldownFileName'/>
<%
} else {
//
%>
<dmf:link onclick='onViewAttachment' datafield='object_name' cssclass='drilldownFileName' name='lnkDoc'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmf:argument name='assembledFromId' datafield='r_assembled_from_id'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
</dmf:link>
<%
}
//
%>
</dmf:stringlengthformatter>
</td>
<td>
<dmfx:actionimage name='propertiesimage' nlsid='MSG_PROPERTIES' action='properties' src='icons/info.gif' showifdisabled='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='templateId' datafield='formTemplateId'/>
<dmf:argument name='preferForms' datafield='useFormForProperties'/>
</dmfx:actionimage>
</td>
<td>
<dmfx:docformatvalueformatter>
<dmf:label datafield='a_content_type'/>
</dmfx:docformatvalueformatter>
</td>
<td>
<dmf:datevalueformatter>
<dmf:label datafield='r_modify_date'/>
</dmf:datevalueformatter>
</td>
<td align=right style='padding-right:5px'>
<dmf:image onclick='onRemoveAttachment' src='icons/trashcan.gif' name='actionimage_removestartwfattachment'>
<dmfx:argument name='objectId' contextvalue='processId'/>
<dmf:argument name='type' value='dmi_package'/>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='attachmentName' datafield='object_name'/>
<dmf:argument name='isPackageManufactured' datafield='isPackageManufactured'/>
<dmf:argument name='docId' datafield='r_object_id'/>
</dmf:image>
<td>
</dmf:datagridRow>
<%
// avoid rendering the row if the pager doesn't appear
if(form.getAttachmentCount() > ((Datagrid)form.getControl(StartAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getPageSize())
{
//
%>
<tr class='contentBackground'>
<td colspan='20' align='center' valign='top'>
<br><br>
<dmf:datapaging name='attachpager'/>
</td>
</tr>
<%
}
//
%>
</dmf:datagrid>
</td></tr>
<%
if(form.getEmptyAttachmentCount() > 0)
{
//
%>
<tr><td>
<dmf:datagrid name='<%=StartAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME%>' paged='true'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0' cssclass='doclistbodyDatagrid'
sortcolumn='packageName' sortdir='<%=Integer.toString(IDataboundParams.SORTDIR_FORWARD)%>'>
<dmf:datagridRow name='missingattachcolumns' height='24' cssclass='contentBackground'>
<td nowrap width='16'>&nbsp;</td>
<td nowrap width='16' style='padding-right: 5px'>
<dmfx:docbaseicon name='icon' type='dm_package' size='16'/>
</td>
<td nowrap>
<dmf:link onclick='onSelectEmptyAttachment' datafield='packageName'>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmfx:argument name='formSaveFolderPath' contextvalue='formsavefolderpath'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
</dmf:link>
&nbsp;
<dmf:label datafield='optionalLabel'/>
</td>
<td align=right style='padding-right:1px'>
<%
// avoid rendering the row if the pager doesn't appear
String formTemplateId = ((Datagrid)form.getControl(StartAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getDataField("formTemplateId");
String formForProperties = ((Datagrid)form.getControl(StartAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getDataField("useFormForProperties");
if (formForProperties != null && formForProperties.equals("false") &&
formTemplateId != null && formTemplateId.length() > 0)
{
//
%>
<span title='<%=form.getString("MSG_NEW_FORM_ATTACHMENT")%>'>
<dmf:image  onclick='onNewFormAttachment' src='icons/edit.gif'>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmfx:argument name='formSaveFolderPath' contextvalue='formsavefolderpath'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
</dmf:image>
</span>
<%
}
//
%>
<span title='<%=form.getString("MSG_ADD_ATTACHMENT_TOOLTIP")%>'>
<dmf:image onclick='onAddAttachment' src='icons/add.gif'>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmf:argument name='useFormForProperties' datafield='useFormForProperties'/>
</dmf:image>
</span>
<td>
</dmf:datagridRow>
</dmf:datagrid>
</td></tr>
<%
}
//
%>
<%
if (form.isAddWfAttachmentSupported() == true && form.getWorkflowAttachmentCount() > 0)
{
//
%>
<tr><td>
<dmf:datagrid name='<%=StartAttachment.WF_ATTACHMENT_GRID_CONTROL_NAME%>' paged='true' pagesize='10'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0'
cssclass='doclistbodyDatagrid'>
<dmf:datagridRow name='attachcolumns' height='24'>
<td nowrap width='16'>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
</td>
<td nowrap width='25' style='padding-right: 5px'>
<dmfx:docbaseicon name='wfattachment_icon' formatdatafield='a_content_type' typedatafield='r_object_type'
linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc'
assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly'
isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16'/>
</td>
<td scope='row' scope='row'>
<dmf:stringlengthformatter maxlen='18' wrapped='true' postfix='...'>
<dmf:link onclick='onViewAttachment' datafield='object_name' cssclass='drilldownFileName' name='lnkWfDoc'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
</dmf:link>
</dmf:stringlengthformatter>
</td>
<td>
<dmfx:actionimage name='propertiesimage' nlsid='MSG_PROPERTIES' action='properties' src='icons/info.gif' showifdisabled='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmfx:actionimage>
</td>
<td>
<dmfx:docformatvalueformatter>
<dmf:label datafield='a_content_type'/>
</dmfx:docformatvalueformatter>
</td>
<td>
<dmf:datevalueformatter>
<dmf:label datafield='r_modify_date'/>
</dmf:datevalueformatter>
</td>
<td align=right style='padding-right:5px'>
<dmfx:actionimage cssclass='actions' action='removestartwfwfattachment' name='removewfattachmentbtn' nlsid='MSG_REMOVE_ATTACHMENT' src='icons/trashcan.gif'>
<dmf:argument name='type' value='dmi_wf_attachment'/>
<dmfx:argument name='objectId' contextvalue='processId'/>
<dmf:argument name='docId' datafield='r_object_id'/>
<dmf:argument name='attachmentName' datafield='object_name'/>
</dmfx:actionimage>
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
<td colspan='20'>
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
