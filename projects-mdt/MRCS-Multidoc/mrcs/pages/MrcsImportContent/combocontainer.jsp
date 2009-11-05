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

 Filename       $RCSfile: combocontainer.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:43 $

***********************************************************************
--%>

<%
        //
    %>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<html>
<head>
<dmf:webform />
<title><dmf:label nlsid='MSG_TITLE' /></title>
</head>
<body class='contentBorder' marginheight='0' marginwidth='12'
    topmargin='0' bottommargin='0' leftmargin='12' rightmargin='0'>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
    <tr>
        <td valign='top'><dmf:form>
            <table width='100%' cellspacing='0' cellpadding='0'
                border='0'>
                <tr height='5'>
                    <td valign='top'></td>
                </tr>
                <tr height='1'>
                    <td valign='top'><dmf:label cssclass='dialogTitle'
                        nlsid='MSG_TITLE' />:&nbsp; <dmf:label
                        cssclass='dialogFileName' nlsid='MSG_OBJECT' />
                    </td>
                </tr>
                <tr height='5'>
                    <td valign='top'></td>
                </tr>
                <tr class='contentBackground'>
                    <td align='left' valign='top'><dmfx:containerinclude />
                    <br>
                    </td>
                </tr>
                <tr>
                    <td><br>
                    <table border='0' cellpadding='0' cellspacing='0'>
                        <tr>
                            <!--td><dmf:button name='prev'
                                style='color:#000000'
                                cssclass="buttonLink" nlsid='MSG_PREV'
                                onclick='onPrev' height='16'
                                imagefolder='images/dialogbutton'
                                tooltipnlsid='MSG_PREV_TIP' /></td>
                            <td width=5></td-->
                            <td>
	                            <dmf:button name='next'
	                                cssclass='buttonLink' nlsid='MSG_NEXT'
	                                onclick='onNextPage' height='16'
	                                imagefolder='images/dialogbutton'
	                                tooltipnlsid='MSG_NEXT_TIP' />                                    
                            </td>
                            <td width=5></td>
                            <!--td><dmf:button name='ok'
                                cssclass="buttonLink" nlsid='MSG_OK'
                                onclick='onOk' height='16'
                                imagefolder='images/dialogbutton'
                                tooltipnlsid='MSG_OK_TIP' /></td>
                            <td width=5></td-->
                            <td><dmf:button name='cancel'
                                cssclass='buttonLink' nlsid='MSG_CANCEL'
                                onclick='onCancel' height='16'
                                imagefolder='images/dialogbutton'
                                tooltipnlsid='MSG_CANCEL_TIP' /></td>
                            <td width=5></td>
                            <!--td><dmf:button name='close'
                                cssclass="buttonLink" nlsid='MSG_CLOSE'
                                onclick='onClose' height='16'
                                imagefolder='images/dialogbutton'
                                tooltipnlsid='MSG_CLOSE_TIP' /></td>
                            <td width=5></td-->
                            <td><dmf:button name='help'
                                cssclass="buttonLink" nlsid='MSG_HELP'
                                onclick='onClickHelp' runatclient='true'
                                height='16'
                                imagefolder='images/dialogbutton'
                                tooltipnlsid='MSG_HELP_TIP' /></td>
                        </tr>
                    </table>
                    </td>
                </tr>
                <tr height='4'>
                    <td valign='bottom'></td>
                </tr>
            </table>
        </dmf:form></td>
    </tr>
</table>
</body>
</html>
