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

 Filename       $RCSfile: MrcsImportContent.jsp,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2006/10/13 20:31:47 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ page import="com.medtronic.documentum.mrcs.client.MrcsImportContentUCF" %>
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
                <td>
                    &nbsp;&nbsp;&nbsp;<b><dmf:label nlsid="MSG_FILE" />:&nbsp;</b><dmf:label name="filename" />
                </td>
            </tr>
            <tr>
                <td></td>
            </tr>
        </table>
        <hr>
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
            <tr>
                <td scope="row" align="right">
                    <b><dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_TYPE" />:&nbsp;</b>
                </td>
                <td align="left">
<% com.documentum.fc.common.DfLogger.debug(MrcsImportContentUCF.class,"importcontentjsp - mrcsdoctypes",null,null); %>
                    <dmf:dropdownlist name="mrcsdoctypes" onselect="onMrcsSelectType" tooltipnlsid="MSG_TYPE_COLON"/>
                </td>
            </tr>
            <tr>
                <td scope="row" align="right">
                </td>
                <td align="left">
                    <!--  dmf-text name="attribute_object_name" cssclass="defaultDocbaseAttributeStyle" size="51" tooltipnlsid="MSG_NAME" / -->                    
<% com.documentum.fc.common.DfLogger.debug(MrcsImportContentUCF.class,"importcontentjsp - objecttypelist",null,null); %>
                    <dmf:datadropdownlist width="270" name="objectTypeList" cssclass="defaultDocbaseAttributeStyle" onselect="onSelectType" tooltipnlsid="MSG_TYPE">
                        <dmf:dataoptionlist>
                            <dmf:option datafield="type_name" labeldatafield="label_text"/>
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
<% com.documentum.fc.common.DfLogger.debug(MrcsImportContentUCF.class,"importcontentjsp - formatList",null,null); %>
                    <dmf:dropdownlist name="formatList" tooltipnlsid="MSG_TYPE_COLON" onselect="onSelectFormat" />
                    <dmf:label name='unknown_format_info_label' nlsid='MSG_ENFORCE_SELECT_FORMAT' cssclass='validatorMessageStyle' />
                </td>
            </tr>
            <% // %>
<% com.documentum.fc.common.DfLogger.debug(MrcsImportContentUCF.class,"importcontentjsp - obj",null,null); %>
            <!-- dmfx:docbaseobject name="obj" / -->
            <dmfx:docbaseobject name="docbaseObj"/>
<% com.documentum.fc.common.DfLogger.debug(MrcsImportContentUCF.class,"importcontentjsp - attrlist",null,null); %>
            <dmfx:docbaseattributelist name="attrlist" object="docbaseObj" attrconfigid="mrcs_import_attrlist_config" showcategorynames="false" pre="<tr><td align=\"right\"><b>" col1=":&nbsp;</b></td><td>"/>
            <!-- dmfx:docbaseattributelist name="attrlist" object="obj" attrconfigid="import" showcategorynames="false" pre="<tr><td align=\"right\"><b>" col1=":&nbsp;</b></td><td>"/ -->
            <!-- dmfx:docbaseattributelist name="attrlist" object="obj" attrconfigid="mrcs_import_attrlist_config" showcategorynames="false" pre="<tr><td align=\"right\"><b>" col1=":&nbsp;</b></td><td>"/ -->
        </table>
    </dmf:form>
</dmf:body>
</dmf:html>
