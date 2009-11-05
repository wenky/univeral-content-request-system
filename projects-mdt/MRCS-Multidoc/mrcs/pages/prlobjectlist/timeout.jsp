<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ page import="com.documentum.web.common.LocaleService" %>
<%@ page import="com.documentum.web.formext.config.ConfigService" %>
<%@ page import="com.documentum.web.formext.config.Context" %>
<%@ page import="java.util.Locale" %>
<%
String strLocale = ConfigService.getConfigLookup().lookupString(LocaleService.LOCALE_CONFIG_PATH, Context.getApplicationContext());
if (strLocale != null && strLocale.trim().length() > 0)
{
try
{
Locale locale = LocaleService.createLocale(strLocale);
if (locale != null)
{
LocaleService.setLocale(locale);
}
}
catch (Throwable tCreateLocaleErr)
{
}
}
//
%>
<html>
<head>
<dmf:webform formclass="com.documentum.web.form.Form" nlsbundle='com.documentum.web.form.FormProcessorNlsProp'/>
<title><dmf:label nlsid='MSG_TIMEOUT'/></title>
<script>
function relogin()
{
setTimeout("loginRedirect()", 3000);
}
function loginRedirect()
{
getTopLevelWnd().location.replace(g_virtualRoot+"/mrcs/pages/prlobjectlist/prlcustom_nodocuments.jsp");
}
</script>
</head>
<body class='contentBackground' topmargin='10' bottommargin='10'
leftmargin='13' rightmargin='0' marginheight='10' marginwidth='13' onload='relogin()'>
<dmf:form>
<h3><dmf:label nlsid='MSG_TIMEOUT'/></h3>
<dmf:label nlsid='MSG_TIMEOUT_MESSAGE'/>
</dmf:form>
</body>
</html>
