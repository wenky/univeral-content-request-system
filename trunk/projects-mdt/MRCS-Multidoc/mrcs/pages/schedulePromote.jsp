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

 Filename       $RCSfile: schedulePromote.jsp,v $
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
<dmf:label label="Scheduled Promotion" cssclass="defaultDocbaseAttributeLabelStyle"/>
<table border="0" cellpadding="2" cellspacing="0" width="50%">
<% // %>
<tr>
<td scope="row" align="left" nowrap>
<b><dmf:label label="Enter date and time for promotion" cssclass="defaultDocbaseAttributeLabelStyle"/>:&nbsp;</b>
</td>
<td><dmf:datetime name="promote_date"/></td>
</tr>
<tr>
<td scope="row" align="left" nowrap>
<table border="0" cellpadding="2" cellspacing="0" width="100">
<tr>
<td scope="row" align="left" nowrap>
<dmf:button name="Submit" onclick="startScheduledPromote" nlsid="MSG_BUTTON_OK" 
   cssclass='buttonLink' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_OK_TIP'/>
</td>
<td scope="row" align="left" nowrap>
<dmf:button name="Cancel" onclick="cancelScheduledPromote" nlsid="MSG_BUTTON_CANCEL" 
   cssclass='buttonLink' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>     
</td>
</tr>
</table>
</td>
</tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
