<%@ page import="com.documentum.webcomponent.navigation.doclist.DocList"%>
<%
//
%>
<div id="logo"><center><img height="40" width="206" alt="Medtronic logo" src="http://www.medtronic.com/wcm/groups/mdtcom_sg/@shared/documents/images/mdt-logo.png" /></center></div>

<dmfx:actionmultiselect name='multi'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='folderId' contextvalue='folderId'/>
<dmfx:argument name='folderPath' contextvalue='folderPath'/>
<dmf:argument name='startworkflowId' value='startworkflow'/>
<dmf:datagrid name='<%=DocList.CONTROL_GRID%>' paged='true' preservesort='false'
width='100%' cellspacing='0' cellpadding='0' bordersize='0'>
<tr class='pagerBackground'>
<td colspan=20 align=center height=24>
  <table width=100% border=0 cellspacing=0 cellpadding=0>
    <tr>
      <td align=center width=100%><dmf:datapaging name='pager1' gotopageclass='doclistPager'/></td>
      <td align=right nowrap><dmf:label nlsid='MSG_SHOW_ITEMS'/> <dmf:datapagesize name='sizer' preference='application.display.classic' tooltipnlsid='MSG_SHOW_ITEMS'/></td>
    </tr>
  </table>
</td>
</tr>
<tr>
<th scope='col' align='left'>
<dmf:datasortimage name='sortimg' datafield='r_lock_owner' cssclass='doclistbodyDatasortlink' reversesort='true' image='icons/sort/sortByLock.gif'/>
</th>
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<th scope='col' align='left' nowrap class='doclistfilenamedatagrid objectlistheaderspacing'>
<dmf:datasortlink name='sort1' datafield='object_name' cssclass='doclistbodyDatasortlink'/>
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
<td nowrap class="doclistlocicon">
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
<dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' size='16'/>
</nobr>
</td>
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<td nowrap scope='row' class='doclistfilenamedatagrid'>
<dmf:stringlengthformatter maxlen='32'>
<dmf:link onclick='onClickObject' name='objectLink' runatclient='true' datafield='object_name' >
<dmf:argument name='id' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='isFolder' datafield='isfolder'/>
</dmf:link>
</dmf:stringlengthformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='title'>
<td class='doclistfilenamedatagrid'>
<dmfx:folderexclusionformatter datafield='r_object_type'>
<dmf:stringlengthformatter maxlen='14'>
<dmf:label datafield='title'/>
</dmf:stringlengthformatter>
</dmfx:folderexclusionformatter>
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
<dmf:datevalueformatter type='short'>
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
</dmfx:actionmultiselect>
<dmf:hidden name='<%=DocList.CONTROL_FOLDERPATH%>'/>
<center>
<table border='0' cellpadding='0' cellspacing='0' >
<div id="footer">
&copy; 2008 Medtronic, Inc.
</div>
</table>
</center>
