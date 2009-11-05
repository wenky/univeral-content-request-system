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
</dmf:head>
<dmf:body id="modalSmall" bottommargin='0'
leftmargin='0' rightmargin='0' >
<dmf:form>
<div id="mainPaneset">
<div><dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE'/></div>
<div id="scrollingcontent">
<div class="innerContent">
<div id="logo"><dmf:label nlsid="MSG_LOGO"/>&nbsp;<dmf:label cssclass="dialogTitleVersion" nlsid="MSG_VERSION"/></div>
<table border=0 cellspacing=2 cellpadding=0>
<tr>
<td scope="row" class="leftAlignment"><dmf:label nlsid='MSG_BUILD'/><span class="defaultcolumnspacer">: </span>
<dmf:label name='<%=About.CONTROL_BUILD%>'/></td>
</tr>
<tr>
<td scope="row" class="leftAlignment"><dmf:label nlsid='MSG_DFCVERSION'/><span class="defaultcolumnspacer">: </span>
<dmf:label name='<%=About.CONTROL_DFCVERSION%>'/></td>
</tr>
<!-- Added to distinguish QADOC builds -->
<tr>
<td scope="row" class="leftAlignment"><dmf:label nlsid='MSG_QAD_LABEL'/><span class="defaultcolumnspacer">: </span>
@release_number@.@build_number@ (@tag_number@)</td>
</tr>
</table>
<dmf:panel name='<%=About.CONTROL_HIDDENTOOLS%>'>
<div>
<dmf:button name='<%=About.CONTROL_DQLEDITOR%>' nlsid='MSG_DQLEDITOR' onclick='onDQLEditor' runatclient='true' tooltipnlsid='MSG_DQLEDITOR_TIP'/>
<dmf:button name='<%=About.CONTROL_IAPIEDITOR%>' nlsid='MSG_IAPIEDITOR' onclick='onIAPIEditor' runatclient='true' tooltipnlsid='MSG_IAPIEDITOR_TIP'/>
</div>
</dmf:panel>
<div style="margin-top:10px;text-align:center;border-top: 1px solid #bbb;padding-top: 20px;width:100%;">
<a href="http://www.emc.com" target='_top'><dmf:image src="images/about/about_documentumlogo.gif" border="0"/></a>
<p>&copy; 1994-2007 EMC Corporation. All rights reserved.</p>
</div>
</div>
</div>
<div id="buttonareaPane">
<div class="modalSmallButtonBar">
<div class="rightButtonSection">
<dmf:button name='<%=About.CONTROL_CLOSE%>' nlsid='MSG_OK' onclick='onClose' default='true' tooltipnlsid='MSG_CLOSE_TIP'/>
</div>
</div>
</div>
</dmf:form>
</dmf:body>
</dmf:html>
