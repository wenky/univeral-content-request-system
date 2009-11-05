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

 Filename       $RCSfile: MrcsAttributesAll.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:39 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<%--
--%>
<dmf:webform/>
</dmf:head>
<dmf:body cssclass='contentBackground'>
<dmf:form>
<%-- the Docbase object --%>
<dmfx:docbaseobject name="obj"/>
<%-- datagrid containing all attribute --%>
<dmf:datagrid name="allPropertiesGrid" paged="false" cellspacing="1" cellpadding="1" bordersize="0" width="100%" >
<%-- show all attributes checkbox --%>
<tr><td scope="row" colspan="3" align=right>
<dmf:checkbox name='show_all' onclick='onShowAllClicked' nlsid='MSG_SHOW_ALL_PROPERTIES'/>
</td></tr>
<%-- header block with column titles --%>
<dmf:row cssclass="contentBackground" valign="top">
<th align="right" style="font-family:Arial;font-size:11pt" scope='col'>
<b><nobr><dmf:label nlsid="MSG_ATTRIBUTE_NAME"/></nobr></b>
</th>
<th  scope='col'>
</th>
<th align="left" style="font-family:Arial;font-size:11pt" scope='col'>
<b><nobr><dmf:label nlsid="MSG_ATTRIBUTE_VALUE"/></nobr></b>
</th>
</dmf:row>
<dmf:row cssclass="contentBackground">
<td colspan="3">&nbsp;</td>
</dmf:row>
<%-- object id: if you prefer to see the r_object_id always at the top  --%>
<%-- of the properties page, uncomment the dmf:row block below. --%>
<%-- comment begin ----------------------------------------------
<dmf:row cssclass="contentBackground" valign="top">
<td align="right">
<nobr><b><dmfx:docbaseattributelabel object="obj" attribute="r_object_id"/></b></nobr>
</td>
<td>
<b>:</b>&nbsp;
</td>
<td align="left">
<dmfx:docbaseattributevalue object="obj" attribute="r_object_id"/>
</td>
</dmf:row>
------------------------------------------------- comment end --%>
<%-- row block attribute details --%>
<dmf:datagridRow cssclass="contentBackground">
<td align="right" valign="top" scope='row'>
<nobr><dmf:label name='sort_attr_label' datafield="label_text" cssclass='defaultDocbaseAttributeLabelStyle' /></nobr>
</td>
<td valign="top">:&nbsp;</td>
<td align="left">
<dmfx:docbaseattributevalue name='attr_value' size="48" object="obj" datafield="attr_name"/>
</td>
</dmf:datagridRow>
</dmf:datagrid>
</dmf:form>
</dmf:body>
</dmf:html>
