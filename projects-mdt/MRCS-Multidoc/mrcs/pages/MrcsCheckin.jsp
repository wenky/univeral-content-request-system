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

 Filename       $RCSfile: MrcsCheckin.jsp,v $
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
<dmf:webform/>
</dmf:head>
<dmf:body cssclass='contentBackground'>
<dmf:form>
<dmfx:docbaseobject name="object" modifyonversion="true"/>
<table border="0" cellpadding="2" cellspacing="0">
<tr>
<td align=left valign=top width="100%" >
<table border="0" cellpadding="2" cellspacing="0">
<tr>
<td><dmfx:docbaseicon size='32' name="obj_icon"/></td>
<td scope="row"><b><dmfx:docbaseattributevalue object="object" name="attribute_object_name" attribute="object_name" size="57" readonly="true"/></b></td>
</tr>
<tr>
<td scope="row"><b><dmf:label name="version" nlsid="MSG_VERSION" cssclass="defaultDocbaseAttributeLabelStyle"/></b></td>
<td><dmfx:docbaseattributevalue object="object" attribute="r_version_label" readonly="true"/></td>
</tr>
<tr>
<td scope="row"><dmfx:docbaseattribute object="object" attribute="r_object_type" readonly="true"/></td>
</tr>
<tr>
<td scope="row"><dmfx:docbaseattribute object="object" attribute="a_content_type" readonly="true"/></td>
</tr>
</table>
</td>
</tr>
</table>
<br>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<tr>
<td></td>
<td></td>
<td>
<dmf:label name="checkinfromfilelabelerrormessage" cssclass="validatorMessageStyle"/>
</td>
</tr>
</table>
<br>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<% // %>
<dmf:panel name="existingobjversion">
<tr>
<td scope="row" align=right nowrap>
<b><dmf:label nlsid="MSG_SAVE_AS" cssclass="defaultDocbaseAttributeLabelStyle"/>:&nbsp;</b>
</td>
<dmf:panel name="sameversionpanel">
<td><dmf:radio name="sameversion" group="group1" value="false" tooltipnlsid="MSG_SAME_VERSION" />&nbsp;<dmf:label name="sameversionnum"/></td>
</dmf:panel>
</tr>
<dmf:panel name="minorversionpanel">
<tr><td></td><td scope="row"><dmf:radio name="minorversion" group="group1" value="true"  tooltipnlsid="MSG_MINOR_VERSION"/>&nbsp;<dmf:label name="minorversionnum"/></td></tr>
</dmf:panel>
<dmf:panel name="majorversionpanel">
<tr><td></td><td scope="row"><dmf:radio name="majorversion" group="group1" value="false"  tooltipnlsid="MSG_MAJOR_VERSION"/>&nbsp;<dmf:label name="majorversionnum"/></td></tr>
</dmf:panel>
</dmf:panel>
<dmf:panel name="newobjversion">
<tr>
<td scope="row" align=right nowrap>
<b><dmf:label nlsid="MSG_SAVE_AS" cssclass="defaultDocbaseAttributeLabelStyle"/>:&nbsp;</b>
</td>
<td><dmf:radio name="newversion" group="group2" value="true" tooltipnlsid="MSG_NEW_VERSION" />&nbsp;<dmf:label name="newversionnum"/></td>
</tr>
</dmf:panel>
<dmf:panel name="branchversion">
<tr>
<td scope="row" align=right nowrap>
<b><dmf:label nlsid="MSG_SAVE_AS" cssclass="defaultDocbaseAttributeLabelStyle"/>:&nbsp;</b>
</td>
<td><dmf:radio name="branchrevision" group="group3" value="true" tooltipnlsid="MSG_BRANCH_REVISION" />&nbsp;<dmf:label name="branchrevisionnum"/></td>
</tr>
</dmf:panel>
<% // %>
<dmf:panel name="versionlabelpanel">
<tr>
<td scope="row" valign=top align=right >
<b><dmf:label nlsid="MSG_VERSION_LABEL" cssclass="defaultDocbaseAttributeLabelStyle"/>:</b>
</td>
<td>
<dmf:text name="versionlabel" size="57" tooltipnlsid="MSG_VERSION_LABEL"/>
<br><dmf:regexpvalidator name="version_validator" controltovalidate="versionlabel" expression=".{0,31}" nlsid="MSG_VERSION_LABEL_TOO_LONG"/>
<dmfx:symbolicversionlabelvalidator name="symbolicversionlabel_validator" controltovalidate="versionlabel"/>
<dmf:label name="versionlabelerrormessage" nlsid="MSG_VERSION_LABEL_NEEDED" cssclass="validatorMessageStyle"/>
</td>
</tr>
</dmf:panel>
<% // %>
<tr>
<dmf:panel name="descriptionspanel">
<td scope="row" align=right valign=top >
<b><dmf:label nlsid="MSG_DESCRIPTION" cssclass="defaultDocbaseAttributeLabelStyle"/>:</b>
</td>
</dmf:panel>
<td>
<dmfx:docbaseattributevalue object="object" name="attr_value_description" attribute="log_entry" readonly="false" size="80"/>
<dmf:regexpvalidator name="validator" controltovalidate="attr_value_description" expression=".{1,80}" nlsid="MSG_DESC_TOO_LONG"/>
<dmf:panel name="descreqpanel">
<dmf:requiredfieldvalidator name="reqDescValidator" controltovalidate= "attr_value_description" nlsid= "MSG_DESC_REQ" /><br>
</dmf:panel>
</td>
</tr>
<% // %>
<tr>
<td scope="row" align="right" valign='top'>
<b><dmf:label nlsid="MSG_FORMAT" cssclass="defaultDocbaseAttributeLabelStyle"/>:&nbsp;</b>
</td>
<td align="left" valign='top' >
<dmf:datadropdownlist name="formatlist"  tooltipnlsid="MSG_FORMAT">
<dmf:dataoptionlist>
<dmf:option datafield="name" labeldatafield="description"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
<br><dmf:label name='unknown_format_info_label' nlsid='MSG_ENFORCE_SELECT_FORMAT' cssclass="validatorMessageStyle" />
</td>
</tr>
<dmfx:docbaseattributelist name="attrlist" object="object" attrconfigid="checkin" showcategorynames="false" pre="<tr><td align=\"right\"><b>" col1=":&nbsp;</b></td><td>" readonly="false" />
<% // %>
<dmf:panel name="fulltext">
<tr>
<td scope="row" align="right" nowrap>
<b><dmfx:docbaseattributelabel object="object" attribute="a_full_text"/>:&nbsp;</b>
</td>
<td align="left">
<dmfx:docbaseattributevalue object="object" attribute="a_full_text" readonly="false" />
</td>
</tr>
</dmf:panel>
</table>
<br>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<% // %>
<tr>
<td></td>
<td scope="row" colspan="2" nowrap>
<dmf:link name="showhideoptions" onclick="onClickShowHideOptions"/>
</td>
</tr>
<dmf:panel name="optionspanel">
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="retainlock" value="false" nlsid="MSG_RETAIN_LOCK"/>
</td>
</tr>
<tr>
<td></td>
<td></td>
<dmf:panel name="makecurrentpanel">
<td scope="row">
<dmf:checkbox name="makecurrent" value="true" nlsid="MSG_MAKE_CURRENT"
onclick="onClickMakeCurrent"/>
</td>
</dmf:panel>
</tr>
<dmf:panel name="keeplocalfilepanel">
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="keeplocalfile" value="false" nlsid="MSG_KEEP_LOCAL_FILE"/>
</td>
</tr>
</dmf:panel>
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="subscribe" nlsid="MSG_SUBSCRIBE_TO_FILE"/>
</td>
</tr>
<dmf:panel name="vdmoptions">
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="checkindescendents" nlsid="MSG_CHECKIN_DESCENDENTS"/>
</td>
</tr>
</dmf:panel>
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="checkinfromfile" nlsid="MSG_CHECKIN_FROM_FILE"/>&nbsp;
<dmf:panel name="httpoptions">
<dmf:filebrowse name="filebrowse" size="50" nlsid="MSG_BROWSE" />
</dmf:panel>
<dmf:panel name="appletoptions">
<dmf:filebrowse name="filebrowse" size="50" onselect='onSelectFileBrowse' nlsid="MSG_BROWSE" />
</dmf:panel>
<dmf:label name="filebrowselabel"/>
</td>
</tr>
</dmf:panel>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
