<%
	System.out.println("I am in c prlcustomdoclist_body.jsp");
%>

<%@ page import="com.documentum.webcomponent.navigation.doclist.DocList"%>
<%
//
%>
<center>
<table border='0' cellpadding='0' cellspacing='0' >
	<tr><td align="center"><P><IMG alt="" src="/webtop/mrcs/pages/prlobjectlist/prl_header.gif"></P></td></tr>
	<tr><td >&nbsp</td></tr>
</table>
</center>
<dmfx:actionmultiselect name='multi'>
<dmfx:argument name='objectId' contextvalue='objectId'/>
<dmfx:argument name='type' contextvalue='type'/>
<dmfx:argument name='folderId' contextvalue='folderId'/>
<dmfx:argument name='folderPath' contextvalue='folderPath'/>
<dmf:argument name='startworkflowId' value='startworkflow'/>
<dmf:datagrid name='<%=DocList.CONTROL_GRID%>' paged='true' preservesort='false'
cssclass='doclistbodyDatagrid' width='100%' cellspacing='0' cellpadding='0' bordersize='0'>
<tr class=pagerBackground>
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
<b><dmf:datasortimage name='sortimg' datafield='r_lock_owner' cssclass='doclistbodyDatasortlink' reversesort='true' image='icons/sort/sortByLock.gif'/></b>
</th>
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<th scope='col' align='left' width=150 class='doclistbodyHeaderPadding'>
&nbsp;&nbsp;<b><dmf:datasortlink name='sort1' datafield='object_name' cssclass='doclistbodyDatasortlink'/></b>
</th>
</dmf:celltemplate>
<dmf:celltemplate type='number'>
<th scope='col' align='left' width=50>
<b><nobr><dmf:datasortlink name='sort4' datafield='CURRENT' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:celltemplate>
<dmf:celltemplate type='date'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='sort5' datafield='CURRENT' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:celltemplate>
<dmf:celltemplate>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='sort6' datafield='CURRENT' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:celltemplate>
</dmf:celllist>
</tr>
<tr height=1 class=doclistbodySeparator>
<td colspan=20 class=rowSeparator><dmf:image src='images/space.gif' width='1' height='1' alttext=''/></td>
</tr>
<dmf:datagridRow tooltipdatafield='object_name'>
<td width=40>
<nobr>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
<dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' size='16'/>
</nobr>
</td>
<dmf:celllist>
<dmf:celltemplate field='object_name'>
<td width=300 align = 'left' nowrap scope='row'>
<dmf:stringlengthformatter maxlen='64'>
<dmf:link onclick='onClickObject' runatclient='true' datafield='object_name'>
<dmf:argument name='id' datafield='r_object_id'/>
<dmf:argument name='type' datafield='r_object_type'/>
<dmf:argument name='isFolder' datafield='isfolder'/>
</dmf:link>
</dmf:stringlengthformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='a_content_type'>
<td>
<dmfx:docformatvalueformatter>
<dmf:label datafield='CURRENT'/>
</dmfx:docformatvalueformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate field='r_content_size'>
<td nowrap>
<dmfx:docsizevalueformatter datafield='r_object_type'>
<dmf:label datafield='r_content_size'/>
</dmfx:docsizevalueformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate type='date'>
<td nowrap>
<dmf:datevalueformatter type='short'>
<dmf:label datafield='CURRENT'/>
</dmf:datevalueformatter>
</td>
</dmf:celltemplate>
<dmf:celltemplate>
<td nowrap>
<dmf:label datafield='CURRENT'/>
</td>
</dmf:celltemplate>
</dmf:celllist>
</tr>
<tr height=1 class=doclistbodySeparator>
<td colspan=20 class=rowSeparator><dmf:image src='images/space.gif' width='1' height='1' alttext=''/></td>
</dmf:datagridRow>
<dmf:nodataRow>
<td colspan=20 height=24>
<dmf:label nlsid='MSG_NO_DOCUMENTS'/>
</td>
</dmf:nodataRow>
</dmf:datagrid>
</dmfx:actionmultiselect>
<dmf:hidden name='<%=DocList.CONTROL_FOLDERPATH%>'/>
<center>
<table border='0' cellpadding='0' cellspacing='0' >
	<tr><td>&nbsp</td></tr>
	<tr><td align="center"><P><IMG alt="" src="/webtop/mrcs/pages/prlobjectlist/prl_footer.GIF"></P></td></tr>
</table>
</center>
