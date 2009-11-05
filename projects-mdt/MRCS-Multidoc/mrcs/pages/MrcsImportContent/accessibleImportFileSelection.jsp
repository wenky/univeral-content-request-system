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

 Filename       $RCSfile: accessibleImportFileSelection.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:42 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/dmcontentxfer_1_0.tld" prefix="dmxfer" %>
<%@ page import="com.documentum.web.form.Form,
com.documentum.web.formext.component.DialogContainer"%>
<%@ page import="com.documentum.web.form.control.clientdetect.MacClientDetector" %>
<dmf:webform/>
<%
Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
String macOsX = MacClientDetector.getMacOsName();
if (macOsX != null)
{
if (macOsX.equals(MacClientDetector.MAC_OS9))
{
macOsX = "false";
}
else
{
macOsX = "true";
}
}
else
{
macOsX = "null";
}
//
%>
<dmf:html>
<dmf:head>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
<script>
var isOSX = "<%=macOsX%>";
function onSelectFile()
{
document.getElementById("fileselector").click();
}
//added for Mac os9 IE. The "change" event is not fired after
function onSelectFileChange()
{
onSelectFile();
onAddToList();
}
function selectFileList()
{
document.getElementById("filelist").focus();
}
function onAddToList()
{
var strFilenameWithPath = document.getElementById("fileselector").value;
var osPath = getOsSpecificPath(strFilenameWithPath);
if (strFilenameWithPath != null && strFilenameWithPath != "")
{
postServerEvent(null, null, null, "onAddToList", "filenameWithPath", osPath);
}
}
function onRemoveFromList()
{
var fileList = document.getElementById("filelist");
var nIndex = fileList.options.selectedIndex;
if ( nIndex >= 0 )
{
var strFilenameWithPath = fileList.options[nIndex].value;
postServerEvent(null, null, null, "onRemoveFromList", "filenameWithPath", strFilenameWithPath);
}
}
function getOsSpecificPath(strFilenameWithPath)
{
//On Mac Os the separator passed from Browser is ":"
//but jvm has problem to find a file when separator is ":"
var sepChar= ":";
var isIE = (navigator.appName == "Microsoft Internet Explorer");
if ( isOSX == "null" )
{
return strFilenameWithPath;
}
strFilenameWithPath = strFilenameWithPath.replace(/:/g, "/");
if (isOSX == "false")
{
startIt=true;
if ( strFilenameWithPath.charAt(0) != "/" )
{
strFilenameWithPath = "/" + strFilenameWithPath;
}
}
else if ((isOSX == "true") && !isIE)
{
strFilenameWithPath = "/" + strFilenameWithPath;
}
if ((isOSX == "true") )
{
// change the file sep because OSX IE uses '/' instead of ':'
var firstChar = strFilenameWithPath.charAt(0);
var secondSlash = strFilenameWithPath.indexOf(firstChar, 1) ;
strFilenameWithPath = strFilenameWithPath.substring(secondSlash);
}
return strFilenameWithPath;
}
</script>
</dmf:head>
<dmf:body cssclass='contentBorder' marginheight='0' marginwidth='12'
topmargin='0' bottommargin='0' leftmargin='12' rightmargin='0'>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr><td valign='top'>
<dmf:form>
<table width='100%' cellspacing='0' cellpadding='0' border='0'>
<tr height='5'><td valign='top'></td></tr>
<tr height='1'><td valign='top'>
<dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE_IMPORT_FILE_SELECTION'/>:&nbsp;
<dmf:label cssclass='dialogFileName' nlsid='MSG_OBJECT'/>
</td></tr>
<tr height='5'><td valign='top'></td></tr>
<tr class='contentBackground'><td align='left' valign='top'>
<table border="0" cellpadding="5" cellspacing="5" width='100%'>
<tr><td>
<b><dmf:label nlsid="MSG_SELECTED_FILES"/>:</b>
</td></tr>
<tr>
<td>
<dmf:listbox name="filelist" id="filelist" size="10" width="500" tooltipnlsid="MSG_SELECTED_FILES" />
</td>
</tr>
<tr>
<td>
<table height='100%' border='0' cellpadding='0' cellspacing='0'>
<tr>
<td>
<table border='0' cellpadding='0' cellspacing='0'>
<tr>
<dmf:panel name='IEButtons'>
<td>
<dmf:button cssclass="buttonLink" nlsid='MSG_ADD_TO_LIST' onclick='onSelectFileChange' runatclient='true'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_ADD_TO_LIST_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button cssclass="buttonLink" nlsid='MSG_REMOVE_FROM_LIST' onclick='onRemoveFromList' runatclient='true'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_REMOVE_FROM_LIST_TIP'/>
</td>
<span style='display:none'>
<input type='file' id='fileselector' onchange='onAddToList()'>
</span>
</dmf:panel>
<dmf:panel name='NSButtons'>
<td>
<input id='fileselector' size='1' style='width:<%=form.getString("MSG_BROWSE_BUTTON_WIDTH_PX")%>px' type='file' onclick='onAddToList()'>
</td>
<td width=5>
</td>
<td>
<dmf:button nlsid='MSG_REMOVE_FROM_LIST' onclick='onRemoveFromList' runatclient='true' tooltipnlsid='MSG_REMOVE_FROM_LIST_TIP'/>
</td>
</dmf:panel>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
<tr height='4'><td></td></tr>
<tr><td>
<br>
<table border='0' cellpadding='0' cellspacing='0'>
<tr>
<td>
<dmf:button name='prev' style='color:#000000' cssclass="buttonLink" nlsid='MSG_PREV' onclick='onPrev'
height='16' imagefolder='images/dialogbutton' tooltipnlsid="MSG_PREV_TIP" />
</td>
<td width=5>
</td>
<td>
<dmf:button name='next' cssclass='buttonLink' nlsid='MSG_NEXT' onclick='onNext'
height='16' imagefolder='images/dialogbutton' tooltipnlsid="MSG_NEXT_TIP"/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='ok' cssclass="buttonLink" nlsid='MSG_OK' onclick='onOk'
height='16' imagefolder='images/dialogbutton' tooltipnlsid="MSG_OK_TIP"/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='cancel' cssclass='buttonLink' nlsid='MSG_CANCEL' onclick='onCancel'
height='16' imagefolder='images/dialogbutton' tooltipnlsid="MSG_CANCEL_TIP"/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='<%=DialogContainer.CONTROL_HELPBUTTON %>' cssclass="buttonLink" nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'
height='16' imagefolder='images/dialogbutton' tooltipnlsid="MSG_HELP_TIP"/>
</td>
</tr>
</table>
</td></tr>
<tr height='4'><td valign='bottom'></td></tr>
</table>
</dmf:form>
</td></tr>
</table>
</dmf:body>
</dmf:html>
