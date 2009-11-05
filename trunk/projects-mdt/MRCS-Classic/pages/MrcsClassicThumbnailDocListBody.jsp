<%@ page import="com.documentum.webcomponent.navigation.doclist.DocList"%>
<%
//
%>
<dmfx:actionmultiselect name='multi'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='folderId' contextvalue='folderId'/>
<dmf:argument name='startworkflowId' value='startworkflow'/>
<dmf:datagrid name='<%=DocList.CONTROL_GRID%>' paged='true' preservesort='false'
cssclass='doclistbodyDatagrid' width='100%' cellspacing='0' cellpadding='0' bordersize='0'>
<tr class='headerBackground'>
<td colspan=20 height=40>
<table width=100% cellspacing=0 cellpadding=0 border=0>
<tr>
<td align=left colspan=2>
<dmf:breadcrumb name='<%=DocList.CONTROL_BREADCRUMB%>' cssclass='webcomponentBreadcrumb' onclick='onClickFolderPath'/>
</td>
</tr>
<tr>
<td align=left>
<dmf:label name='<%=DocList.CONTROL_TITLE%>' cssclass='webcomponentTitle'/>
</td>
<td align=right>
<dmf:dropdownlist name='<%=DocList.CONTROL_FILTER%>' onselect='onSelectTypeFilter' tooltipnlsid="MSG_FILE_FILTER">
</dmf:dropdownlist>
</td>
</tr>
</table>
</td>
</tr>
<tr class='pagerBackground'>
<td colspan=20 align=center height=24>
<table width=100% border=0 cellspacing=0 cellpadding=0>
<tr>
<td nowrap align=left><dmf:checkbox name='<%=DocList.CONTROL_THUMBNAILS%>' onclick='onClickThumbnails' nlsid='MSG_THUMBNAILS' tooltipnlsid='MSG_THUMBNAILS'/></td>
<td align=center width=100%><dmf:datapaging name='pager1' gotopageclass='doclistPager'/></td>
<td align=right nowrap><dmf:label nlsid='MSG_SHOW_ITEMS'/>&nbsp;</td>
<td valign="middle" nowrap><dmf:datapagesize name='sizer' preference='application.display.classic' tooltipnlsid='MSG_SHOW_ITEMS'/></td>
</tr>
</table>
</td>
</tr>
<tr>
<td colspan='20' width='100%' align='center'><table align='center' border='0'><tr>
<td scope='col' align='left' width=16 height=16>
<dmfx:actionmultiselectcheckall cssclass='actions'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='folderId' contextvalue='folderId'/>
</dmfx:actionmultiselectcheckall>
</td>
<td><nobr><b><span class='drilldownFileInfo'><dmf:label nlsid='MSG_SORTBY'/>:&nbsp;</span></b>
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<dmf:datasortlink cssclass='drilldownFileInfo' name='sort1' datafield='object_name' mode='caseinstext'/>
</dmf:celltemplate>
<dmf:celltemplate field='thumbnail_url'>
</dmf:celltemplate>
<dmf:celltemplate type='number'>
<span class="pipetextshadow">|<span class="pipetext">|</span></span>&nbsp;<dmf:datasortlink cssclass='drilldownFileInfo' name='sort2' datafield='CURRENT' mode='numeric'/>
</dmf:celltemplate>
<dmf:celltemplate type='date'>
<span class="pipetextshadow">|<span class="pipetext">|</span></span>&nbsp;<dmf:datasortlink cssclass='drilldownFileInfo' name='sort3' datafield='CURRENT' mode='numeric'/>
</dmf:celltemplate>
<dmf:celltemplate>
<span class="pipetextshadow">|<span class="pipetext">|</span></span>&nbsp;<dmf:datasortlink cssclass='drilldownFileInfo' name='sort4' datafield='CURRENT'/>
</dmf:celltemplate>
</dmf:celllist>
</nobr></td></tr></table></td>
</tr>
<tr height='1' class='doclistbodySeparator'>
<td colspan='20' class='rowSeparator'><dmf:image src='images/space.gif' width='1' height='1' alttext=''/></td>
</tr>
<dmf:datagridRow valign='center' columns='4'>
<td>
<dmfx:actionmultiselectcheckbox name='check' value='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='lockOwner' datafield='r_lock_owner'/>
<dmfx:argument name='folderId' contextvalue='objectId'/>
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
</dmfx:actionmultiselectcheckbox>
</td>
<td height=16 nowrap>
<dmf:stringlengthformatter maxlen='16'>
<dmf:link onclick='onClickObject' runatclient='true' datafield='object_name'>
<dmf:argument name='id' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='isFolder' datafield='isfolder'/>
</dmf:link>
</dmf:stringlengthformatter>
</td>
</tr>
<tr>
<td width=16 valign=top>
<dmfx:actionimage name='propact' nlsid='MSG_PROPERTIES' action='properties' src='icons/info.gif' showifdisabled='false'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
</dmfx:actionimage>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
</td>
<td valign=center>
<table border=1 cellspacing=0 cellpadding=2 width=100 height=100><tr align=center><td>
<dmf:columnpanel columnname='<%=DocList.THUMBNAIL_COLUMN%>'>
<dmf:image datafield='<%=DocList.THUMBNAIL_COLUMN%>' runatclient='true' onclick='onClickObject'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='isFolder' datafield='isfolder'/>
</dmf:image>
</dmf:columnpanel>
</td></tr></table>
</td>
</dmf:datagridRow>
<dmf:nodataRow>
<td colspan=20 height=24>
<dmf:label nlsid='MSG_NO_DOCUMENTS'/>
</td>
</dmf:nodataRow>
</dmf:datagrid>
</dmfx:actionmultiselect>
