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

 Filename       $RCSfile: importContent.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:42 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<%@ taglib uri="/WEB-INF/tlds/dmcontentxfer_1_0.tld" prefix="dmxfer"%>
<dmf:html>
<dmf:head>
    <dmf:webform />
</dmf:head>
<dmf:body cssclass='contentBackground'>
    <dmf:form>
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
            <tr>
                <td></td>
            </tr>
            <tr>
                <td><% // %> &nbsp;&nbsp;&nbsp<b><dmf:label
                    nlsid="MSG_FILE" />:&nbsp;</b><dmf:label
                    name="filename" /></td>
            </tr>
            <tr>
                <td></td>
            </tr>
        </table>
        <hr>
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
            <% // %>
            <tr>
                <td align="right">
                    <b><dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_NAME" />:&nbsp;</b>
                </td>
                <td align="left">
                    <dmf:text name="attribute_object_name" cssclass="defaultDocbaseAttributeStyle" size="30" tooltipnlsid="MSG_NAME" />
                </td>
            </tr>
            <% // %>
            <tr>
                <td scope="row" align="right">
                    <b><dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_TYPE" />:&nbsp;</b>
                </td>
                <td align="left">
                    <dmf:datadropdownlist name="objectTypeList" cssclass="defaultDocbaseAttributeStyle" onselect="onSelectType" tooltipnlsid="MSG_TYPE">
	                    <dmf:dataoptionlist>
	                        <dmf:option datafield="type_name" labeldatafield="label_text" />
	                    </dmf:dataoptionlist>
                    </dmf:datadropdownlist>
                </td>
            </tr>
            <% // %>
            <tr>
                <td scope="row" align="right" valign='top'>
                    <b><dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_FORMAT" />:&nbsp;</b>
                </td>
                <td align="left" valign='top'>
                    <dmf:datadropdownlist name="formatList" cssclass="defaultDocbaseAttributeStyle" tooltipnlsid="MSG_FORMAT">
	                    <dmf:dataoptionlist>
	                        <dmf:option datafield="name" labeldatafield="description" />
                        </dmf:dataoptionlist>
                    </dmf:datadropdownlist><br>
                    <dmf:label name='unknown_format_info_label' nlsid='MSG_ENFORCE_SELECT_FORMAT' cssclass='validatorMessageStyle' />
                </td>
            </tr>
            <% // %>
            <dmfx:docbaseobject name="obj" />
            <dmfx:docbaseattributelist name="attrlist" object="obj" attrconfigid="import" showcategorynames="false" pre="<tr><td align=\"right\"><b>" col1=":&nbsp;</b></td><td>"/>
                <tr>
                    <td scope="row" align="right">
                        <b><dmf:label name="xmlCategoryListLabel" cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_XML_CATEGORY" />&nbsp;</b>
                    </td>
                    <td align="left">
                        <dmf:datadropdownlist name="xmlCategoryList" cssclass="defaultDocbaseAttributeStyle" tooltipnlsid="MSG_XML_CATEGORY">
	                        <dmf:dataoptionlist>
	                            <dmf:option datafield="id" labeldatafield="description" />
	                        </dmf:dataoptionlist>
                        </dmf:datadropdownlist>
                    </td>
                </tr>
        </table>
    </dmf:form>
</dmf:body>
</dmf:html>
