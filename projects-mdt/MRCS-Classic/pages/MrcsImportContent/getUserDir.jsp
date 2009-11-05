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

 Filename       $RCSfile: getUserDir.jsp,v $
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
<%@ taglib uri="/WEB-INF/tlds/dmcontentxfer_1_0.tld" prefix="dmxfer" %>
<%@ page import="com.documentum.webcomponent.library.contentxfer.ContentTransferContainer" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
</dmf:head>
<dmf:body cssclass='contentBackground' marginheight='0' marginwidth='12'
topmargin='0' bottommargin='0' leftmargin='12' rightmargin='0'>
<dmf:form>
<br><br>
<table align="center">
<tr><td align="center"><dmf:label nlsid="MSG_GETUSERDIR_FEEDBACK"/></td></tr>
<tr>
<td>
<dmxfer:utilapplet name="<%=ContentTransferContainer.UTILAPPLET_CONTROL_NAME%>"
successhandler="onGetUserDir" successhandlerarg="userdir" encoderesult="true"/>
</td>
</tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
