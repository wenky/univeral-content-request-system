
<%@page import="com.medtronic.ecm.documentum.core.webtop.MdtLoginWithAppSelection"%><%
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
function onLogin()
{
try
{
fireAppIntgEvent("event=HideDialog", "busyCursor=true");
if (g_ai_enabled)
{
//This call can throw an exception with message="Cancel" and we
window.external.AppIntgProcessEvent("event=OnLogin");
}
}
catch(sError)
{
if (sError.message == "Cancel")
{
return;
}
}
postServerEvent(null, null, null, "onLogin");
}
function onCancel()
{
try
{
fireAppIntgEvent("event=HideDialog");
}
catch(sError)
{
}
}
function onChangePassword()
{
try
{
fireAppIntgEvent("event=HideDialog", "busyCursor=true");
}
catch(sError)
{
}
postServerEvent(null, null, null, "onChangePassword");
}
function onClickHelp()
{
fireClientEvent("InvokeHelp");
}
</script>
<script>
var varMenuMap="<dmfx:menugroupconfig menugroupid='appintgmenubar'></dmfx:menugroupconfig>";
</script>
<dmf:fireclientevent event='aiEvent' includeargname='true'>
<dmf:argument name='event'       value='ShowLogin'/>
<dmf:argument name='id'          value='login'/>
<dmf:argument name='width'       value='420'/>
<dmf:argument name='height'      value='320'/>
<dmf:argument name='title'       nlsid='MSG_LOGIN'/>
<dmf:argument name='sizepreference' value='true' />
<dmf:argument name='menumap'     value='aivar:varMenuMap'/>
</dmf:fireclientevent>
</dmf:head>
<dmf:body cssclass='contentSpecialBackground1' topmargin='0' bottommargin='0'
leftmargin='0' rightmargin='0' marginheight='0' marginwidth='0'>
<dmf:form>
<dmf:panel name='<%=Login.CONTROL_CENTEREDPANEL1%>'>
<table border='0' width='100%' cellspacing='0' cellpadding='10'>
<tr><td>
</dmf:panel>
<table align='center' width="400" cellspacing='0' cellpadding='0' border='0'>
<tr><td width="256"><dmf:image src="images/login/login_logo.gif" border="0"/></td></tr>
</table>
<table align='center' cellspacing='0' cellpadding='0' border='0'>
<tr>
<td class="spacer" height="30">&nbsp;</td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" width="30%" height="30"><dmf:label nlsid='MSG_USERNAME'/></td>
<td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:text id='LoginUsername' name='<%=Login.CONTROL_USERNAME%>' size='40' defaultonenter='true'  tooltipnlsid='MSG_USERNAME'/></td>
</tr>
<tr>
<td></td>
<td></td>
<td><dmf:requiredfieldvalidator name='<%=Login.CONTROL_USERNAME_VALIDATOR%>' controltovalidate='<%=Login.CONTROL_USERNAME%>' nlsid='<%=Login.MSG_USERNAME_REQUIRED%>' indicator=""/></td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30"><dmf:label nlsid='MSG_PASSWORD'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:password id='LoginPassword' name='<%=Login.CONTROL_PASSWORD%>' size='40' defaultonenter='true' tooltipnlsid='MSG_PASSWORD'/></td>
</tr>
<tr>
<td></td>
<td></td>
<td><dmf:requiredfieldvalidator name='<%=Login.CONTROL_PASSWORD_VALIDATOR%>' controltovalidate='<%=Login.CONTROL_PASSWORD%>' nlsid='<%=Login.MSG_PASSWORD_REQUIRED%>' indicator=""/></td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30"><dmf:label nlsid='MSG_DOCBASE'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td>
<dmf:datadropdownlist width='214' name='<%=Login.CONTROL_DOCBASE%>' id='DocbaseName' tooltipnlsid='MSG_DOCBASE' onselect="onSelectDocbaseFromDropDown" visible="false">
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
<dmf:requiredfieldvalidator name="<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN_VALIDATOR%>" controltovalidate="<%=Login.CONTROL_NETWORK_LOCATION_DROPDOWN%>" nlsid="<%=Login.MSG_SELECT_NETWORK_LOCATION_REQUIRED%>" indicator="" />
</td>
</tr>
</dmf:panel>
<tr>
<td height='1' class="spacer">&nbsp;</td>
<td></td><td></td>
</tr>
<tr>
<td scope="row" class="fieldlabel rightAlignment" height="30"><dmf:label nlsid='MSG_DOMAIN'/></td><td class="defaultcolumnspacer">:&nbsp;</td>
<td><dmf:text name='<%=Login.CONTROL_DOMAIN%>' id='Domain' size='40' defaultonenter='true' tooltipnlsid='MSG_DOMAIN'/></td>
</tr>
<tr>
<td height="30">&nbsp;</td>
<td>&nbsp;</td>
<td scope="row" class='leftAlignment'><dmf:checkbox name='<%=Login.CONTROL_ACCESS%>' nlsid='MSG_ACCESS' tooltipnlsid='MSG_ACCESS_TIP'/>
</tr>
<tr>
<td height='7' class="spacer">&nbsp;</td>
<td></td><td></td>
</tr>
<div class='loginerrorspacing'>
<dmf:panel name='<%=Login.CONTROL_ERRMSGPANEL%>'>
<dmf:fireclientevent event='aiEvent' includeargname='true'>
<dmf:argument name='event'       value='ShowDialog'/>
<dmf:argument name='id'          value='login'/>
<dmf:argument name='width'       value='420'/>
<dmf:argument name='height'      value='390'/>
<dmf:argument name='title'       nlsid='MSG_LOGIN'/>
</dmf:fireclientevent>
<tr>
<td colspan='4' >
<table align='center' cellpadding="0" cellspacing="0" border="0">
<tr align='center'>
<td>
<div id='Pane_LoginError' style='width:390px; height:70px; overflow:auto'>
<dmf:label name='<%=Login.CONTROL_ERRMSG%>'/>
</div>
</td>
</tr>
</table>
</td>
</tr>
</dmf:panel>
</div>
<tr>
<td height='7' class="spacer">&nbsp;</td>
<td></td><td></td>
</tr>
<tr>
<td colspan='3' align='center' nowrap>
<dmf:button name='Button_ChangePassword' id='changepasswordbtn' nlsid='MSG_CHANGE_PASSWORD' onclick='onChangePassword' runatclient='true'
tooltipnlsid='MSG_CHANGE_PASSWORD_TIP'/>&nbsp;
<dmf:button name='<%=Login.CONTROL_LOGINBUTTON%>' id='LoginButton' nlsid='MSG_LOGIN' onclick='onLogin' runatclient='true' default='true'
cssclass='buttonOkCancel' tooltipnlsid='MSG_LOGIN_TIP'/>&nbsp;
<dmf:button name='cancelbtn' id='cancelbtn' nlsid='MSG_CANCEL' onclick='onCancel' runatclient='true'
cssclass='buttonOkCancel' tooltipnlsid='MSG_CANCEL_TIP'/>
</td>
</tr>
</table>
<dmf:panel name='<%=Login.CONTROL_CENTEREDPANEL2%>'>
</td></tr>
</table>
</dmf:panel>
<dmf:checkbox name='<%=Login.CONTROL_SAVE_CREDENTIAL%>' nlsid='MSG_SAVE_OPTION' visible='false'/>
</dmf:form>
</dmf:body>
</dmf:html>
