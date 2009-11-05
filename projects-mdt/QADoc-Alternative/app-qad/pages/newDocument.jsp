<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<dmf:webform validation='false'/>
</dmf:head>
<dmf:body cssclass='contentBackground'>
<dmf:form keepfresh='true'>
<table border="0" cellpadding="0" cellspacing="0">
<tr height="10" class="spacer">
<dmfx:clientenvpanel environment='appintg' reversevisible='true'>
<td colspan="3">&nbsp;</td>
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment='appintg'>
<td width="25%">&nbsp;</td>
<td colspan="2">&nbsp;</td>
</dmfx:clientenvpanel>
</tr>
<% // %>
<tr>
<td nowrap class="rightAlignment" valign="middle">
<dmf:label cssclass="shortfieldlabel" nlsid="MSG_NAME_COLON"/>
</td>
<td class="defaultcolumnspacer">&nbsp;</td>
<td nowrap class="leftAlignment">
<dmf:text name="attribute_object_name" size="30" id="object_name" tooltipnlsid="MSG_NAME_COLON_TIP" autocompleteid="DBAttr_object_name" value="Auto Generated Name"/>
<dmfx:clientenvpanel environment='appintg' reversevisible='true'>
&nbsp;<dmf:requiredfieldvalidator name="validator" controltovalidate="attribute_object_name" nlsid="MSG_MUST_HAVE_NAME"/>
<dmf:utf8stringlengthvalidator name="validator" controltovalidate="attribute_object_name" maxbytelength="255" nlsid="MSG_NAME_TOO_LONG"/>
<dmf:label name="labelnormalcharsneeded" nlsid="MSG_NORMAL_CHARS_NEEDED" cssclass="validatorMessageStyle"/>
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment='appintg'>
<br><dmf:requiredfieldvalidator name="validator" controltovalidate="attribute_object_name" nlsid="MSG_MUST_HAVE_NAME" indicator=''/>
<dmf:utf8stringlengthvalidator name="validator" controltovalidate="attribute_object_name" maxbytelength="255" nlsid="MSG_NAME_TOO_LONG"/>
<dmf:label name="labelnormalcharsneeded" nlsid="MSG_NORMAL_CHARS_NEEDED" cssclass="validatorMessageStyle"/>
</dmfx:clientenvpanel>
<dmfx:docbaseattributeproxy name="docbaseobjectnameproxy" object="docbaseObj" controltorepresent="attribute_object_name" attribute="object_name"/>
</td>
</tr>
<% // %>
<tr>
<td  nowrap class="rightAlignment" valign="middle" scope="row">
<dmf:label cssclass="shortfieldlabel" nlsid="MSG_TYPE_COLON"/>
</td>
<td class="defaultcolumnspacer">&nbsp;</td>
<td class="fieldlabel leftAlignment">
<dmf:datadropdownlist name="objectTypeList" onselect="onSelectType" tooltipnlsid="MSG_TYPE_COLON_TIP">
<dmf:dataoptionlist>
<dmf:option datafield="type_name" labeldatafield="label_text"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
<% // %>
<tr>
<td  nowrap class="rightAlignment" valign="middle" scope="row">
<dmf:label cssclass="shortfieldlabel" nlsid="MSG_FORMAT_COLON"/>
</td>
<td class="defaultcolumnspacer">&nbsp;</td>
<td class="fieldlabel leftAlignment">
<dmf:datadropdownlist name="formatList" onselect="onSelectFormat" tooltipnlsid="MSG_FORMAT_COLON_TIP">
<dmf:dataoptionlist>
<dmf:option datafield="name" labeldatafield="description"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
<dmfx:docbaseattributeproxy name="docbaseobjectformatproxy" object="docbaseObj" controltorepresent="formatList" attribute="a_content_type"/>
</td>
</tr>
<% // %>
<dmf:panel name="template_panel" >
<tr>
<td  nowrap class="rightAlignment" valign="middle" scope="row">
<dmf:label cssclass="shortfieldlabel" nlsid="MSG_TEMPLATE_COLON"/>
</td>
<td class="defaultcolumnspacer">&nbsp;</td>
<td class="fieldlabel leftAlignment">
<dmf:datadropdownlist name="templateList" tooltipnlsid="MSG_TEMPLATE_COLON_TIP">
<dmf:dataoptionlist>
<dmf:option datafield="r_object_id" labeldatafield="object_name"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
</dmf:panel>
<% // %>
<dmf:panel name="location_panel">
<td scope="row" nowrap class="rightAlignment" valign="middle">
<dmf:label cssclass="shortfieldlabel" nlsid="MSG_LOCATION_TO_SAVE"/><span class="shortfieldlabel">:</span>
</td>
<td class="defaultcolumnspacer">&nbsp;</td>
<td class="fieldlabel leftAlignment">
<dmf:text name="locationToSave"  size="60" enabled="false" tooltipnlsid="MSG_LOCATION_TO_SAVE" autocompleteid="DBLocToSave"/>
<dmf:button nlsid="MSG_BROWSE" onclick="onBrowse" tooltipnlsid='MSG_BROWSE' />
</td>
</dmf:panel>
<tr height="10" class="spacer">
<td colspan="3">&nbsp;</td>
</tr>
<dmfx:clientenvpanel environment='appintg' reversevisible='true'>
<% // %>
<tr>
<td nowrap class="fieldlabel rightAlignment">
<dmf:link name="showhideoptions" nlsid="MSG_SHOW_OPTIONS" onclick="onClickShowHideOptions"/>
</td>
<td></td>
<td></td>
</tr>
<dmf:panel name="optionspanel">
<tr>
<td nowrap colspan="3" class="leftAlignment">
<dmf:checkbox name="subscribe" nlsid="MSG_SUBSCRIBE_TO_FILE"/>
</td>
</tr>
<tr>
<td  nowrap colspan="3" class="leftAlignment">
<dmf:checkbox name="makevirtual" nlsid="MSG_MAKE_VIRTUAL"/>
</td>
</tr>
</dmf:panel>
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment='appintg'>
<tr>
<td></td>
<td nowrap colspan="2" class="leftAlignment">
&nbsp;&nbsp;&nbsp;<dmf:checkbox name="subscribe" nlsid="MSG_SUBSCRIBE_TO_FILE"/>
</td>
</tr>
</dmfx:clientenvpanel>
<dmfx:docbaseobject name="docbaseObj"/>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
