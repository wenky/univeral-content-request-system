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

 Filename       $RCSfile: MrcsStreamlineview.jsp,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/06/30 22:58:08 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.web.form.control.databound.Datagrid" %>
<%@ page import="com.documentum.webcomponent.navigation.drilldown.DrillDown" %>
<html>
<head>
<dmf:webform/>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/popupMenu.js")%>'></script>
</head>
<body class='contentBackground' marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>
<dmf:form>
<%@ include file='/mrcs/pages/MrcsDrilldownBody.jsp' %>
<!--  %@ include file='/webcomponent/navigation/drilldown/drilldown_body.jsp' % -->
</dmf:form>
</body>
</html>
