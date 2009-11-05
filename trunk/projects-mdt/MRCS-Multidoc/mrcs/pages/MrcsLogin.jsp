<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ page import="com.documentum.web.formext.session.Login,
com.documentum.web.env.EnvironmentService,
com.documentum.web.env.PortalEnvironment" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%
String strContextPath = request.getContextPath();
//
%>
<dmf:html>
<dmf:head>
<dmf:webform/>
<dmf:title><dmf:label nlsid="MSG_TITLE"/></dmf:title>
<script language="JavaScript" src="<%=strContextPath%>/wdk/include/browserRequirements.js"></script>
<script>
function onClickHelp()
{
fireClientEvent("InvokeHelp");
}
</script>
</dmf:head>
<dmf:body cssclass='contentBackground' topmargin='40' bottommargin='0'
leftmargin='0' rightmargin='0' marginheight='40' marginwidth='0'>
<dmf:form keepfresh="true">
<dmf:browserrequirements/>
<dmf:panel name='<%=Login.CONTROL_CENTEREDPANEL1%>'>
<div style="position:absolute;top:40;right:0;bottom:0;left:0;width:100%;height:100%;margin:auto;">
</dmf:panel>
<table width="400" height="65" border="0" cellpadding="0" cellspacing="0" align="center">
<tr>
<td width="11" height="1" nowrap class="spacer">&nbsp;</td>
<td width="10" nowrap class="spacer">&nbsp;</td>
<td width="9" nowrap class="spacer">&nbsp;</td>
<td class="spacer" nowrap width="99%">&nbsp;</td>
<td width="9" nowrap class="spacer">&nbsp;</td>
<td width="10" nowrap class="spacer">&nbsp;</td>
<td width="11" nowrap class="spacer">&nbsp;</td>
</tr>
<tr>
<td colspan="3" width="30" rowspan="3" class="logintopleftedge" valign="top" align="right"><dmf:image src="images/login/login_top_corner_left.gif" width="30" height="23" border="0"/></td>
<td nowrap height="3" class="logintopbg">&nbsp;</td>
<td colspan="3" rowspan="3" class="logintoprightedge" valign="top" align="left"><dmf:image src="images/login/login_top_corner_right.gif" width="30" height="23" border="0"/></td>
</tr>
<tr>
<td nowrap class="logintitletop" height="7">&nbsp;</td>
</tr>
<tr>
<td nowrap class="logintitletext" height="23" valign="top"><dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE'/></td>
</tr>
<tr>
<td nowrap class="loginleftedge">&nbsp;</td>
<td nowrap colspan="2" class="documentumBgColor">&nbsp;</td>
<td nowrap class="documentumBgColor">&nbsp;</td>
<td nowrap colspan="2" class="documentumBgColor">&nbsp;</td>
<td nowrap class="loginrightedge">&nbsp;</td>
</tr>
</table>
<table width="400" border="0" cellpadding="0" cellspacing="0" align="center">
<tr>
<td width="11" nowrap class="loginleftedge">&nbsp;</td>
<td width="19" nowrap class="contentBackground">&nbsp;</td>
<td width="98%" class="contentBackground" valign="top"><br>
<table align='center' cellspacing='0' cellpadding='0' border='0'>
<tr>
<td scope="row" align='right' class="fieldlabel" height="30"><dmf:label nlsid='MSG_USERNAME'/></td>
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
<td scope="row" align='right' class="fieldlabel" height="30"><dmf:label nlsid='MSG_PASSWORD'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:password id='LoginPassword' name='<%=Login.CONTROL_PASSWORD%>' size='40' defaultonenter='true' tooltipnlsid='MSG_PASSWORD'/></td>
</tr>
<tr>
<td></td>
<td></td>
<td><dmf:requiredfieldvalidator name='<%=Login.CONTROL_PASSWORD_VALIDATOR%>' controltovalidate='<%=Login.CONTROL_PASSWORD%>' nlsid='<%=Login.MSG_PASSWORD_REQUIRED%>' indicator=""/></td>
</tr>
<tr>
<td scope="row" align='right' class="fieldlabel" height="30"><dmf:label nlsid='MSG_DOCBASE'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist name='<%=Login.CONTROL_DOCBASE%>' id='DocbaseName' tooltipnlsid='MSG_DOCBASE' onselect="onSelectDocbaseFromDropDown">
<dmf:dataoptionlist>
<dmf:option datafield="docbase" labeldatafield="docbase"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
<dmf:panel name='<%=Login.CONTROL_NETWORK_LOCATION_PANEL%>' visible='false'>
<tr>
<td scope="row" align='right' class="fieldlabel" height="30" nowrap><dmf:label nlsid='MSG_NETWORK_LOCATION'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist name='<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN%>' id='NetworkLocation' tooltipnlsid='MSG_NETWORK_LOCATION'>
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
<dmf:requiredfieldvalidator name="<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN_VALIDATOR%>" controltovalidate="<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN%>" nlsid="MSG_SELECT_NETWORK_LOCATION_REQUIRED" indicator="" />
</td>
</tr>
</dmf:panel>
<dmf:panel name='<%=Login.CONTROL_CREDENTIAL_PANEL%>'>
<tr>
<td height="5" colspan="3" class="spacer">&nbsp;</td>
</tr>
<tr>
<td scope="row" align='right'></td><td></td>
<td>
<div id="hideme" style="display:none;">
<dmf:checkbox name='<%=Login.CONTROL_SAVE_CREDENTIAL%>' nlsid='MSG_SAVE_OPTION'/>
</div>
</td>
</tr>
</dmf:panel>
<tr>
<td height='12' class="spacer" height="30">&nbsp;</td>
<td></td><td></td>
</tr>
<tr>
<td class="fieldlabel" nowrap align="left" height="20" colspan="3"><dmf:link name='<%=Login.CONTROL_SHOWOPTIONS%>' onclick='onShowOptions'/></td>
</tr>
<dmf:panel name='<%=Login.CONTROL_OPTIONSPANEL%>'>
<tr>
<td height='12' class="spacer" height="30">&nbsp;</td>
<td></td><td></td>
</tr>
<tr>
<td scope="row" align='right' class="fieldlabel" height="30"><dmf:label nlsid='MSG_DOMAIN'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:text name='<%=Login.CONTROL_DOMAIN%>' id='Domain' size='40' defaultonenter='true' tooltipnlsid='MSG_DOMAIN'/></td>
</tr>
<tr>
<td scope="row" align='right' class="fieldlabel" height="30"><dmf:label nlsid='MSG_LANGUAGE'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist name='<%=Login.CONTROL_LANGUAGE%>' onselect="onChangeLanguage" tooltipnlsid='MSG_LANGUAGE'>
<dmf:dataoptionlist>
<dmf:option datafield="locale" labeldatafield="language"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
<dmf:panel name='<%=Login.CONTROL_SERVER_PANEL%>'>
<tr>
<td scope="row" align='right' class="fieldlabel" height="30"><dmf:label nlsid='MSG_SERVER'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
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
<td scope="row" align='left'><dmf:checkbox name='<%=Login.CONTROL_ACCESS%>' nlsid='MSG_ACCESS' />
</tr>
</dmf:panel>
</table>
</td>
<td width="19" nowrap class="contentBackground">&nbsp;</td>
<td nowrap class="loginrightedge" width="11">&nbsp;</td>
</tr>
<tr>
<td width="11" nowrap class="loginleftedge">&nbsp;</td>
<td width="19" nowrap class="contentBackground">&nbsp;</td>
<td width="98%" class="contentBackground" valign="top">
<div class='loginerrorspacing'><dmf:panel name='<%=Login.CONTROL_ERRMSGPANEL%>'>
<dmf:label name='<%=Login.CONTROL_ERRMSG%>' style='{COLOR: #FF0000}'/>
</dmf:panel></div>
<td width="19" nowrap class="contentBackground">&nbsp;</td>
<td nowrap class="loginrightedge" width="11">&nbsp;</td>
</tr>
</table>
<table width="400" border="0" cellpadding="0" cellspacing="0" align="center">
<tr>
<td nowrap rowspan="3" class="loginleftedge" valign="bottom"><dmf:image src="images/login/login_btnbar_corner_left.gif" width="11" height="17" border="0"/></td>
<td nowrap colspan="2" class="modalnavbg" height="7">&nbsp;</td>
<td nowrap colspan="2" class="modalnavbg">&nbsp;</td>
<td nowrap colspan="2" class="modalnavbg">&nbsp;</td>
<td nowrap rowspan="3" class="loginrightedge" valign="bottom"><dmf:image src="images/login/login_btnbar_corner_right.gif" width="11" height="17" border="0"/></td>
</tr>
<tr>
<td nowrap class="modalnavbg">&nbsp;</td>
<td nowrap colspan="4" align="right" class="modalnavbg">
<table cellpadding="0" cellspacing="0" border="0" width="100%">
<tr>
<td align="left"><dmf:helpimage onclick='onClickHelp' nlsid='MSG_HELP' runatclient='true' src="images/icons/help_16.gif"/></td>
<td class="spacer" width="40">&nbsp;</td>
<td align="right" nowrap>
<table cellpadding="0" cellspacing="0" border="0">
<tr>
<td class="spacer"><div class='buttonbuffer'><dmf:button nlsid='MSG_LOGIN' onclick='onLogin' default='true' height='16' imagefolder='images/dialogbutton' accessible='true' tooltipnlsid='MSG_LOGIN_TIP'/></div></td>
<td><div class='buttonbuffer'><dmf:button name='<%=Login.CONTROL_CHANGEPASSWORD%>' cssclass='buttonLink' nlsid='<%=Login.MSG_CHANGE_PASSWORD%>' onclick='onChangePassword' height='16' imagefolder='images/dialogbutton' accessible='true' tooltipnlsid='MSG_CHANGE_PASSWORD_TIP'/></div></td>
</tr>
</table>
</td>
</tr>
</table>
</td>
<td nowrap class="modalnavbg">&nbsp;</td>
</tr>
<tr>
<td nowrap colspan="2" class="modalnavbg" height="7">&nbsp;</td>
<td nowrap colspan="2" class="modalnavbg">&nbsp;</td>
<td nowrap colspan="2" class="modalnavbg">&nbsp;</td>
</tr>
<tr>
<td nowrap class="spacer"><dmf:image src="images/login/login_btnbar_corner_lft_btm.gif" width="11" height="11" border="0"/></td>
<td nowrap colspan="2" class="loginbgbbtm">&nbsp;</td>
<td nowrap colspan="2" class="loginbgbbtm">&nbsp;</td>
<td nowrap colspan="2" class="loginbgbbtm">&nbsp;</td>
<td nowrap class="spacer"><dmf:image src="images/login/login_btnbar_corner_rt_btm.gif" width="11" height="11" border="0"/></td>
</tr>
<tr>
<td width="11" height="15" nowrap class="spacer">&nbsp;</td>
<td width="10" nowrap class="spacer">&nbsp;</td>
<td width="9" nowrap class="spacer">&nbsp;</td>
<td class="spacer" nowrap width="58%">&nbsp;</td>
<td class="spacer" nowrap width="58%">&nbsp;</td>
<td width="9" nowrap class="spacer">&nbsp;</td>
<td width="10" nowrap class="spacer">&nbsp;</td>
<td width="11" nowrap class="spacer">&nbsp;</td>
</tr>
</table>
<dmf:panel name='<%=Login.CONTROL_CENTEREDPANEL2%>'>
</div>
</dmf:panel>
</dmf:form>
</dmf:body>
</dmf:html>
