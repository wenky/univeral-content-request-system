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

 Filename       $RCSfile: MrcsNewDocumentCustomComponent.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/01/12 22:02:40 $

***********************************************************************
--%>


<%@ page language="java" import="java.util.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>


<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<dmf:html>
  <dmf:head>
    <%// enable webtop/wdk form processing logic %>
    <dmf:webform/>    
    
    <base href="<%=basePath%>">        
    <title><dmf:label nlsid='USERSPECIFIED_PAGE_TITLE'/></title>

    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="This is my page">

    <!--
    <link rel="stylesheet" type="text/css" href="styles.css">
    -->
    
  </dmf:head>
  
  <dmf:body>
    <dmf:label nlsid='USERSPECIFIED_TITLE'/><br>
    <dmf:form>
        <dmf:label nlsid='USERSPECIFIED_DOC_NAME_LABEL'/><dmf:text name="MrcsCustomObjectName"/><br>
        <BR>
        <table border="0" cellpadding="2" cellspacing="0" width="100">
            <tr>
                <td>
			        <dmf:button name="CustomSubmit" onclick="submitCustomData" nlsid="MSG_BUTTON_NEXT" 
			                    cssclass='buttonLink' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_NEXT_TIP'/>     
                </td>
                <td>
			        <dmf:button name="CustomCancel" onclick="cancelNewDocument" nlsid="MSG_BUTTON_CANCEL" 
			                    cssclass='buttonLink' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>     
                </td>
           </tr>
       </table>      
    </dmf:form>
    
  </dmf:body>
</dmf:html>
