<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ page import="com.documentum.webtop.webcomponent.environment.preferences.general.AppGeneralPreferences,
com.documentum.webcomponent.environment.preferences.general.GeneralPreferences" %>
<html>
<head>
<dmf:webform/>
</head>
<body class='contentBackground'>
<dmf:form>
<table  cellspacing=0 cellpadding=0 border=0>
<tr>
<td colspan="3" class="spacer" height="10">&nbsp;</td>
</tr>
<tr>
<td class="fieldlabel" align="right" valign="top" scope="row" nowrap>
<dmf:label nlsid="MSG_APPLICATION_TYPE_HEADER"/></td>
<td class="defaultcolumnspacer" align="left" valign="top">&nbsp;</td>
<td align="left" valign="top">
<dmf:radio nlsid="MSG_APPLICATION_TYPE_CLASSIC" name='<%=AppGeneralPreferences.CONTROL_APPTYPE_CLASSIC%>' group='theme' onclick='onClassicSelected' tooltipnlsid="MSG_APPLICATION_TYPE_CLASSIC"/><br>
<dmf:radio nlsid="MSG_APPLICATION_TYPE_STREAMLINE"
name='<%=AppGeneralPreferences.CONTROL_APPTYPE_STREAMLINE%>' group='theme' onclick='onStreamlineSelected'
tooltipnlsid="MSG_APPLICATION_TYPE_STREAMLINE"/><br>
<div class="note"><dmf:label nlsid="MSG_APPLICATION_TYPE_DESCRIPTION"/></div>
</td>
</tr>
<tr>
<td colspan="3" class="spacer" height="20">&nbsp;</td>
</tr>
<tr>
<td class="fieldlabel" align="right" valign="top" scope="row">
<dmf:label nlsid="MSG_START_SECTION_HEADER"/></td>
<td class="defaultcolumnspacer" align="left" valign="top">&nbsp;</td>
<td align="left" valign="top">
<dmf:datadropdownlist name="<%=AppGeneralPreferences.CONTROL_SECTIONS%>"  tooltipnlsid="MSG_START_SECTION_HEADER">
<dmf:dataoptionlist>
<dmf:option datafield="id" labeldatafield="label"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>   <br>
<div class="note"><dmf:label nlsid="MSG_START_SECTION_DESCRIPTION"/></div>
</td>
</tr>
<tr>
<td colspan="3" class="spacer" height="20">&nbsp;</td>
</tr>
<tr>
<td class="fieldlabel" align="right" valign="top" scope="row">
<dmf:label cssclass='fieldlabel' nlsid="MSG_CHOOSE_THEME_HEADER"/></td>
<td class="defaultcolumnspacer" align="left" valign="top">&nbsp;</td>
<td align="left" valign="top">
<dmf:datadropdownlist name="<%=AppGeneralPreferences.CONTROL_RESOURCE_FOLDERS%>" tooltipnlsid="MSG_CHOOSE_THEME_HEADER">
<dmf:dataoptionlist>
<dmf:option datafield="value" labeldatafield="value"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
<br>
<div class="note"><dmf:label nlsid="MSG_CHOOSE_THEME_DESCRIPTION"/></div>
</td>
</tr>
<tr>
<td colspan="3" class="spacer" height="20">&nbsp;</td>
</tr>
<!--
<-- mtw: Commented out because Accessibility features may conflict with MRCS customizations and
<-- change user experience
<tr>
<td class="fieldlabel" align="right" valign="top" scope="row">
<dmf:label cssclass="fieldlabel" nlsid="MSG_ACCESSIBILITY_OPTION" visible='false'/>
</td>
<td class="defaultcolumnspacer" align="left" valign="top">&nbsp;</td>
<td align="left" valign="top">
<dmf:checkbox name='<%=AppGeneralPreferences.CONTROL_ACCESSIBILITY_OPTION%>'
onclick='onAccessibilitySelected' tooltipnlsid="MSG_ACCESSIBILITY_OPTION" visible='false'/>
<dmf:label nlsid="MSG_ACCESSIBILITY_OPTION_DESCRIPTION" visible='false'/> <br>
<div class="note"><dmf:label nlsid="MSG_ACCESSIBILITY_OPTION_NOTE" visible='false'/></div>
</td>
</tr>
-->
<dmf:panel name="changeCheckoutPathPanel">
<tr>
<td colspan="3" class="spacer" height="20">&nbsp;</td>
</tr>
<tr>
<td class="fieldlabel" align="right" valign="top" scope="row">
<dmf:label cssclass='fieldlabel' nlsid="MSG_CHECKOUT_CHECKEDOUT_LOCATION"/>
</td>
<td class="defaultcolumnspacer" align="left" valign="top">&nbsp;</td>
<td align="left" valign="top">
<dmf:link onclick="onChangeUcfCheckoutLocation" nlsid="MSG_CHANGE_CHECKOUT_CHECKEDOUT_LOCATION"/>
&nbsp;<dmf:label name="checkoutPathLabel" />
</td>
</tr>
</dmf:panel>
<!--
<-- mtw: Commented out because Drag & Drop behavior may conflict with MRCS customizations 
<dmf:panel name="enableDragDropPluginPanel">
<tr>
<td colspan="3" class="spacer" height="20">&nbsp;</td>
</tr>
<tr> 
<td class="fieldlabel" align="right" valign="top" scope="row">
<dmf:label nlsid="MSG_DRAG_DROP_PLUGIN_OPTION"/>
</td>
<td class="defaultcolumnspacer" align="left" valign="top">&nbsp;</td>
<td align="left" valign="top">
<dmf:checkbox name='<%=AppGeneralPreferences.CONTROL_DRAG_DROP_PLUGIN_OPTION%>'
tooltipnlsid="MSG_DRAG_DROP_PLUGIN_OPTION"/><dmf:label nlsid="MSG_DRAG_DROP_PLUGIN_OPTION_DESCRIPTION"/><br>
<div class="note"><dmf:label nlsid="MSG_DRAG_DROP_NOTE"/></div>
</td>
</tr>
-->
</dmf:panel>
<dmf:panel name='<%=GeneralPreferences.CONTROL_NETWORK_LOCATION_PANEL%>' visible='false'>
<tr>
<td colspan="3" class="spacer" height="20">&nbsp;</td>
</tr>
<tr>
<td class="fieldlabel" align="right" valign="top" nowrap  scope="row">
<dmf:label cssclass="fieldlabel" nlsid='MSG_NETWORK_LOCATION'/>
</td>
<td class="defaultcolumnspacer">&nbsp;</td>
<td valign="top">
<dmf:datadropdownlist name='<%=AppGeneralPreferences.CONTROL_NETWORK_LOCATION_DROPDOWN%>' id='NetworkLocation' tooltipnlsid='MSG_NETWORK_LOCATION'>
<dmf:option nlsid='MSG_SELECT_NETWORK_LOCATION' value='<%=AppGeneralPreferences.CONTROL_NETWORK_LOCATION_DROPDOWN_SELECT%>' />
<dmf:dataoptionlist>
<dmf:option datafield="<%=AppGeneralPreferences.COLUMN_NETWORK_LOCATION_ID%>" labeldatafield="<%=AppGeneralPreferences.COLUMN_NETWORK_LOCATION_LABEL%>"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
<div class="note"><dmf:label nlsid="MSG_NETWORK_LOCATION_NOTE"/></div>
</td>
</tr>
</dmf:panel>
<dmf:panel name="displayHiddenObjectPanel">
<tr>
<td colspan="3" class="spacer" height="20">&nbsp;</td>
</tr>
<tr>
<td class="fieldlabel" align="right" valign="top" scope="row">
<dmf:label nlsid='MSG_HIDDEN_OBJECT_OPTION'/>
</td>
<td class="defaultcolumnspacer" align="left" valign="top">&nbsp;</td>
<td align="left" valign="top">
<dmf:checkbox name='<%=AppGeneralPreferences.CONTROL_DISPLAY_HIDDEN_OBJECT_OPTION%>'
tooltipnlsid='MSG_SHOW_HIDDEN_OBJECT_OPTION'/><dmf:label nlsid='MSG_SHOW_HIDDEN_OBJECT_OPTION'/>
</td>
</tr>
</dmf:panel>
</table>
<br>
</dmf:form>
</body>
</html>
