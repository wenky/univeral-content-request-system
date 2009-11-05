<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
</dmf:head>
<dmf:body cssclass='contentBackground' topmargin='0' bottommargin='0'
leftmargin='0' rightmargin='0' marginheight='0' marginwidth='0'>
<dmf:form>
<table height='100%' width='100%' align='center' cellspacing='0' cellpadding='0' border='0'>
<tr><td valign='middle'>
<table align='center' cellspacing='0' cellpadding='0' border='0'>
<tr class='contentBorderDark'><td>
<table align='center' cellspacing='1' cellpadding='0' border='0'>
<tr class='contentBackground'><td>
<table width='350' align='center' cellspacing='0' cellpadding='0' border='0'>
<tr align='left'>
<td nowrap valign='middle'>&nbsp;
<dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE'/>:&nbsp;
<dmf:label cssclass='dialogTitle' name='object_name'/>
</td>
</tr>
<tr align='center'>
<td height='3' valign='middle' class="spacer">&nbsp;</td>
</tr>
<tr><td height='10' class="spacer">&nbsp;</td></tr>
<dmf:panel name='details'>
<tr><td>
<table cellspacing=3 cellpadding=3 border=0>
<tr>
<td width='32' class='doclistHeader'>
<dmfx:docbaseicon size='32' name="object_icon"/>
</td>
<td>
<table cellspacing=0 cellpadding=0 border=0>
<tr><td>
<b><dmf:label nlsid='MSG_VERSION' /></b>:&nbsp;
<dmf:label name='object_version' />
</td></tr>
<tr><td>
<b><dmf:label nlsid='MSG_FORMAT' /></b>:&nbsp;
<dmfx:docformatvalueformatter>
<dmf:label name='object_format' />
</dmfx:docformatvalueformatter>
</td></tr>
</table>
</td>
</tr>
<tr><td colspan='2'>
<dmf:checkbox name='opencurrent' nlsid='MSG_OPENCURRENT' onclick='onOpenCurrent'/>
</td></tr>
</table>
</td></tr>
</dmf:panel>
<dmf:panel name='error'>
<tr><td>
<table cellspacing=3 cellpadding=3 border=0>
<tr><td>
<dmf:label name="error_message"/>
</td></tr>
</table>
</td></tr>
</dmf:panel>
<tr><td>
<table height='100%' align='right' border='0' cellpadding='2' cellspacing='2'>
<tr>
<td>
<dmf:button name="view" cssclass="buttonLink" nlsid='MSG_VIEW' onclick='onViewClicked'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_VIEW_TIP'/>
</td>
<td>
<!--  dmf:button name='edit' cssclass='buttonLink' nlsid='MSG_EDIT' onclick='onEditClicked'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_EDIT_TIP'/ -->
<dmf:button name='mrcsedit' cssclass='buttonLink' nlsid='MSG_EDIT' onclick='onMrcsEditClicked'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_EDIT_TIP'/>
</td>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<td>
<dmf:button name='close' cssclass='buttonLink' nlsid='MSG_CLOSE' onclick='onCloseClicked'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CLOSE_TIP'/>
</td>
</dmfx:clientenvpanel>
<td>&nbsp;&nbsp;</td>
</tr>
</table>
</td></tr>
<tr><td height='10' class="spacer">&nbsp;</td></tr>
<tr align='center'><td valign='middle'>&nbsp;</td></tr>
</td></tr>
</table>
</table>
</td></tr>
</table>
</td></tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
