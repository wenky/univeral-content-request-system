<%@ page import="com.documentum.webcomponent.navigation.doclist.DocList,
com.documentum.web.common.BrandingService,
com.documentum.webcomponent.navigation.AbstractNavigation,
com.documentum.web.dragdrop.IDropTarget"%>
<%
//
%>
<!--  dmfx:dragdrop/ -->
<dmfx:actionmultiselect name='multi'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='folderId' contextvalue='folderId'/>
<dmfx:argument name='folderPath' contextvalue='folderPath'/>
<dmf:argument name='startworkflowId' value='startworkflow'/>
<!--  dmfx:dragdropregion name='namedtopdragdropregion' enableddroppositions="<%=IDropTarget.DROP_POSITION_OVER%>" ondrop='onDrop' isbackground='true' -->
<!--  dmfx:argument name='objectId' contextvalue='objectId'/ -->
<dmf:datagrid name='<%=DocList.CONTROL_GRID%>' paged='true' preservesort='false'
width='100%' cellspacing='0' cellpadding='0' bordersize='0'>
<tr class='headerBackground'>
<td colspan=23 height=40>
<table width=100% cellspacing=0 cellpadding=0 border=0>
<tr>
<td align=left colspan=2>
<dmf:breadcrumb name='<%=DocList.CONTROL_BREADCRUMB%>' cssclass='webcomponentBreadcrumb' onclick='onClickFolderPath'/>
</td>
</tr>
<tr>
<td align=left>
<div class='webcomponentBreadcrumbTitle'><dmf:label name='<%=DocList.CONTROL_TITLE%>' cssclass='webcomponentTitle'/></div><div class="bookmarkicon"><dmf:bookmarklink name='<%=DocList.CONTROL_BOOKMARK%>' /></div>
</td>
<td align=right nowrap>
<dmf:dropdownlist name='<%=DocList.CONTROL_FILTER%>' onselect='onSelectTypeFilter' tooltipnlsid="MSG_FILE_FILTER">
</dmf:dropdownlist>&nbsp;
</td>
</tr>
</table>
</td>
</tr>
<dmf:panel name='<%=DocList.BANNER_BACKGROUND_CONTROL%>'>
<tr class='bannerBackground'>
<td colspan=23>
<dmfx:bannerbox>
<dmfx:roombanner name='<%=DocList.BANNER_CONTROL%>' height='36px'/>
<dmfx:richtextpanel>
<div style="margin: 0px 16px"><dmfx:richtextdisplay name='<%=DocList.FOLDER_DESCRIPTION%>'/></div>
</dmfx:richtextpanel>
</dmfx:bannerbox>
</td>
</tr>
</dmf:panel>
<tr class='pagerBackground'>
<td colspan=23 height="24">
<table width=100% border=0 cellspacing=0 cellpadding=0>
<tr>
<td nowrap align=left><dmf:checkbox name='<%=DocList.CONTROL_THUMBNAILS%>' onclick='onClickThumbnails' nlsid='MSG_THUMBNAILS' tooltipnlsid='MSG_THUMBNAILS'/></td>
<td align=center width=100%><dmf:datapaging name='pager1' gotopageclass='doclistPager'/></td>
<td align=right nowrap valign="middle"><dmf:label nlsid='MSG_SHOW_ITEMS'/>&nbsp;</td>
<td valign="middle" nowrap><dmf:datapagesize name='sizer' preference='application.display.classic' tooltipnlsid='MSG_SHOW_ITEMS'/>&nbsp;</td>
</tr>
</table>
</td>
</tr>
<tr>
<td colspan='23' height='1' class='spacer'>&nbsp;</td>
</tr>
<tr class='colHeaderBackground'>
<th scope='col' align='left' nowrap class='doclistcheckbox'>
<dmfx:actionmultiselectcheckall cssclass='doclistbodyDatasortlink'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='folderId' contextvalue='folderId'/>
</dmfx:actionmultiselectcheckall>
</th>
<th scope='col' align='left' nowrap class='doclistlocicon'>
<dmf:datasortimage name='sortimg' datafield='r_lock_owner' cssclass='doclistbodyDatasortlink' reversesort='true' image='icons/sort/sortByLock.gif'/>
</th>
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<th scope='col' align='left' nowrap class='doclistfilenamedatagrid objectlistheaderspacing'>
<dmf:datasortlink name='sort1' datafield='object_name' cssclass='doclistbodyDatasortlink'/>
</th>
<th scope='col' align='center' nowrap class='spacer'>
<dmf:image name='prop' nlsid='MSG_PROPERTIES'  src='images/space.gif'/>
</th>
</dmf:celltemplate>
<dmf:celltemplate field='topic_status'>
<th scope='col' align='left' class='doclisticon'>
<dmf:datasortimage name='sorttopic' datafield='topic_status' cssclass='doclistbodyDatasortlink'
image='icons/sort/sortByDisc.gif'/>
</th>
</dmf:celltemplate>
<dmf:celltemplate field='room_status'>
<th scope='col' align='center' class='doclisticon'>
<dmf:datasortimage name='sortroom' datafield='room_status' cssclass='doclistbodyDatasortlink'
image='icons/sort/sortByRoom.gif'/>
</th>
</dmf:celltemplate>
<dmf:celltemplate field='title'>
<th scope='col' align='left' class='doclistfilenamedatagrid'>
<dmf:datasortlink name='sort2' datafield='title' cssclass='doclistbodyDatasortlink'/>
</th>
</dmf:celltemplate>
<dmf:celltemplate field='authors'>
<th scope='col' align='left' class='doclistfilenamedatagrid'>
<dmf:datasortlink name='sort3' datafield='authors' cssclass='doclistbodyDatasortlink'/>
</th>
</dmf:celltemplate>
<dmf:celltemplate type='number'>
<th scope='col' align='left' class='doclistfilenamedatagrid'>
<nobr><dmf:datasortlink name='sort4' datafield='CURRENT' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr>
</th>
</dmf:celltemplate>
<dmf:celltemplate type='date'>
<th scope='col' align='left' class='doclistfilenamedatagrid'>
<nobr><dmf:datasortlink name='sort5' datafield='CURRENT' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr>
</th>
</dmf:celltemplate>
<dmf:celltemplate>
<th scope='col' align='left' class='doclistfilenamedatagrid'>
<nobr><dmf:datasortlink name='sort6' datafield='CURRENT' cssclass='doclistbodyDatasortlink'/><nobr>
</th>
</dmf:celltemplate>
</dmf:celllist>
<td valign="middle" class="doclisticon">
<dmf:image src="icons/columnprefs_16.gif" nlsid="MSG_COLUMN_PREFERENCES" onclick="onClickColumnsPrefs"/>
</td>
<td width="99%">&nbsp;</td>
</tr>
<dmf:datagridRow tooltipdatafield='object_name' cssclass='defaultDatagridRowStyle' altclass="defaultDatagridRowAltStyle">
<td height=24 nowrap class="doclistcheckbox">
<dmfx:actionmultiselectcheckbox name='check' value='false' cssclass='actions'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='lockOwner' datafield='r_lock_owner'/>
<dmfx:argument name='folderId' contextvalue='objectId'/>
<dmfx:argument name='folderPath' contextvalue='folderPath'/>
<dmf:argument name='ownerName' datafield='owner_name'/>
<dmf:argument name='contentSize' datafield='r_content_size'/>
<dmf:argument name='contentType' datafield='a_content_type'/>
<dmf:argument name="isVirtualDoc" datafield='r_is_virtual_doc'/>
<dmf:argument name="linkCount" datafield='r_link_cnt'/>
<dmf:argument name='startworkflowId' value='startworkflow'/>
<dmf:argument name='workflowRuntimeState' value='-1'/>
<dmf:argument name='isReference' datafield='i_is_reference'/>
<dmf:argument name='isReplica' datafield='i_is_replica'/>
<dmf:argument name='assembledFromId' datafield='r_assembled_from_id'/>
<dmf:argument name='isFrozenAssembly' datafield='r_has_frzn_assembly'/>
<dmf:argument name='compoundArchitecture' datafield='a_compound_architecture'/>
<dmf:argument name='roomId' datafield='room_status'/>
<dmf:argument name='topicStatus' datafield='topic_status'/>
</dmfx:actionmultiselectcheckbox>
</td>
<td nowrap class="doclistlocicon">
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
</td>
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<td nowrap scope='row' class='doclistfilenamedatagrid'>
<dmf:stringlengthformatter maxlen='32'>
<!--  dmfx:dragdropregion datafield='object_name' enableddroppositions="<%=IDropTarget.DROP_POSITION_OVER%>" ondrop='onDrop' dragenabled='true' -->
<!--  dmf:argument name='objectId' datafield='r_object_id'/ -->
<!--  dmfx:argument name='parentObjectId' contextvalue='objectId'/ -->
<dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly' isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16'/>
<dmf:link onclick='onClickObject' runatclient='true' datafield='object_name' >
<dmf:argument name='id' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='isFolder' datafield='isfolder'/>
</dmf:link>
<!--  /dmfx:dragdropregion -->
</dmf:stringlengthformatter>
</td>
<td align="center" valign="middle">
<dmfx:actionimage name='propact' nlsid='MSG_PROPERTIES' action='properties'
src='icons/info.gif' showifdisabled='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmfx:actionimage>
</td>
</dmf:celltemplate>
<dmf:celltemplate field="topic_status">
<td class='doclisticon'>
<dmfx:topicstatus name='status'  nlsid='MSG_NO_COMMENTS' action='showtopicaction'  src='icons/none.gif' height='16' width='16' showifdisabled='false' >
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='topicStatus' datafield='topic_status'/>
<dmfx:argument name='folderId' contextvalue='objectId'/>
</dmfx:topicstatus>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='room_status' >
<td class='doclisticon'>
<dmfx:governedicon name='room' action='view' src='icons/none.gif' height='16' width='16'  >
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='governing' datafield='room_status'/>
<dmf:argument name='type' value='dmc_room'/>
<dmfx:argument name='folderId' contextvalue='objectId'/>
</dmfx:governedicon>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='title'>
<td class='doclistfilenamedatagrid'>
<!-- MRCS 4.2.2 changed to display title for folders CQ Issue: PDCTM00000670-->
<!-- dmfx:folderexclusionformatter datafield='r_object_type' -->
<dmf:stringlengthformatter maxlen='32'>
<dmf:label datafield='title'/>
</dmf:stringlengthformatter>
<!-- /dmfx:folderexclusionformatter -->
</td>
</dmf:celltemplate>
<dmf:celltemplate field='authors'>
<td class='doclistfilenamedatagrid'>
<dmfx:folderexclusionformatter datafield='r_object_type'>
<dmf:label datafield='authors'/>
</dmfx:folderexclusionformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='r_version_label'>
<td class='doclistfilenamedatagrid'>
<dmfx:folderexclusionformatter datafield='r_object_type'>
<dmf:label datafield='r_version_label'/>
</dmfx:folderexclusionformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='a_content_type'>
<td nowrap class='doclistfilenamedatagrid'>
<dmf:stringlengthformatter maxlen='14'>
<dmfx:docformatvalueformatter>
<dmf:label datafield='CURRENT'/>
</dmfx:docformatvalueformatter>
</dmf:stringlengthformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='r_content_size'>
<td nowrap class='doclistfilenamedatagrid'>
<dmfx:docsizevalueformatter datafield='r_object_type'>
<dmf:label datafield='r_content_size'/>
</dmfx:docsizevalueformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='r_current_state'>
<td nowrap class='doclistfilenamedatagrid'>
<dmfx:policystatenameformatter datafield='r_policy_id'>
<dmf:label datafield='r_current_state'/>
</dmfx:policystatenameformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate type='date'>
<td nowrap class='doclistfilenamedatagrid'>
<dmf:datevalueformatter>
<dmf:label datafield='CURRENT'/>
</dmf:datevalueformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate>
<td nowrap class='doclistfilenamedatagrid'>
<dmf:label datafield='CURRENT'/>
</td>
</dmf:celltemplate>
</dmf:celllist>
<td>&nbsp;</td>
<td width="99%">&nbsp;</td>
</dmf:datagridRow>
<dmf:nodataRow>
<td colspan=23 height=24>
<dmf:label nlsid='MSG_NO_DOCUMENTS'/>
</td>
</dmf:nodataRow>
</dmf:datagrid>
<!--  /dmfx:dragdropregion -->
</dmfx:actionmultiselect>
<dmf:hidden name='<%=DocList.CONTROL_FOLDERPATH%>' encrypt='true'/>
<dmfx:topicpanel>
<dmfx:componentinclude name='showtopic' component='embeddedtopic'>
<dmfx:argument name="objectId" contextvalue='objectId'/>
</dmfx:componentinclude>
</dmfx:topicpanel>