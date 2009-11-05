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

 Filename       $RCSfile: AuditConfiguration.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:40 $

***********************************************************************
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>

<dmf:html>
<dmf:head>
    <dmf:webform validation='false' />
</dmf:head>
<dmf:body cssclass='contentBackground'>
    <!-- output report -->
    <% String auditreport = (String)session.getAttribute("mrcs.auditreport"); %>
    <%=auditreport%>
    <br><br>
    <dmf:form keepfresh='true'>
        <dmf:button name="Reload" onclick="reloadConfiguration" nlsid="MSG_BUTTON_RELOAD" 
                            cssclass='buttonLink' height='16' imagefolder='images/dialogbutton'/>
    </dmf:form>
</dmf:body>
</dmf:html>
