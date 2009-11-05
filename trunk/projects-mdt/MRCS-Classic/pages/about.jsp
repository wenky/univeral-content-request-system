<%--
***********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.2
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: about.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/09/08 15:18:02 $

***********************************************************************
--%>
<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ page import="com.documentum.webtop.webcomponent.about.About" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<dmf:title><dmf:label nlsid="MSG_TITLE"/></dmf:title>
<script>
function onDQLEditor()
{
postComponentNestEvent(null, "dql", "dql");
}
function onIAPIEditor()
{
postComponentNestEvent(null, "api", "api");
}
</script>
<style type="text/css">
.centered {
position: absolute;
top: 0;
right: 0;
bottom: 0;
left: 0;
width: 100%;
height: 100%;
margin: auto;
}
</style>
</dmf:head>
<dmf:body cssclass='contentBackground' topmargin='40' bottommargin='0'
leftmargin='0' rightmargin='0' marginheight='40' marginwidth='0'>
<dmf:form>
<div style="position: absolute;top: 40;right: 0;bottom: 0;left: 0;width: 100%;height: 100%;margin: auto;">
<table align="center" width="513" border="0" cellpadding="0" cellspacing="0">
<tr>
<td rowspan="14" class="aboutbgleft" valign="bottom"><dmf:image src="images/about/about_bg_left_bot.gif" width="8" border="0" /></td>
<td colspan="11" bgcolor="#333333" height="1" class="spacer">&nbsp;</td>
<td rowspan="14" class="aboutbgright" width="7" valign="bottom"><dmf:image src="images/about/about_bg_right_bot.gif" width="7" border="0"/></td>
</tr>
<tr>
<td colspan="5" bgcolor="#000000">&nbsp;</td>
<td bgcolor="#666666" class="spacer" width="1">&nbsp;</td>
<td colspan="3" bgcolor="#000000">&nbsp;</td>
<td bgcolor="#666666" class="spacer" width="1">&nbsp;</td>
<td><a href="http://www.emc.com" target='_top'><dmf:image src="images/about/about_emclogo.gif" width="123" height="37" border="0"/></a></td>
</tr>
<tr>
<td colspan="11"><dmf:image src="images/about/about_colorpanel.gif" width="498" height="95" border="0"/></td>
</tr>
<tr>
<td colspan="11" bgcolor="#000000" class="spacer" height="10">&nbsp;</td>
</tr>
<tr>
<td colspan="11" height="33">&nbsp;</td>
</tr>
<tr>
<td colspan="2">&nbsp;</td>
<td colspan="5" height="32" valign="middle">
<dmf:label cssclass='aboutTitle' nlsid='MSG_TITLE'/>
</td>
<td colspan="4">&nbsp;</td>
</tr>
<tr>
<td colspan="11" height="18" class="spacer">&nbsp;</td>
</tr>
<tr>
<td colspan="8" height="1" class="spacer" bgcolor="#666666" align="left"><dmf:image src="images/about/about_stripe_left.gif" width="213" height="1" border="0"/></td>
<td colspan="3" align="right"><dmf:image src="images/about/about_stripe_right.gif" width="178" height="1" border="0"/></td>
</tr>
<tr>
<td colspan="11" height="21">&nbsp;</td>
</tr>
<tr>
<td colspan="3" height="29">&nbsp;</td>
<td colspan="8">
<table border=0 cellspacing=2 cellpadding=0>
<tr>
<td scope="row" align="left"><dmf:label nlsid='MSG_BUILD'/><span class="defaultcolumnspacer">: </span>
<dmf:label name='<%=About.CONTROL_BUILD%>'/></td>
</tr>
<tr>
<td scope="row" align="left"><dmf:label nlsid='MSG_DFCVERSION'/><span class="defaultcolumnspacer">: </span>
<dmf:label name='<%=About.CONTROL_DFCVERSION%>'/></td>
</tr>
<tr>
<td scope="row"><dmf:label nlsid='MSG_MRCS_LABEL'/> : </td>
<td><dmf:label nlsid='MSG_MRCS_VERS'/> (<dmf:label nlsid='MSG_MRCS_DESC'/>)</td>
</tr>
<tr>
<td scope="row"><dmf:label nlsid='MSG_MRCS_BUILD_LABEL'/> : </td>
<td><dmf:label nlsid='MSG_MRCS_TAG'/></td>
</tr>
</table>
</td>
</tr>
<tr>
<td colspan="11">&nbsp;</td>
</tr>
<tr>
<td colspan="4">
<a href='http://www.documentum.com' target='_top'><dmf:image src="images/about/about_documentumlogo.gif" width="124" height="21" border="0"/></a>
</td>
<td colspan="7" align="right" valign="middle">
<table cellspacing=0 cellpadding=4 border=0>
<tr valign=middle>
<td valign="middle" align="right">
<dmf:panel name='<%=About.CONTROL_HIDDENTOOLS%>'>
<dmf:button name='<%=About.CONTROL_DQLEDITOR%>' height='16' cssclass='buttonLink' nlsid='MSG_DQLEDITOR' onclick='onDQLEditor' runatclient='true' imagefolder='images/button' tooltipnlsid='MSG_DQLEDITOR_TIP'/>
</td>
<td valign="middle" align="right" class="buttonbuffer">
<dmf:button name='<%=About.CONTROL_IAPIEDITOR%>' height='16' cssclass='buttonLink' nlsid='MSG_IAPIEDITOR' onclick='onIAPIEditor' runatclient='true' imagefolder='images/button' tooltipnlsid='MSG_IAPIEDITOR_TIP'/>
</dmf:panel>
</td>
<td width="100%" align="right" class="buttonbuffer" style="padding-right:20px;">
<dmf:button name='<%=About.CONTROL_CLOSE%>' cssclass='buttonLink' nlsid='MSG_CLOSE' onclick='onClose' default='true'
height='16' imagefolder='images/button' tooltipnlsid='MSG_CLOSE_TIP'/>
</td>
</tr>
</table>
</td>
</tr>
<tr>
<td width="19" height="17" class="spacer">&nbsp;</td>
<td colspan="10" width="479" height="17" class="spacer">&nbsp;</td>
</tr>
<tr>
<td colspan="11" class="aboutbgbot" height="11">&nbsp;</td>
</tr>
<tr>
<td class="spacer" width="8" height="1">&nbsp;</td>
<td class="spacer" width="19" height="1">&nbsp;</td>
<td class="spacer" width="14" height="1">&nbsp;</td>
<td class="spacer" width="85" height="1">&nbsp;</td>
<td class="spacer" width="6" height="1">&nbsp;</td>
<td class="spacer" width="12" height="1">&nbsp;</td>
<td class="spacer" width="1" height="1">&nbsp;</td>
<td class="spacer" width="73" height="1">&nbsp;</td>
<td class="spacer" width="110" height="1">&nbsp;</td>
<td class="spacer" width="54" height="1">&nbsp;</td>
<td class="spacer" width="1" height="1">&nbsp;</td>
<td class="spacer" width="123" height="1">&nbsp;</td>
<td class="spacer" width="7" height="1">&nbsp;</td>
</tr>
</table>
</div>
</dmf:form>
</dmf:body>
</dmf:html>
