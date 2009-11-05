

<%@page import="com.medtronic.ecm.documentum.core.webtop.MdtLoginWithAppSelection"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ page import="com.documentum.web.formext.session.Login" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%
String strContextPath = request.getContextPath();
//
%>
<dmf:html>
<dmf:head>
<dmf:webcontainerrequirements/>
<dmf:webform/>
<dmf:title><dmf:label nlsid="MSG_TITLE"/></dmf:title>
<script type="text/javascript" src="<%=strContextPath%>/wdk/include/browserRequirements.js"></script>
<script type="text/javascript">
function onClickHelp()
{
fireClientEvent("InvokeHelp");
}
function clientSideTestHook()
{
}
</script>
</dmf:head>
<dmf:body id="modalSmall">
<dmf:form keepfresh="true">
<dmf:browserrequirements/>
<div id="mainPaneset">
<div><dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE'/></div>
<div id="scrollingcontent">
<div class="innerContent">
<div id="logo"><dmf:label nlsid="MSG_LOGO"/>&nbsp;<dmf:label cssclass="dialogTitleVersion" nlsid="MSG_VERSION"/></div>
<table align='center' cellspacing='0' cellpadding='0' border='0'>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30" nowrap><dmf:label nlsid='MSG_USERNAME'/></td>
<td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:text id='LoginUsername' name='<%=Login.CONTROL_USERNAME%>' size='40' defaultonenter='true'
tooltipnlsid='MSG_USERNAME'/></td>
</tr>
<tr>
<td></td>
<td></td>
<td><dmf:requiredfieldvalidator name='<%=Login.CONTROL_USERNAME_VALIDATOR%>' controltovalidate='<%=Login.CONTROL_USERNAME%>' nlsid='<%=Login.MSG_USERNAME_REQUIRED%>' indicator=""/></td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30" nowrap><dmf:label nlsid='MSG_PASSWORD'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:password id='LoginPassword' name='<%=Login.CONTROL_PASSWORD%>' size='40' defaultonenter='true' tooltipnlsid='MSG_PASSWORD'/></td>
</tr>
<tr>
<td></td>
<td></td>
<td><dmf:requiredfieldvalidator name='<%=Login.CONTROL_PASSWORD_VALIDATOR%>' controltovalidate='<%=Login.CONTROL_PASSWORD%>' nlsid='<%=Login.MSG_PASSWORD_REQUIRED%>' indicator=""/></td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30" nowrap><dmf:label nlsid='MSG_DOCBASE'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist name='<%=Login.CONTROL_DOCBASE%>' id='DocbaseName' tooltipnlsid='MSG_DOCBASE' onselect="onSelectDocbaseFromDropDown" visible="false">
<dmf:dataoptionlist>
<dmf:option datafield="docbase" labeldatafield="docbase"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
<dmf:label name='<%=MdtLoginWithAppSelection.DOCBASE_LABEL%>' id='MdtDocbaseLabel'/>
</td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30"><dmf:label nlsid='MSG_APPLICATION'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist width='214' name='<%=MdtLoginWithAppSelection.CONTROL_APPLICATION%>' id='ApplicationName' tooltipnlsid='MSG_APPLICATION' onselect="onSelectApplicationFromDropDown">
<dmf:dataoptionlist>
<dmf:option datafield="application_name" labeldatafield="label_text"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
<dmf:panel name='<%=Login.CONTROL_NETWORK_LOCATION_PANEL%>' visible='false'>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30" nowrap><dmf:label nlsid='MSG_NETWORK_LOCATION'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist name='<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN%>' id='NetworkLocation' tooltipnlsid='MSG_NETWORK_LOCATION' runatclient='true' onselect='clientSideTestHook'>
<dmf:option nlsid='MSG_SELECT_NETWORK_LOCATION' value='<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN_SELECT%>' />
<dmf:dataoptionlist>
<dmf:option datafield="<%=Login.COLUMN_NETWORK_LOCATION_ID%>" labeldatafield="<%=Login.COLUMN_NETWORK_LOCATION_LABEL%>"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
<dmf:label name='<%=Login.CONTROL_NETWORK_LOCATION_LABEL%>'/>
<dmf:hidden name='<%=Login.CONTROL_NETWORK_LOCATION_LABEL_HIDDEN%>'/>
</td>
</tr>
<tr>
<td></td>
<td></td>
<td>
<dmf:requiredfieldvalidator name="<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN_VALIDATOR%>" controltovalidate="<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN%>" nlsid="<%=Login.MSG_SELECT_NETWORK_LOCATION_REQUIRED%>" indicator="" />
</td>
</tr>
</dmf:panel>
<dmf:panel name='<%=Login.CONTROL_CREDENTIAL_PANEL%>'>
<tr>
<td height="5" colspan="3" class="spacer">&nbsp;</td>
</tr>
<tr>
<td scope="row" class="rightAlignment"></td><td></td>
<td>
<dmf:checkbox name='<%=Login.CONTROL_SAVE_CREDENTIAL%>' nlsid='MSG_SAVE_OPTION'/>
</td>
</tr>
</dmf:panel>
<tr>
<td class="spacer" height="30">&nbsp;</td>
<td></td><td></td>
</tr>
<tr>
<td class="fieldlabel leftAlignment" nowrap height="20" colspan="3"><dmf:link name='<%=Login.CONTROL_SHOWOPTIONS%>' onclick='onShowOptions'/></td>
</tr>
<dmf:panel name='<%=Login.CONTROL_OPTIONSPANEL%>'>
<tr>
<td class="spacer" height="30">&nbsp;</td>
<td></td><td></td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30" nowrap><dmf:label nlsid='MSG_DOMAIN'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:text name='<%=Login.CONTROL_DOMAIN%>' id='Domain' size='40' defaultonenter='true' tooltipnlsid='MSG_DOMAIN'/></td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30" nowrap><dmf:label nlsid='MSG_LANGUAGE'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist name='<%=Login.CONTROL_LANGUAGE%>' onselect="onChangeLanguage" tooltipnlsid='MSG_LANGUAGE'>
<dmf:dataoptionlist>
<dmf:option datafield="locale" labeldatafield="language"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
<dmf:panel name='<%=Login.CONTROL_SERVER_PANEL%>' visible='false'>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30" nowrap><dmf:label nlsid='MSG_SERVER'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist name='<%=Login.CONTROL_SERVER%>'>
<dmf:option nlsid='MSG_ANY_RUNNING_SERVER' value='<%=Login.ANY_RUNNING_SERVER%>'/>
<dmf:dataoptionlist>
<dmf:option datafield="servername" labeldatafield="serverlabel"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
</dmf:panel>
<tr>
<td height="30">&nbsp;</td>
<td>&nbsp;</td>
<td scope="row" class="leftAlignment"><dmf:checkbox name='<%=Login.CONTROL_ACCESS%>' nlsid='MSG_ACCESS' />
</tr>
</dmf:panel>
</table>
<dmf:panel name='<%=Login.CONTROL_ERRMSGPANEL%>'>
<div class='loginerrorspacing'>
<dmf:label name='<%=Login.CONTROL_ERRMSG%>' style='{COLOR: #FF0000}'/>
</div>
</dmf:panel>
</div>
</div>
<div id="buttonareaPane">
<div class="modalSmallButtonBar">
<div class="helpButtonSection">
<dmf:helpimage onclick='onClickHelp' nlsid='MSG_HELP' runatclient='true' src="icons/help_16.gif"/>
</div>
<div class="rightButtonSection" nowrap>
<dmf:button name='<%=Login.CONTROL_LOGINBUTTON%>' nlsid='MSG_LOGIN' onclick='onLogin' default='true' accessible='true' tooltipnlsid='MSG_LOGIN_TIP'/>
<dmf:button name='<%=Login.CONTROL_CHANGEPASSWORD%>' nlsid='<%=Login.MSG_CHANGE_PASSWORD%>' onclick='onChangePassword' accessible='true' tooltipnlsid='MSG_CHANGE_PASSWORD_TIP'/>
</div>
</div>
</div>
</dmf:form>
</dmf:body>
</dmf:html>
