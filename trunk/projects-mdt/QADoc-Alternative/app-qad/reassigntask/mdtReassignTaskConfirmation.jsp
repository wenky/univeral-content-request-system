<%
//
%>
<%@ page import="com.documentum.web.formext.repository.RepositorySelector"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
</dmf:head>
<dmf:body cssclass='contentBackground' topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0' marginheight='0' marginwidth='0'>
<dmf:form>
<table border="0" cellpadding="1" cellspacing="1">
	<tr>
		<td>
			<dmf:label nlsid="MSG_FROM_USER" />
		</td>
		<td>
			<dmf:text name='MSG_FROM_USER_NAME' size="50" id='MSG_FROM_USER_NAME' tooltipnlsid="MSG_FROM_USER_NAME"/>
		</td>
	</tr>
	<tr>
		<td>
			<dmf:label nlsid="MSG_TASK_NAME" />
		</td>
		<td>
			<dmf:text name='MSG_ACTUAL_TASK_NAME' size="150" id='MSG_ACTUAL_TASK_NAME'  tooltipnlsid="MSG_ACTUAL_TASK_NAME" />
		</td>

	</tr>
	<tr>
		<td>
			<dmf:label nlsid="MSG_TO_USER" />
		</td>
		<td>
			<dmf:text name='MSG_TO_USER_NAME' size="50" id='MSG_TO_USER_NAME' tooltipnlsid="MSG_TO_USER_NAME"/>
		</td>

	</tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>


