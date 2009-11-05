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

 Filename       $RCSfile: MrcsStartAttachmentDrilldown.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:41 $

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
<%@ page import="com.documentum.services.workflow.inbox.IInbox" %>
<%@ page import="com.documentum.web.form.control.databound.Datagrid"%>
<%@ page import="com.documentum.webcomponent.library.workflow.attachment.StartAttachment" %>
<%@ page import="com.documentum.web.form.control.databound.IDataboundParams" %>
<dmf:webform/>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/popupMenu.js")%>'></script>
<dmf:form>
<%
StartAttachment form = (StartAttachment)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
//
%>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr><td class='taskmgrSectionHeading'>
<dmf:label nlsid='MSG_ATTACHMENTS_HEADER'/>
</td>
</tr>
<tr><td>
<dmf:datagrid name='<%=StartAttachment.ATTACHMENT_GRID_CONTROL_NAME%>' paged='true' pagesize='5'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0' cssclass='doclistbodyDatagrid'>
<%
if (form.getAttachmentCount() > 1)
{
//
%>
<dmf:row>
<td align='center' style='padding:3px; padding-bottom:6px' colspan='2'>
<table align='center' border='0'><tr><td nowrap>
<b><nobr><span class='drilldownFileInfo'><dmf:label nlsid='MSG_SORTBY'/>:&nbsp;</span></b>
<dmf:datasortlink cssclass='drilldownFileInfo' name='attch_sort1' nlsid='MSG_DOC_NAME' column='object_name'/>&nbsp;|
<dmf:datasortlink cssclass='drilldownFileInfo' name='attch_sort2' nlsid='MSG_DOC_SIZE' column='r_content_size'/>&nbsp;|
<dmf:datasortlink cssclass='drilldownFileInfo' name='attch_sort3' nlsid='MSG_DOC_FORMAT' column='a_content_type'/>&nbsp;|
<dmf:datasortlink cssclass='drilldownFileInfo' name='attch_sort4' nlsid='MSG_DOC_MODIFIED' column='r_modify_date'/>
</nobr>
</td></tr></table>
</td>
</dmf:row>
<%
}
//
%>
<dmf:datagridRow>
<td valign=top style='padding-top:6px;padding-left:3px'>
<dmfx:docbaseicon name='icon' formatdatafield='a_content_type' typedatafield='r_object_type'
linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' size='16'/><br>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
</td>
<td>
<table border=0 cellspacing=2 cellpadding=0 width=100%>
<tr valign=top>
<td align=left width='80%'>
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
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
</dmf:link>
<%
}
//
%>
<br>
<div class='drilldownFileInfo'>
<b><dmf:label nlsid='MSG_DOC_VERSION'/>:</b>&nbsp;
<dmf:label datafield='r_version_label'/>
&nbsp;&nbsp;
<b><dmf:label nlsid='MSG_DOC_SIZE'/>:</b>&nbsp;
<dmfx:docsizevalueformatter>
<dmf:label datafield='r_content_size'/>
</dmfx:docsizevalueformatter>
&nbsp;&nbsp;
<b><dmf:label nlsid='MSG_DOC_FORMAT'/>:</b>&nbsp;
<dmfx:docformatvalueformatter>
<dmf:label datafield='a_content_type'/>
</dmfx:docformatvalueformatter>
&nbsp;&nbsp;
<b><dmf:label nlsid='MSG_DOC_MODIFIED'/>:</b>&nbsp;
<dmf:datevalueformatter type='short'>
<dmf:label datafield='r_modify_date'/>
</dmf:datevalueformatter>
<br>
<b><dmf:label nlsid='MSG_DOC_LOCKED'/>:</b>&nbsp;
<dmf:label datafield='r_lock_owner'/>
</div>
</td>
<td>
<dmfx:actionimage name='propertiesimage' nlsid='MSG_PROPERTIES' action='properties' src='icons/info.gif' showifdisabled='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmfx:actionimage>
</td>
<td align=right>
<!--
<dmf:link cssclass='actions' onclick='onRemoveAttachment' name='removeattachmentbtn' nlsid='MSG_REMOVE_ATTACHMENT'>
<dmfx:argument name='objectId' contextvalue='processId'/>
<dmf:argument name='type' value='dmi_package'/>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='attachmentName' datafield='object_name'/>
<dmf:argument name='isPackageManufactured' datafield='isPackageManufactured'/>
<dmf:argument name='docId' datafield='r_object_id'/>
</dmf:link>
-->
</td>
</tr>
</table>
</td>
</dmf:datagridRow>
<%
// avoid rendering the row if the pager doesn't appear
if(form.getAttachmentCount() > ((Datagrid)form.getControl(StartAttachment.ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getPageSize())
{
//
%>
<dmf:row cssclass='contentBackground'>
<td colspan='20' align='center' valign='top'>
<br><br>
<dmf:datapaging name='attachpager'/>
</td>
</dmf:row>
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
<dmf:datagrid name='<%=StartAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME%>' paged='false'
preservesort='false' width='100%' cellspacing='0' cellpadding='0' bordersize='0' cssclass='doclistbodyDatagrid'
sortcolumn='packageName' sortdir='<%=Integer.toString(IDataboundParams.SORTDIR_FORWARD)%>'>
<dmf:datagridRow name='columns' height='24' cssclass='contentBackground'>
<td valign=top style='padding-top:6px' width='16'>
<dmfx:docbaseicon name='icon2' type='dm_package' size='16'/>
</td>
<td width='10'>&nbsp;</td>
<td>
<table border='0' cellspacing='2' cellpadding='0' width='100%'>
<tr>
<td align='left'>
<dmf:link onclick='onSelectEmptyAttachment' datafield='packageName' cssclass='drilldownFileName'>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmfx:argument name='formSaveFolderPath' contextvalue='formsavefolderpath'/>
</dmf:link>
</td>
</tr>
<tr>
<td valign='top' align='left'>
<dmf:label cssclass='drilldownFileInfo' datafield='optionalLabel'/>
</td>
<td>
<table border='0' cellspacing='2' cellpadding='0' width='100%'>
<tr>
<%
// avoid rendering the row if the pager doesn't appear
String formTemplateId = ((Datagrid)form.getControl(StartAttachment.MISSING_ATTACHMENT_GRID_CONTROL_NAME)).getDataProvider().getDataField("formTemplateId");
if (formTemplateId != null && formTemplateId.length() > 0)
{
//
%>
<td valign='top' align='right'>
<dmf:link onclick='onNewFormAttachment' datafield='useFormName' cssclass='actions' name='<%=StartAttachment.NEW_FORM_LINK_CONTROL_NAME%>'>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
<dmfx:argument name='formSaveFolderPath' contextvalue='formsavefolderpath'/>
</dmf:link>
</td>
<%
}
//
%>
<td valign='top' align='right'>
<dmf:link onclick='onAddAttachment' nlsid='MSG_ADD_ATTACHMENT' cssclass='actions'>
<dmf:argument name='packageName' datafield='packageName'/>
<dmf:argument name='formTemplateId' datafield='formTemplateId'/>
</dmf:link>
</td>
</tr>
</table>
</td>
</tr>
</table>
</td>
</dmf:datagridRow>
</dmf:datagrid>
</td></tr>
<%
}
if(form.getEmptyAttachmentCount() == 0 && form.getAttachmentCount() == 0)
{
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
</dmf:form>
