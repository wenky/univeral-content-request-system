<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.webcomponent.navigation.doclist.DocList" %>
<html>
<head>
<dmf:webform/>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
<script>
function onClickObject(obj, id, type, isFolder)
{
if (isFolder == '1')
{
fireClientEvent("onClickContentObject", id, type);
}
postServerEvent(null, null, null, "onClickObject", "objectId", id, "type", type, "isFolder", isFolder);
}
</script>
</head>
<body class='contentBackground' marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>
<dmf:form>
<%@ include file='/mrcs/pages/MrcsClassicThumbnailDocListBody.jsp' %>
<!--  %@ include file='/webcomponent/navigation/doclist/doclist_thumbnail_body.jsp' % -->
</dmf:form>
</body>
</html>
