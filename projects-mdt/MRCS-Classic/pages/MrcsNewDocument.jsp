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

 Filename       $RCSfile: MrcsNewDocument.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/01/12 22:02:39 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<dmf:html>
<dmf:head>
    <dmf:webform validation='false' />
</dmf:head>
<dmf:body cssclass='contentBackground'>
    <dmf:form keepfresh='true'>
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
            <tr>
                <td></td>
            </tr>
            <% // doctype %>
            <tr>
                <td width="10%" align="right" scope="row">
                    <b><dmf:label nlsid="MSG_TYPE_COLON" />&nbsp;</b>
                </td>
                <td width="90%" align="left">
                    <dmf:dropdownlist name="doctypes" onselect="onSelectType" tooltipnlsid="MSG_TYPE_COLON"/>
                </td>
            </tr>
            <% // formats %>
            <tr>
                <td width="10%" align="right" scope="row">
                    <b><dmf:label nlsid="MSG_FORMAT_COLON" />&nbsp;</b>
                </td>
                <td width="90%" align="left">
                    <dmf:dropdownlist name="formats" onselect="onSelectFormat" tooltipnlsid="MSG_TYPE_COLON"/>
                </td>
            </tr>
            <% // templates %>
            <dmf:panel name="template_panel">
                <tr>
                    <td width="10%" align="right" scope="row"><b><dmf:label
                        nlsid="MSG_TEMPLATE_COLON" />&nbsp;</b></td>
                    <td width="90%" align="left"><dmf:datadropdownlist
                        name="templateList"
                        tooltipnlsid="MSG_TEMPLATE_COLON">
                        <dmf:dataoptionlist>
                            <dmf:option datafield="r_object_id" labeldatafield="object_name" />
                        </dmf:dataoptionlist>
                    </dmf:datadropdownlist></td>
                </tr>
            </dmf:panel>
            <dmf:label name='template_not_found' nlsid='MSG_NO_TEMPLATES' cssclass='validatorMessageStyle' />
            <tr>
                <td></td>
                <td>
			        <table border="0" cellpadding="2" cellspacing="0" width="100">
			            <tr>
			                <td>
			                    <dmf:button name="Submit" onclick="onGotoNextComponent" nlsid="MSG_BUTTON_NEXT" 
			                                cssclass='buttonLink' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_NEXT_TIP'/>     
			                </td>
			                <td>
			                    <dmf:button name="Cancel" onclick="cancelNewDocument" nlsid="MSG_BUTTON_CANCEL" 
			                                cssclass='buttonLink' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>     
			                </td>
			           </tr>
			       </table>      
                </td>
            </tr>
            <tr height="15">
                <td></td>
                <td></td>
            </tr>
            <tr>
                <table border="0" cellpadding="2" cellspacing="0" width="100%">
                    <% // %>
                    <tr>
                        <td></td>
                        <td colspan="2" nowrap scope="row"><dmf:link
                            name="showhideoptions"
                            nlsid="MSG_SHOW_OPTIONS"
                            onclick="onClickShowHideOptions" /></td>
                    </tr>
                    <dmf:panel name="optionspanel">
                        <tr>
                            <td></td>
                            <td scope="row"><dmf:checkbox
                                name="subscribe"
                                nlsid="MSG_SUBSCRIBE_TO_FILE" /></td>
                        </tr>
                    </dmf:panel>
                </table>
            </tr>
        </table>
    </dmf:form>
</dmf:body>
</dmf:html>
