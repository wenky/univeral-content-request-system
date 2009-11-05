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

 Filename       $RCSfile: MrcsWorkflowTemplateLocator.jsp,v $
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
<%@ page import="com.documentum.web.form.Form,
com.documentum.web.common.BrandingService" %>
<%@ page import="com.documentum.web.form.control.Hidden" %>
<%@ page import="com.documentum.webcomponent.library.locator.LocatorContainer" %>
<%@ page import="com.documentum.webcomponent.library.locator.ObjectLocator" %>
<%@ page import="com.documentum.webcomponent.library.locator.SysObjectLocator" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/webcomponent/library/locator/locator.js")%>'></script>
<%
ObjectLocator locatorComp = (ObjectLocator)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
%>
<script>setInLocatorContainer(<%=locatorComp.getTopForm() instanceof LocatorContainer ? "true" : "false"%>);</script>
<script>setMultiSelectEnabled(<%=locatorComp.isMultiSelectEnabled() ? "true" : "false"%>);</script>
</dmf:head>
<dmf:body cssclass='contentBackground'>
<dmf:form>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<dmf:panel name='<%=SysObjectLocator.PANEL_BREADCRUMB%>'>
<tr><td valign='top' colspan='3'>
<table align='left' valign='top' width='100%' cellspacing='0' cellpadding='6' border='0'>
<tr>
<td align='left' valign='top'>
<dmf:breadcrumb name='<%=SysObjectLocator.BREADCRUMB%>' cssclass='drilldownFolderPath' onclick='onClickBreadcrumb' displayleaf='true'/>
</td>
</tr>
</table>
</td></tr>
</dmf:panel>
<tr valign='top'>
<td valign='top'>
<dmf:panel name='<%=ObjectLocator.NAMEFILTER_PANEL%>'>
<table align='left' valign='top' cellspacing='0' cellpadding='6' border='0'>
<tr valign='top'>
<td scope="row" align='left' valign='center'>
<b><dmf:label cssclass="defaultLabelStyle" nlsid="MSG_FILTER"/>&nbsp;</b>
</td>
<td align='left' valign='top'>
<dmf:text name="<%=SysObjectLocator.NAMEFILTERSTRING%>" size="25" defaultonenter="true" tooltipnlsid="MSG_FILTER"/>
</td>
<td align='left' valign='top'>
<dmf:button default="true" nlsid="MSG_GO" onclick="onClickJumpTo" tooltipnlsid="MSG_GO"/>
</td>
</tr>
</table>
</dmf:panel>
</td>
<dmf:panel name='<%=ObjectLocator.REMOVEHEADER_PANEL%>'>
<td valign='top' width=60>
</td>
<td valign='top'>
<b><dmf:label cssclass="defaultLabelStyle" nlsid="MSG_SELECTED_ITEMS"/>&nbsp;</b>
</td>
</dmf:panel>
</tr>
<tr valign='top'>
<td align='left' valign='top'>
<dmf:datagrid name='<%=ObjectLocator.ADDOBJECT_GRID%>' paged='true' preservesort='false'
cssclass='doclistbodyDatagrid' width='100%' cellspacing='0' cellpadding='0' bordersize='0'>
<tr valign=top height=24>
<td align=left colspan=20 nowrap>
<dmf:datapaging name='pagerAdd' showdisplayinginfo='false'/>
</td>
</tr>
<tr height=24>
<td align=left colspan=20 nowrap>
<dmf:label cssclass='drilldownFileInfo' nlsid='MSG_SHOW_ITEMS'/> <dmf:datapagesize name='sizer' preference='application.display.classic' tooltipnlsid='MSG_SHOW_ITEMS'/>
</td>
</tr>
<tr height=4><td></td></tr>
<tr valign='top'>
<th scope='col' width=16 height=16>
&nbsp;
</th>
<th scope='col' width=40>
</th>
<th style="display:none">
&nbsp;
</th>
<th style="display:none">
&nbsp;
</th>
<th style="display:none">
&nbsp;
</th>
<th scope='col' align='left' width=250>
<b><dmf:datasortlink name='ad_sort_nm' nlsid='MSG_OBJECT_NAME' column='object_name' cssclass='doclistbodyDatasortlink'/></b>
</th>
<dmf:columnpanel columnname='r_version_label'>
<th scope='col' align='left' width=75>
<b><dmf:label name='ad_sort_vl' nlsid='MSG_VERSION_LABEL' cssclass='doclistbodyLabel'/></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='owner_name'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='ad_sort_on' nlsid='MSG_OWNER_NAME' column='owner_name' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='group_name'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='ad_sort_gn' nlsid='MSG_GROUP_NAME' column='group_name' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creator_name'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='ad_sort_cn' nlsid='MSG_CREATOR_NAME' column='r_creator_name' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_object_type'>
<th scope='col' align='left' width=75>
<b><dmf:datasortlink name='ad_sort_ty' nlsid='MSG_OBJECT_TYPE' column='r_object_type' cssclass='doclistbodyDatasortlink'/></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creation_date'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='ad_sort_cd' nlsid='MSG_CREATION_DATE' column='r_creation_date' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modify_date'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='ad_sort_md' nlsid='MSG_MODIFIED_DATE' column='r_modify_date' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modifier'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='ad_sort_mod' nlsid='MSG_MODIFIER' column='r_modifier' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_access_date'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='ad_sort_ad' nlsid='MSG_ACCESS_DATE' column='r_access_date' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:columnpanel>
<% // %>
<th scope='col' align='left' width=1>
</th>
</tr>
<tr class='doclistbodySeparator'>
<td colspan='20' height='1' class='rowSeparator'><dmf:image src='images/space.gif' width='1' height='1' alttext=''/></td>
</tr>
<dmf:hidden name='<%=ObjectLocator.ADDOBJIDS%>' id='addObjIds' onchange='onAddedObjectsChanged'/>
<dmf:datagridRow height='24' cssclass='contentBackground'>
<td width=16 height=24>
<dmf:panel datafield='selectable'>
<dmf:checkbox value='false' onclick='onAddCellClicked' runatclient='true'/>
</dmf:panel>
</td>
<td style="display:none">
<dmf:hidden datafield='r_object_id'/>
</td>
<td style="display:none">
<dmf:hidden datafield='object_name'/>
</td>
<td style="display:none">
<dmf:hidden datafield='r_version_label'/>
</td>
<td width=40 nowrap>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
<dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' size='16'/>
</td>
<td align='left' scope='row'>
<dmf:panel datafield='navigatable'>
<dmf:stringlengthformatter maxlen="40">
<dmf:link onclick='onClickContainer' datafield='object_name' tooltipdatafield='object_name'>
<dmf:argument name='objectId' datafield='r_object_id'/>
<dmf:argument name='objectName' datafield='object_name'/>
</dmf:link>
</dmf:stringlengthformatter>
</dmf:panel>
<dmf:panel datafield='notnavigatable'>
<dmf:stringlengthformatter maxlen="40">
<dmf:label datafield='object_name'/>
</dmf:stringlengthformatter>
</dmf:panel>
</td>
<dmf:columnpanel columnname='r_version_label'>
<td nowrap>
<dmfx:folderexclusionformatter datafield='r_object_type'>
<dmf:label datafield='r_version_label'/>
</dmfx:folderexclusionformatter>&nbsp;
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='owner_name'>
<td nowrap>
<dmf:label datafield='owner_name'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='group_name'>
<td nowrap>
<dmf:label datafield='group_name'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creator_name'>
<td nowrap>
<dmf:label datafield='r_creator_name'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_object_type'>
<td nowrap>
<dmf:label datafield='r_object_type'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creation_date'>
<td nowrap>
<dmf:datevalueformatter type='short'>
<dmf:label datafield='r_creation_date'/>
</dmf:datevalueformatter>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modify_date'>
<td nowrap>
<dmf:datevalueformatter type='short'>
<dmf:label datafield='r_modify_date'/>
</dmf:datevalueformatter>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modifier'>
<td nowrap>
<dmf:label datafield='r_modifier'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_access_date'>
<td nowrap>
<dmf:datevalueformatter type='short'>
<dmf:label datafield='r_access_date'/>
</dmf:datevalueformatter>
</td>
</dmf:columnpanel>
<% // %>
<td width=1><img src='<%=BrandingService.getThemeResolver().getResourcePath("images/space.gif", pageContext, false)%>' width='1' height='1' onload='checkAddCell(this)' alt=''></td>
</dmf:datagridRow>
<dmf:nodataRow height='24' cssclass='contentBackground'>
<td colspan=20 valign='top'>
<dmf:label nlsid='MSG_NO_DATA'/>
</td>
</dmf:nodataRow>
<tr class='contentBackground'>
<td colspan=20 valign=bottom height=100%>&nbsp;</td>
</tr>
<dmf:panel name='<%=SysObjectLocator.PANEL_ATTRIBUTEFILTERS+"0"%>'>
<tr valign='top'>
<td align='left' valign='center' colspan=20>
<dmf:datadropdownlist name='<%=SysObjectLocator.DROPDOWN_ATTRIBUTEFILTERS+"0"%>' width='200' onselect="onChangeAttributeFilter" tooltipnlsid="MSG_FILTER_1" >
<dmf:dataoptionlist>
<dmf:option datafield="filterid" labeldatafield="name"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
</dmf:panel>
<dmf:panel name='<%=SysObjectLocator.PANEL_ATTRIBUTEFILTERS+"1"%>'>
<tr valign='top'>
<td align='left' valign='center' colspan=20>
<dmf:datadropdownlist name='<%=SysObjectLocator.DROPDOWN_ATTRIBUTEFILTERS+"1"%>' width='200' onselect="onChangeAttributeFilter"  tooltipnlsid="MSG_FILTER_2">
<dmf:dataoptionlist>
<dmf:option datafield="filterid" labeldatafield="name"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
</dmf:panel>
<dmf:panel name='<%=SysObjectLocator.PANEL_ATTRIBUTEFILTERS+"2"%>'>
<tr valign='top'>
<td align='left' valign='center' colspan=20>
<dmf:datadropdownlist name='<%=SysObjectLocator.DROPDOWN_ATTRIBUTEFILTERS+"2"%>' width='200' onselect="onChangeAttributeFilter"  tooltipnlsid="MSG_FILTER_3">
<dmf:dataoptionlist>
<dmf:option datafield="filterid" labeldatafield="name"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
</dmf:panel>
<dmf:panel name='<%=SysObjectLocator.PANEL_VERSIONFILTERS%>'>
<tr valign='top'>
<td align='left' valign='center' colspan=20>
<dmf:dropdownlist name='<%=SysObjectLocator.DROPDOWN_VERSIONFILTERS%>' width='200' onselect="onChangeVersionFilter" tooltipnlsid="MSG_VERSION_FILTER" >
<dmf:option value='CURRENT' nlsid='MSG_CURRENT_VERSIONS'/>
<dmf:option value='ALL' nlsid='MSG_ALL_VERSIONS'/>
</dmf:dropdownlist>
</td>
</tr>
</dmf:panel>
</dmf:datagrid>
</td>
<dmf:panel name='<%=ObjectLocator.REMOVEBODY_PANEL%>'>
<td valign='top'>
<table align='center' valign='top' cellspacing='20' cellpadding='20' border='0' width='100%'>
<tr height=48><td></td></tr>
<tr><td><dmf:button default="true" nlsid="MSG_ADD" id='addbtn' onclick="onClickAdd" enabled='false'  tooltipnlsid="MSG_ADD"/></td></tr>
<tr><td><dmf:button default="true" nlsid="MSG_REMOVE" id='removebtn' onclick="onClickRemove" enabled='false' tooltipnlsid="MSG_REMOVE"/></td></tr>
</table>
</td>
<td valign='top'>
<dmf:datagrid name='<%=ObjectLocator.REMOVEOBJECT_GRID%>' paged='true' preservesort='false'
cssclass='doclistbodyDatagrid' width='100%' cellspacing='0' cellpadding='0' bordersize='0'>
<tr height=48>
<td valign=top align=left colspan=20 nowrap>
<dmf:datapaging name='pagerRemove' showdisplayinginfo='false'/>
</td>
</tr>
<tr height=4><td></td></tr>
<tr valign='top'>
<th scope='col' width=16 height=16>
&nbsp;
</th>
<th scope='col' width=40>
</th>
<th scope='col' align='left' width=250>
<b><dmf:datasortlink name='rm_sort_nm' nlsid='MSG_OBJECT_NAME' column='object_name' cssclass='doclistbodyDatasortlink'/></b>
</th>
<dmf:columnpanel columnname='location'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_location' nlsid='MSG_LOCATION' column='location' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_version_label'>
<th scope='col' align='left' width=75>
<b><dmf:label name='rm_sort_vl' nlsid='MSG_VERSION_LABEL' cssclass='doclistbodyLabel'/></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='owner_name'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_on' nlsid='MSG_OWNER_NAME' column='owner_name' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='group_name'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_gn' nlsid='MSG_GROUP_NAME' column='group_name' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creator_name'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_cn' nlsid='MSG_CREATOR_NAME' column='r_creator_name' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_object_type'>
<th scope='col' align='left' width=75>
<b><dmf:datasortlink name='rm_sort_ty' nlsid='MSG_OBJECT_TYPE' column='r_object_type' cssclass='doclistbodyDatasortlink'/></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creation_date'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_cd' nlsid='MSG_CREATION_DATE' column='r_creation_date' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modify_date'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_md' nlsid='MSG_MODIFIED_DATE' column='r_modify_date' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modifier'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_mod' nlsid='MSG_MODIFIER' column='r_modifier' cssclass='doclistbodyDatasortlink'/><nobr></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_access_date'>
<th scope='col' align='left' width=100>
<b><nobr><dmf:datasortlink name='rm_sort_ad' nlsid='MSG_ACCESS_DATE' column='r_access_date' mode='numeric' cssclass='doclistbodyDatasortlink'/></nobr></b>
</th>
</dmf:columnpanel>
</tr>
<tr class='doclistbodySeparator'>
<td colspan='20' height='1' class='rowSeparator'><dmf:image src='images/space.gif' width='1' height='1' alttext=''/></td>
</tr>
<dmf:hidden name='<%=SysObjectLocator.REMOVEOBJIDS%>' id='removeObjIds'/>
<dmf:datagridRow height='24' cssclass='contentBackground'>
<td width=16 height=24>
<dmf:checkbox value='false' onclick='onRemoveCellClicked' runatclient='true'/>
</td>
<td style="display:none">
<dmf:hidden datafield='r_object_id'/>
</td>
<td width=40 nowrap>
<dmfx:docbaselockicon datafield='r_lock_owner' size='16'/>
<dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' size='16'/>
</td>
<td align='left'>
<dmf:stringlengthformatter maxlen="40">
<dmf:label datafield='object_name'/>
</dmf:stringlengthformatter>
</td>
<dmf:columnpanel columnname='location'>
<td nowrap>
<dmf:label datafield='location'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_version_label'>
<td nowrap>
<dmfx:folderexclusionformatter datafield='r_object_type'>
<dmf:label datafield='r_version_label'/>
</dmfx:folderexclusionformatter>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='owner_name'>
<td nowrap>
<dmf:label datafield='owner_name'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='group_name'>
<td nowrap>
<dmf:label datafield='group_name'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creator_name'>
<td nowrap>
<dmf:label datafield='r_creator_name'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_object_type'>
<td nowrap>
<dmf:label datafield='r_object_type'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_creation_date'>
<td nowrap>
<dmf:datevalueformatter type='short'>
<dmf:label datafield='r_creation_date'/>
</dmf:datevalueformatter>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modify_date'>
<td nowrap>
<dmf:datevalueformatter type='short'>
<dmf:label datafield='r_modify_date'/>
</dmf:datevalueformatter>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_modifier'>
<td nowrap>
<dmf:label datafield='r_modifier'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='r_access_date'>
<td nowrap>
<dmf:datevalueformatter type='short'>
<dmf:label datafield='r_access_date'/>
</dmf:datevalueformatter>
</td>
</dmf:columnpanel>
</dmf:datagridRow>
<dmf:nodataRow height='24' cssclass='contentBackground'>
<td colspan=20 valign='top'>
<dmf:label nlsid='MSG_NO_DATA'/>
</td>
</dmf:nodataRow>
<tr class='contentBackground'>
<td colspan=20 valign=bottom height=100%>&nbsp;</td>
</tr>
</dmf:datagrid>
</td>
</dmf:panel> <% // %>
</tr>
</table>
<% // %>
<img src='<%=BrandingService.getThemeResolver().getResourcePath("images/space.gif", pageContext, false)%>' width='1' height='1' onload='initAddList()' alt=''>
</dmf:form>
</dmf:body>
</dmf:html>
