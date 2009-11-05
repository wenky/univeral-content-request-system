<%
//
%>
<%@ page import="com.documentum.web.formext.repository.RepositorySelector"%>
<%@ page import="com.medtronic.ecm.documentum.qad.reassigntask.MdtReassignTaskFrom"%>
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
<table>
	<tr>
		<td>
			<dmf:multiselector  name='<%=MdtReassignTaskFrom.FROM_USERS_LISTBOX%>' size="20" multiselect="false" orderingenabled="true" listboxwidth="300"  buttoncssclass="button" buttonimagefolder="images/button" />
		</td>
	</tr>
</table>
</dmf:form>
</dmf:body>
</dmf:html>
