<%
//
%>
<%@ page import="com.documentum.webcomponent.navigation.drilldown.DrillDown,
com.documentum.web.form.Form,
com.documentum.web.form.control.databound.Datagrid,
com.documentum.web.form.control.databound.IDataboundParams,
com.documentum.webcomponent.navigation.doclist.DocList,
com.documentum.webcomponent.navigation.AbstractNavigation,
com.documentum.web.dragdrop.IDropTarget"%>
<dmfx:dragdrop/>
<table cellspacing="0" cellpadding="0" border="0" width='100%'>
<tr>
<td class='streamlineheader controlRowSpacing' width="100%" valign="bottom">
<table width=100% cellspacing="0" cellpadding="0" border="0">
<tr>
<td align=left>
<dmf:breadcrumb name='<%=DrillDown.CONTROL_BREADCRUMB%>' onclick='onClickBreadcrumb'/>
</td>
</tr>
<tr>
<td align="left" valign="bottom" class="webcomponenttitle">
<dmf:label name='<%=DrillDown.CONTROL_TITLE%>'/>&nbsp;<dmf:bookmarklink
name='<%=DrillDown.CONTROL_BOOKMARK%>' />
</td>
</tr>
</table>
</td>
</tr>
</table>
<table cellspacing="0" cellpadding="0" border="0" width='100%'>
<tr>
<td valign=top width="100%" >
<div class="actionslinks controlRowSpacing">
<table cellspacing="0" cellpadding=0 border="0" width='100%'>
<tr>
<td align='left' valign='top' nowrap >
<span class='drilldownTitle'><dmf:label nlsid='MSG_ACTIONS'/>:&nbsp;</span>
</td>
<td align='left' valign='top' width="99%" class='actions'>
<dmfx:actionlinklist listid='container-actions' name='container_action_list'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='folderId' contextvalue='parentId'/>
<dmfx:argument name='folderPath' contextvalue='parentFolderPath'/>
<dmf:argument name='startworkflowId' value='startworkflowdrilldown'/>
</dmfx:actionlinklist>
</td>
</tr>
</table>
</div>
<dmf:panel name='<%=DrillDown.BANNER_BACKGROUND_CONTROL%>'>
<div class='bannerBackgroundStreamline'>
<dmfx:bannerbox>
<dmfx:roombanner name='<%=DrillDown.BANNER_CONTROL%>' height='36px'/>
<dmfx:richtextpanel>
<div style="margin: 0px 16px"><dmfx:richtextdisplay name='<%=DocList.FOLDER_DESCRIPTION%>'/></div>
</dmfx:richtextpanel>
</dmfx:bannerbox>
</div>
</dmf:panel>
<dmfx:dragdropregion name='namedtopdragdropregionFolder' enableddroppositions="<%=IDropTarget.DROP_POSITION_OVER%>"
ondrop='onDrop' isbackground='true'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmf:datagrid name='<%=DrillDown.CONTROL_FOLDERGRID%>' width='100%' paged='true' cellspacing="0" cellpadding='0'
bordersize='0' sortcolumn="object_name" sortdir="<%=String.valueOf(IDataboundParams.SORTDIR_FORWARD)%>"
sortmode="<%=String.valueOf(IDataboundParams.SORTMODE_CASEINSTEXT)%>" >
<tr class="pagerBackground">
<td class="pager controlRowSpacing" width="100%" colspan="2">
<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
<td nowrap>
<dmf:label name='<%=DrillDown.CONTROL_FOLDERHEADER%>' cssclass='drilldownTitle'/>&nbsp;&nbsp;
</td>
<td align="center" width="97%" nowrap>
<dmf:datapaging gotopageclass='drilldownPager' name='folderpager'/>
</td>
<td align="right" valign="middle" nowrap>
<dmf:label nlsid='MSG_SHOW_ITEMS'/>&nbsp;
</td>
<td align="right" valign="middle" nowrap>
<dmf:datapagesize name='foldersizer' preference='application.display.streamline_folders'
tooltipnlsid='MSG_SHOW_ITEMS'/>
</td>
</tr>
</table>
</td>
</tr>
<tr>
<td class="folderlinks">
<table cellspacing="0" cellpadding='0' border="0">
<%
String strColumns = "3";
%>
<dmfx:clientenvpanel environment='portal'>
<%
strColumns = "2";
%>
</dmfx:clientenvpanel>
<dmf:datagridRow name='objname' columns='<%=strColumns%>' tooltipdatafield='object_name'
cssclass='contentBackground' altclass='contentBackground'>
<td nowrap>
<dmf:stringlengthformatter maxlen='32'>
<dmfx:dragdropregion datafield='object_name' enableddroppositions="<%=IDropTarget.DROP_POSITION_OVER%>" ondrop='onDrop' dragenabled='true'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmfx:argument name='parentObjectId' contextvalue='objectId'/>
<dmfx:docbaseicon typedatafield='r_object_type' isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference'/>
<dmf:link cssclass='drilldownDirectoryName' name='lnkFolder' onclick='onClickFolder' datafield='object_name'>
<dmf:argument name='objectId' datafield='r_object_id'/>
</dmf:link>
</dmfx:dragdropregion>
</dmf:stringlengthformatter>
</td>
</dmf:datagridRow>
<dmf:nodataRow name='objnamenodata'>
<td colspan='20'>
<dmf:label cssclass='drilldownFileInfo' nlsid='MSG_NO_FOLDERS'/>
</td>
</dmf:nodataRow>
</table>
</td>
</tr>
</dmf:datagrid>
<%
Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
Datagrid docgrid = (Datagrid)form.getControl(DrillDown.CONTROL_DOCGRID, Datagrid.class);
if ( docgrid.getDataProvider().getQuery() != null &&
docgrid.getDataProvider().getQuery().length() != 0 )
{
//
%>
<dmf:datagrid name='<%=DrillDown.CONTROL_DOCGRID%>' paged='true' preservesort='false' width='100%'
cellspacing="0" cellpadding="0" bordersize='0'>
<tr><td height="5" class="spacer">&nbsp;</td></tr>
<tr class="pagerBackground">
<td width="100%" class="controlRowSpacing">
<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tr>
<td nowrap>
<dmf:label cssclass='drilldownTitle' nlsid='MSG_DOCUMENTS'/>&nbsp;&nbsp;
</td>
<td width="99%" align="center" valign="middle">
<dmf:datapaging gotopageclass='drilldownPager' name='docpager'/>
</td>
<td align="right" valign="middle" nowrap>
&nbsp;<dmf:label nlsid='MSG_SHOW_ITEMS'/>&nbsp;
</td>
<td align="right" valign="middle" nowrap>
<dmf:datapagesize name='docsizer' preference='application.display.streamline_files'  tooltipnlsid='MSG_SHOW_ITEMS'/>&nbsp;
</td>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<td nowrap align='right'>
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment='portal'>
</tr>
</table>
</td>
</tr>
<tr>
<td class="spacer" height="1" colspan="2">&nbsp;</td>
</tr>
<tr>
<td nowrap align='right' class="colHeaderBackground controlRowSpacing">
</dmfx:clientenvpanel>
<dmf:checkbox name='<%=DrillDown.CONTROL_THUMBNAILS%>' onclick='onClickThumbnails' nlsid='MSG_THUMBNAILS' tooltipnlsid='MSG_THUMBNAILS'/>
&nbsp;
<dmf:dropdownlist name='<%=DrillDown.CONTROL_FILTER%>' onselect='onSelectTypeFilter' tooltipnlsid='MSG_FILE_FILTER'>
</dmf:dropdownlist>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
</td>
</tr>
</table>
</td>
</tr>
<tr>
<td class="spacer" height="1" colspan="2">&nbsp;</td>
</tr>
<tr>
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment='portal'>
</td>
</tr>
<tr>
<td class="spacer" height="1" colspan="2">&nbsp;</td>
</tr>
<tr>
</dmfx:clientenvpanel>
<td colspan="2">
<table cellspacing="0" cellpadding="0" border="0" width='100%'>
<%
if (docgrid.getDataProvider().getResultsCount() > 0)
{
//
%>
<tr  class="colHeaderBackground">
<td colspan='20' align="left" valign="middle" width='100%' class="controlRowSpacing">
<table cellpadding="0" cellspacing="0" border="0">
<tr valign="middle">
<td>
<nobr><dmf:label cssclass="drilldownTitle" nlsid='MSG_SORTBY'/>:&nbsp;
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<dmf:datasortlink name='sort1' datafield='object_name' mode='caseinstext'/>
</dmf:celltemplate>
<dmf:celltemplate field='r_lock_owner'>
<span class="pipetextshadow">|<span class="pipetext">|</span></span>&nbsp;<dmf:datasortlink name='sort2' datafield='r_lock_owner' reversesort='true'/>
</dmf:celltemplate>
<dmf:celltemplate field='thumbnail_url'/>
<dmf:celltemplate type='number'>
<span class="pipetextshadow">|<span class="pipetext">|</span></span>&nbsp;<dmf:datasortlink name='sort3' datafield='CURRENT' mode='numeric'/>
</dmf:celltemplate>
<dmf:celltemplate type='date'>
<span class="pipetextshadow">|<span class="pipetext">|</span></span>&nbsp;<dmf:datasortlink name='sort4' datafield='CURRENT' mode='numeric'/>
</dmf:celltemplate>
<dmf:celltemplate>
<span class="pipetextshadow">|<span class="pipetext">|</span></span>&nbsp;<dmf:datasortlink name='sort5' datafield='CURRENT'/>
</dmf:celltemplate>
</dmf:celllist>
</nobr>
</td>
<td class="colprefsicon">
<dmf:image src="icons/columnprefs_16.gif" nlsid="MSG_COLUMN_PREFERENCES" onclick="onClickColumnsPrefs"/>
</td>
</tr>
</table>
</td>
</tr>
<%
}
//
%>
<dmf:datagridRow name='controls' tooltipdatafield='object_name' cssclass='streamlineDatagridRowStyle'
altclass="streamlineDatagridRowAltStyle">
<td class="streamlinerowspacer">&nbsp;</td>
<td valign=top class="spacer" align="right">
<dmfx:docbaselockicon datafield='r_lock_owner' cssclass="null" size='16'/>
</td>
<td align=left style="padding-left: 4px">
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<table cellspacing="0" cellpadding="0" width="100%" border="0">
<tr>
<td class="objectname" nowrap>
<dmf:stringlengthformatter maxlen='48'>
<dmfx:dragdropregion datafield='object_name' enableddroppositions="<%=IDropTarget.DROP_POSITION_OVER%>" ondrop='onDrop' dragenabled='true'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmfx:argument name='parentObjectId' contextvalue='objectId'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmfx:docbaseicon formatdatafield='a_content_type'
typedatafield='r_object_type' linkcntdatafield='r_link_cnt'
isvirtualdocdatafield='r_is_virtual_doc'
assembledfromdatafield='r_assembled_from_id'
isfrozenassemblydatafield='r_has_frzn_assembly'
isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference'/>
<dmf:link name='doclnk' onclick='onClickDocument' datafield='object_name' >
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmf:link>
</dmfx:dragdropregion>
</dmf:stringlengthformatter>
</td>
</tr>
</table>
</dmf:celltemplate>
<div class='drilldownFileInfo'>
<dmf:celltemplate field='r_content_size'>
<nobr>
<dmf:label cssclass='drilldownLabel'/>:&nbsp;
<dmfx:docsizevalueformatter>
<dmf:label datafield='r_content_size'/>
</dmfx:docsizevalueformatter>
&nbsp;&nbsp;
</nobr>
</dmf:celltemplate>
<dmf:celltemplate field='a_content_type'>
<nobr>
<dmf:label cssclass='drilldownLabel'/>:&nbsp;
<dmf:stringlengthformatter maxlen='16'>
<dmfx:docformatvalueformatter>
<dmf:label datafield='a_content_type'/>
</dmfx:docformatvalueformatter>
</dmf:stringlengthformatter>
&nbsp;&nbsp;
</nobr>
</dmf:celltemplate>
<dmf:celltemplate field='r_lock_owner'>
<dmf:panel datafield='r_lock_owner'>
<br>
<nobr>
<i><dmf:label cssclass='drilldownLabel'/>:&nbsp;
<dmf:label cssclass='drilldownLabel' datafield='r_lock_owner'/></i>
</nobr>
</dmf:panel>
</dmf:celltemplate>
<dmf:celltemplate field='r_current_state'>
<nobr>
<dmf:label cssclass='drilldownLabel'/>:&nbsp;
<dmfx:policystatenameformatter datafield='r_policy_id'>
<dmf:label datafield='r_current_state'/>
</dmfx:policystatenameformatter>
&nbsp;&nbsp;
</nobr>
</dmf:celltemplate>
<dmf:celltemplate field='thumbnail_url'/>
<dmf:celltemplate type='date'>
<nobr>
<dmf:label cssclass='drilldownLabel'/>:&nbsp;
<dmf:datevalueformatter>
<dmf:label datafield='CURRENT'/>
</dmf:datevalueformatter>
</nobr>
</dmf:celltemplate>
<dmf:celltemplate field='room_status'/>
<dmf:celltemplate field='topic_status'/>
<dmf:celltemplate>
<nobr>
<dmf:label cssclass='drilldownLabel'/>:&nbsp;
<dmf:label datafield='CURRENT'/>
&nbsp;&nbsp;
</nobr>
</dmf:celltemplate>
</div>
</dmf:celllist>
</td>
<dmfx:clientenvpanel environment='portal'>
<td class="nowrap" align="left">
<table border="0" cellspacing="0" cellpadding="0" width="100%">
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<td width="84" nowrap>
<table border="0" cellspacing="0" cellpadding="0" style="margin-left:2px;margin-right:2px">
</dmfx:clientenvpanel>
<tr valign="top">
<dmfx:clientenvpanel environment="portal">
<td class="icons">
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment="portal" reversevisible="true">
<td>
</dmfx:clientenvpanel>
<dmfx:actionimage name='propact' nlsid='MSG_PROPERTIES' action='properties' cssclass='actions' src='icons/info.gif'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmfx:actionimage>
</td>
<dmf:celllist>
<dmf:celltemplate field='room_status'>
<td width='18' align='center'>
<dmfx:governedicon name='room' action='view' src='icons/none.gif' height='16' width='16'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='governing' datafield='room_status'/>
<dmf:argument name='type' value='dmc_room'/>
<dmfx:argument name='folderId' contextvalue='objectId'/>
</dmfx:governedicon>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='topic_status'>
<td width='18' align='center'>
<dmfx:topicstatus name='status' nlsid='MSG_NO_COMMENTS' action='showtopicaction' src=''>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='topicStatus' datafield='topic_status'/>
<dmfx:argument name='folderId' contextvalue='objectId'/>
<dmfx:argument name='folderPath' contextvalue='folderPath'/>
</dmfx:topicstatus>
</td>
</dmf:celltemplate>
</dmf:celllist>
<dmf:columnpanel columnname='<%=DrillDown.THUMBNAIL_COLUMN%>'>
<td>
<table width="100" height="100" cellspacing="0" cellpadding="0" border="0">
<tr>
<td align=center>
<dmf:image datafield='<%=DrillDown.THUMBNAIL_COLUMN%>' onclick='onClickDocument'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmf:image>
</td>
<tr>
</table>
</td>
</dmf:columnpanel>
</tr>
</table>
</td>
<dmfx:clientenvpanel environment="portal" reversevisible="true">
<td align=left width=60%>
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment="portal">
<td align=left width="120">
</dmfx:clientenvpanel>
<dmfx:actionlinklist name='<%=DrillDown.CONTROL_ACTIONLIST%>' cssclass="actions">
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='lockOwner' datafield='r_lock_owner'/>
<dmf:argument name='ownerName' datafield='owner_name'/>
<dmfx:argument name='folderId' contextvalue='objectId'/>
<dmfx:argument name='folderPath' contextvalue='folderPath'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmf:argument name='isVirtualDoc' datafield='r_is_virtual_doc'/>
<dmf:argument name='linkCount' datafield='r_link_cnt'/>
<dmf:argument name='startworkflowId' value='startworkflowdrilldown'/>
<dmf:argument name='isReference' datafield='i_is_reference'/>
<dmf:argument name='isReplica' datafield='i_is_replica'/>
<dmf:argument name='assembledFromId' datafield='r_assembled_from_id'/>
<dmf:argument name='isFrozenAssembly' datafield='r_has_frzn_assembly'/>
<dmf:argument name='compoundArchitecture' datafield='a_compound_architecture'/>
<dmf:argument name='roomId' datafield='room_status'/>
<dmf:argument name='topicStatus' datafield='topic_status'/>
</dmfx:actionlinklist>
</td>
</dmf:datagridRow>
<dmf:nodataRow name='nodatarow2'>
<td width=2 height=18>&nbsp;</td>
<td colspan=20>
<dmf:label nlsid='MSG_NO_DOCUMENTS'/>
</td>
</dmf:nodataRow>
</table>
</td>
</tr>
</dmf:datagrid>
<%
}
//
%>
</dmfx:dragdropregion>
</td>
</tr>
</table>
<dmf:hidden name='<%=DrillDown.CONTROL_FOLDERPATH%>' encrypt='true'/>
<dmfx:topicpanel>
<dmfx:componentinclude name='showtopic' component='embeddedtopic'>
<dmfx:argument name="objectId" contextvalue='objectId'/>
</dmfx:componentinclude>
</dmfx:topicpanel>
