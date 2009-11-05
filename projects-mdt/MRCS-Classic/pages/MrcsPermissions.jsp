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

 Filename       $RCSfile: MrcsPermissions.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:40 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/dmda_1_0.tld" prefix="dmda" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.webcomponent.library.permissions.Permissions" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<script language='JavaScript1.2' src='<%=Permissions.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
</dmf:head>
<dmf:body cssclass='contentBackground'>
<dmf:form>
<br/>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<tr>
<td align="right" scope="row">
<dmf:label nlsid="MSG_YOUR_PERMISSIONS" cssclass="defaultDocbaseAttributeStyle"/>
</td>
<td><b>:</b>&nbsp;</td>
<td colspan="2">
<dmf:label name="yourpermissions" cssclass="defaultDocbaseAttributeStyle"/>
</td>
</tr>
<tr>
<td colspan="4">&nbsp;</td>
</tr>
<tr>
<td align="right" scope="row">
<dmf:label nlsid="MSG_PERMISSION_SET" cssclass="defaultDocbaseAttributeStyle"/>
</td>
<td><b>:</b>&nbsp;</td>
<td>
<dmf:label name="permissionset" cssclass="defaultDocbaseAttributeStyle"/>
</td>
<td align="right">
</td>
</tr>
<tr>
<td align="right" scope="row">
<dmf:label nlsid="MSG_DESCRIPTION" cssclass="defaultDocbaseAttributeStyle"/>
</td>
<td><b>:</b>&nbsp;</td>
<td colspan="2">
<dmf:label name="description" cssclass="defaultDocbaseAttributeStyle"/>
</td>
</tr>
<tr>
<td align="right" scope="row">
<dmf:label nlsid="MSG_OWNER" cssclass="defaultDocbaseAttributeStyle"/>
</td>
<td><b>:</b>&nbsp;</td>
<td colspan="2">
<dmf:label name="owner" cssclass="defaultDocbaseAttributeStyle"/>
</td>
</tr>
<tr>
<td colspan="4">
<hr/>
</td>
</tr>
<tr>
<td colspan="4" scope="row">
<b><dmf:label nlsid="MSG_USER_GROUP_PERMISSIONS" cssclass="defaultDocbaseAttributeStyle"/>:</b>
</td>
</tr>
<tr>
<td colspan="4" height='6'>
</td>
</tr>
<tr>
<td colspan="4">
<dmfx:actionmultiselect name='multi'>
<dmf:datagrid name="accessorsgrid" cellspacing="0" cellpadding="0" bordersize="0" width="100%" paged='true' pagesize='5'>
<tr class='pagerBackground'>
<td colspan=20 align=center height=16>
<table width=100% border=0 cellspacing=0 cellpadding=0>
<tr valign=top>
<dmf:panel name="usergroupicons">
<td valign=top>
<dmfx:actionimage src="icons/add.gif"
action="addaceaction" name="addaceaction" tooltipnlsid="MSG_ADD" oncomplete="onCompleteAddAccessor">
<dmf:argument name='type' value='dm_ace'/>
<dmf:argument name='multiselect' value='true'/>
</dmfx:actionimage>
</td>
<td>
<dmfx:actionimage dynamic="multiselect" src="icons/edit.gif"
height='16' action="editaceaction" name="editaceaction" tooltipnlsid="MSG_EDIT" showifdisabled="true"/>
</td>
<td>
<dmfx:actionimage dynamic="multiselect" src="icons/trashcan.gif"
height='16' action="rmaccessor" name="rmaccessor" tooltipnlsid="MSG_REMOVE" showifdisabled="true"/>
</td>
</dmf:panel>
<td align=center width=100%><dmf:datapaging name='pager1' gotopageclass='doclistPager'/></td>
<td align=right nowrap>
<dmf:label nlsid='MSG_SHOW_ITEMS'/>&nbsp;<dmf:datapagesize name='sizer' preference='application.display.classic' tooltipnlsid='MSG_SHOW_ITEMS'/>
</td>
</tr>
</table>
</td>
</tr>
<tr>
<dmf:columnpanel columnname="checkbox">
<th scope='col' align='left' width=16 height=16>
<dmfx:actionmultiselectcheckall/>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='accessorPanel'>
<th scope='col' align='left'>
<b><dmf:datasortlink name='sort0' nlsid='MSG_ACCESSORS' column='r_accessor_name' cssclass='doclistbodyDatasortlink'/></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='permitPanel'>
<th scope='col' align='left'>
<b><dmf:datasortlink name='sort1' nlsid='MSG_PERMITS' column='r_accessor_permit' cssclass='doclistbodyDatasortlink'/></b>
</th>
</dmf:columnpanel>
<dmf:columnpanel columnname='extendedPanel'>
<th scope='col' align='left'>
<b><dmf:label nlsid='MSG_EXTENDED_PERMITS' cssclass='doclistbodyDatasortlink'/></b>
</th>
</dmf:columnpanel>
</tr>
<tr class='doclistbodySeparator'>
<td colspan='4' height='1' class='rowSeparator'><dmf:image src='images/space.gif' width='1' height='1' alttext=''/></td>
</tr>
<dmf:datagridRow cssclass="contentBackground" valign="top">
<dmf:columnpanel columnname="checkbox">
<td width=16 height=24>
<dmfx:actionmultiselectcheckbox name='checkbox' value='false'>
<dmf:argument name='type' value='dm_ace'/>
<dmf:argument name='accessorName' datafield='r_accessor_name'/>
<dmf:argument name='basicpermit' datafield='r_accessor_permit'/>
<dmf:argument name='extpermit' datafield='r_accessor_xpermit'/>
</dmfx:actionmultiselectcheckbox>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='accessorPanel'>
<td valign="top" scope='row'>
<dmf:label datafield='r_accessor_name'/>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='permitPanel'>
<td valign="top">
<dmda:permissionvalueformatter>
<dmf:label datafield='r_accessor_permit'/>
</dmda:permissionvalueformatter>
</td>
</dmf:columnpanel>
<dmf:columnpanel columnname='extendedPanel'>
<td valign='top' >
<dmda:extpermvalueformatter>
<dmf:label datafield='r_accessor_xpermit'/>
</dmda:extpermvalueformatter>
</td>
</dmf:columnpanel>
</tr>
<tr class='doclistbodySeparator'>
<td colspan='4' height='1' class='rowSeparator'><dmf:image src='images/space.gif' width='1' height='1' alttext=''/></td>
</tr>
</dmf:datagridRow>
<dmf:nodataRow height='24' cssclass='contentBackground'>
<td>
<dmf:label nlsid='MSG_NO_DATA'/>
</td>
</dmf:nodataRow>
</dmf:datagrid>
</dmfx:actionmultiselect>
</td>
</tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
