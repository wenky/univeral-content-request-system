<%--
--%>
<%@ page import="com.documentum.web.form.Form"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
</dmf:head>
<dmf:body cssclass='defaultPanesetBackground' marginheight='0' marginwidth='12'
topmargin='0' bottommargin='0' leftmargin='12' rightmargin='0'>
<dmf:form>
<dmf:paneset name="mainPaneset" rows="59,*,45" cssclass='defaultPanesetBackground'
minheight="390" minwidth="600">
<dmf:pane name="headerareaPane" overflow="hidden">
<dmfx:fragment src='modal/modalContainerStart.jsp'/>
<dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE_IMPORT_FILE_SELECTION'/>:&nbsp;
<dmf:label cssclass='dialogFileName' nlsid='MSG_OBJECT'/>
<dmfx:fragment src='modal/modalNavbarStart.jsp'/>
&nbsp;
<dmfx:fragment src='modal/modalNavbarEnd.jsp'/>
</dmf:pane>
<dmf:paneset name="contentareaPaneset" cols="11,*,11" cssclass="contentBackground">
<dmf:pane name="leftcolumn" overflow="hidden">
<dmfx:fragment src='modal/modalEdgesStart.jsp'/>
</dmf:pane>
<dmf:pane name="scrollingcontent" overflow="auto">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b><dmf:label nlsid="MSG_SELECTED_FILES_FOLDERS"/>:</b>
<dmfx:fragment src='modal/modalContentGutterStart.jsp'/>
<dmf:fileselectorapplet name="fileselector" folderselect="false" height="250" width="500" folderselectmode="tree"/>
<dmfx:fragment src='modal/modalContentGutterEnd.jsp'/>
</dmf:pane>
<dmf:pane name="rightcolumn" overflow="hidden">
<dmfx:fragment src='modal/modalEdgesEnd.jsp'/>
</dmf:pane>
</dmf:paneset>
<dmf:pane name="buttonareaPane" overflow="hidden">
<dmfx:fragment src='modal/modalButtonbarStart.jsp'/>
<table align='right' cellspacing='4' cellpadding='0' border='0'>
<tr>
<td>
<dmf:button name='prev' style='color:#000000' cssclass="buttonLink" nlsid='MSG_PREV' onclick='onPrev'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_PREV_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='next' cssclass='buttonLink' nlsid='MSG_NEXT' onclick='onNext'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_NEXT_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='ok' cssclass="buttonLink" nlsid='MSG_OK' onclick='onOk'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_OK_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='cancel' cssclass='buttonLink' nlsid='MSG_CANCEL' onclick='onCancel'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>
</td>
</tr>
</table>
<dmfx:fragment src='modal/modalButtonbarEnd.jsp'/>
<dmfx:fragment src='modal/modalContainerEnd.jsp'/>
</dmf:pane>
</dmf:paneset>
</dmf:form>
</dmf:body>
</dmf:html>
