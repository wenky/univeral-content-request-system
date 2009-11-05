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

 Filename       $RCSfile: installFullContentXfer.jsp,v $
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
<%@ page import="com.documentum.nls.NlsResourceBundle,
com.documentum.web.common.HttpSessionState,
com.documentum.web.formext.config.HttpPreferenceStore,
com.documentum.web.formext.session.SessionManagerHttpBinding,
com.documentum.fc.client.IDfSessionManager,
com.documentum.fc.client.IDfSession" %>
<%@ page import="com.documentum.web.common.LocaleService" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.web.util.Browser" %>
<%@ page import="com.documentum.web.contentxfer.common.IContentXferConstants" %>
<%@ page import="com.documentum.web.form.control.clientdetect.MacClientDetector" %>
<%@ page import="javax.servlet.http.HttpUtils" %>
<%
String macSet=request.getParameter("appleFinished");
if (macSet != null&&macSet.equals("true"))
{
session.setAttribute("appleFinished", "true");
}
//
%>
<dmf:webform/>
<%
Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
String realUrl="";
String macFinished=(String)session.getAttribute("appleFinished");
if(macFinished!=null&&macFinished.equals("true"))
{
session.removeAttribute("appleFinished");
}
else if(Browser.isMac(request))
{
String url=HttpUtils.getRequestURL(request).toString();
int in=url.indexOf("checkFullApplet");
if (in != -1)
{
realUrl=url.substring(0,in)+"installFullContentXfer"+url.substring(in+15);
}
else
{
realUrl = url;
}
java.util.Enumeration enu= request.getParameterNames();
int i=0;
while(enu.hasMoreElements())
{
String name=(String)enu.nextElement();
String value=request.getParameter(name);
if(i==0)
realUrl += "?" + name + "=" + value;
else
realUrl += "&" + name + "=" + value;
i++;
}
}
// check if it's a Mac client
boolean macos9 = false;
boolean macosx = false;
String macOsName = MacClientDetector.getMacOsName();
if (macOsName != null)
{
// it's either one of two
macos9 = macOsName.equals(MacClientDetector.MAC_OS9);
macosx = macOsName.equals(MacClientDetector.MAC_OSX);
}
String strArchivePath_os9 = IContentXferConstants.DEFAULT_CODEBASE_URL + "mac_FullContentxfer.jar";
String strArchivePath_osx = IContentXferConstants.DEFAULT_CODEBASE_URL + "mac_FullContentxfer_osx.jar";
String strArchivePath_Jar = IContentXferConstants.DEFAULT_CODEBASE_URL + "FullContentXfer.jar";
String contextPath_os9 = request.getContextPath() +  strArchivePath_os9  ;
String contextPath_osx = request.getContextPath() + strArchivePath_osx ;
String strContentXferArchivePath = null;
if ( macos9 == true )
{
strContentXferArchivePath = strArchivePath_os9;
}
else
if ( macosx == true )
{
strContentXferArchivePath = strArchivePath_osx;
}
else
{
strContentXferArchivePath = strArchivePath_Jar;
}
NlsResourceBundle s_lookup   = new NlsResourceBundle("com.documentum.webcomponent.library.contentxfer.ContentTransferNlsProp");
String strFullUri            = Form.makeUrl(request, strContentXferArchivePath);
String strContentXferFullUrl = Form.makeFullUrl(request, strFullUri);
int contentXferArchiveSize   = Form.getContentSize(strContentXferFullUrl) / 1024;
Object[] argsArray = { new Integer( contentXferArchiveSize ) };
String strContentXferCabFileInstall = s_lookup.getString("MSG_INSTALLING_FULL_PLEASE_WAIT", argsArray, LocaleService.getLocale());
//
%>
<dmf:html>
<dmf:head>
<%--  <dmf:title><dmf:label nlsid='MSG_INSTALL_FULL_CONTENT_TRANSFER_APPLETS'/></dmf:title> --%>
<script language='JavaScript'>
var macDone='<%=macFinished%>';
var macos=false;
var navInstall=true;
if ( (navigator.userAgent.indexOf("Mac_PowerPC") != -1) || (navigator.userAgent.indexOf("Macintosh") != -1) )
{
macos=true;
}
function finishPC()
{
if(macos)
{
if(macDone=='true'||macDone==true)
{
finish(true);
}
else
{
document.all.installing.style.visibility = "hidden";
document.all.warning.style.visibility = "hidden";
document.all.checking.style.visibility = "visible";
return;
}
}
else
{
finish(false);
}
}
function finish(isMac)
{
if(isMac)
{
setFullInstallCookie();
alert('<%=form.getString("MSG_INSTALL_FULL_SUCCESS_MESSAGE")%>');
postServerEvent(null, null, null, "onInstallFullComplete");
}
else
{
var bIsIE = (document.all) ? true : false;
if ( bIsIE == true )
{
if (document.cookie.indexOf("isMicrosoftVm=true") == -1)
{
finishPlugin();
}
else
{
setFullInstallCookie();
postServerEvent(null, null, null, "onInstallFullComplete");
}
}
else
{
finishPlugin();
}
}
}
function finishPlugin()
{
setFullInstallCookie();
alert('<%=form.getString("MSG_INSTALL_FULL_SUCCESS_MESSAGE")%>');
postServerEvent(null, null, null, "onInstallFullComplete");
}
function setFullInstallCookie()
{
var expireDate = new Date();
expireDate = new Date(expireDate.getTime() + (24 * 60 * 60 * 1000 * 365 * 10));
var strFullInstallCookie = "<%=IContentXferConstants.FULL_INSTALL_ARCHIVE_COOKIE_NAME%>=true; expires=" + expireDate.toGMTString() + "; path=/";
document.cookie = strFullInstallCookie;
}
function handleInstall()
{
window.location.reload();
}
function handleExit()
{
postServerEvent(null, null, null, "onComponentReturn");
}
</script>
</dmf:head>
<dmf:body onload='finishPC()' topmargin='20' leftmargin='0' rightmargin='0' bottommargin='0' marginheight='20' marginwidth='0'>
<dmf:form>
<div id='checking' style='visibility:hidden'>
<table width='100%' cellpadding='0' cellspacing='0'>
<tr>
<td class='heading' align='center'>
<dmf:label nlsid='MSG_CHECKING_PERMISSION_PLEASE_WAIT'/>
</td>
</tr>
</table>
</div>
<div id='installing' style='visibility:visible'>
<table width='100%' cellpadding='0' cellspacing='0'>
<tr>
<td class='heading' align='center'>
<dmf:label label='<%=strContentXferCabFileInstall%>'/>
</td>
</tr>
<tr>
<td class='heading' align='center'>
<dmf:image name='busy_image' src='images/animated/busy.gif' width='58' height='57'/>
</td>
</tr>
</table>
</div>
<div id='warning' style='visibility:hidden'>
<table width='100%' cellpadding='0' cellspacing='0'>
<tr>
<td class='heading' align='center'>
<dmf:label nlsid='MSG_WONT_WORK_WITHOUT_FULL_APPLETS'/>
<br><br>
</td>
</tr>
<tr>
<td align='center'>
<dmf:button nlsid='MSG_INSTALL' runatclient="true" onclick='handleInstall' tooltipnlsid='MSG_INSTALL'/>
<dmf:button nlsid='MSG_EXIT_INSTALL' runatclient="true" onclick='handleExit' tooltipnlsid='MSG_EXIT_INSTALL'/>
</td>
</tr>
</table>
</div>
<%
if( macos9&&!(macFinished!=null&&macFinished.equals("true")))
{
%>
<applet archive='<%=contextPath_os9%>' code='com.documentum.web.contentxfer_5_2_5.applet.MacInstallationApplet' width='100' height='100' id='installationApplet' name='installationApplet'  alt='installationApplet'>
<param name='successMessage' value='<%=form.getString("MSG_INSTALL_FULL_SUCCESS_MESSAGE")%>'>
<param name='archive' value='<%=contextPath_os9%>'>
<param name='mac' value='true'>
<param name='fromPage' value='<%=realUrl%>'>
</applet>
<%
}
else if (( Browser.isIE(request) || Browser.isMac(request))
&& !(macFinished != null && macFinished.equals("true"))
)
{
if (Browser.isMac(request))
{
%>
<applet archive='<%=contextPath_osx%>' code='com.documentum.web.contentxfer_5_2_5.applet.MacInstallationApplet' width='100' height='100' id='installationApplet' name='installationApplet'  alt='installationApplet'>
<param name='mac' value='true'>
<%
}
else
{
%>
<applet code='com.documentum.web.contentxfer_5_2_5.applet.InstallationApplet' width='0' height='0' id='installationApplet' name='installationApplet'  alt='installationApplet'
codebase='<%=request.getContextPath() + IContentXferConstants.DEFAULT_CODEBASE_URL%>'>
<param name='useslibrary' value='<%=IContentXferConstants.APPLET_LIB_NAME + "_"%>'>
<param name='useslibrarycodebase' value='FullContentXfer.cab'>
<param name='useslibraryversion' value='<%=IContentXferConstants.VERSION_NUMBER%>'>
<%
}
%>
<param name='successMessage' value='<%=form.getString("MSG_INSTALL_SUCCESS_MESSAGE")%>'>
<param name='fromPage' value='<%=realUrl%>'>
</applet>
<%
}
%>
</dmf:form>
</dmf:body>
</dmf:html>
<%
//
%>
