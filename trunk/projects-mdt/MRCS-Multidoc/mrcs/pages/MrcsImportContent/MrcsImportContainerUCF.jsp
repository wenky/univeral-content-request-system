<%@ page import="com.documentum.web.formext.component.DialogContainer"%>
<%--
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<dmf:webform validation="false"/>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
</dmf:head>
<dmf:body cssclass='defaultPanesetBackground' marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>
<dmf:form>
<dmf:paneset name="mainPaneset" rows="59,*,45" cssclass='defaultPanesetBackground'
minheight="300" minwidth="350">
<dmf:pane name="headerareaPane" overflow="hidden">
<dmfx:fragment src='modal/modalContainerStart.jsp'/>
<dmf:label cssclass='dialogTitle' nlsid='MSG_TITLE'/>:&nbsp;<dmf:label cssclass='dialogFileName' nlsid='MSG_OBJECT'/>
<dmfx:fragment src='modal/modalNavbarStart.jsp'/>
&nbsp;
<dmf:panel name="folderFileIndicatorPanel" >
<script type='text/javascript'>function doNothing() {return false;}</script>
<dmf:tabbar name="folderFileIndicatorTabbar" normalstyle="cursor: default;" selectedstyle="cursor: default;">
<dmf:tab name="folderIndicatorTab" nlsid="MSG_FOLDERS_INDICATOR_TITLE" onclick="doNothing" runatclient="true"/>
<dmf:tab name="fileIndicatorTab" nlsid="MSG_FILES_INDICATOR_TITLE" onclick="doNothing" runatclient="true"/>
</dmf:tabbar>
</dmf:panel>
<dmfx:fragment src='modal/modalNavbarEnd.jsp'/>
</dmf:pane>
<dmf:paneset name="contentareaPaneset" cols="11,*,11" cssclass="contentBackground">
<dmf:pane name="leftcolumn" overflow="hidden">
<dmfx:fragment src='modal/modalEdgesStart.jsp'/>
</dmf:pane>
<dmf:pane name="scrollingcontent" overflow="auto">
<dmfx:fragment src='modal/modalContentGutterStart.jsp'/>
<dmfx:containerinclude/>
<dmfx:fragment src='modal/modalContentGutterEnd.jsp'/>
</dmf:pane>
<dmf:pane name="rightcolumn" overflow="hidden">
<dmfx:fragment src='modal/modalEdgesEnd.jsp'/>
</dmf:pane>
</dmf:paneset>
<dmf:pane name="buttonareaPane" overflow="hidden">
<dmfx:fragment src='modal/modalButtonbarStart.jsp'/>
<table align='right' border='0' cellpadding='0' cellspacing='0'>
<tr>
<td>
<!--  dmf:button name='prev' cssclass="buttonLink" nlsid='MSG_PREV' onclick='onPrev' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_PREV_TIP'/ -->
</td>
<td width=5></td>
<td>
<!--  dmf:button name='next' cssclass='buttonLink' nlsid='MSG_NEXT' onclick='onNext' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_NEXT_TIP'/ -->
<dmf:button name='mrcsnext' cssclass='buttonLink' nlsid='MSG_NEXT' onclick='onMrcsNextPage' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_NEXT_TIP'/>
</td>
<td width=5></td>
<td>
<dmfx:clientenvpanel environment="appintg" reversevisible="true" >
<!--  dmf:button name='ok' cssclass="buttonLink" nlsid='MSG_OK' onclick='onOk' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_OK_TIP'/ -->
</dmfx:clientenvpanel>
<dmfx:clientenvpanel environment="appintg">
<!--  dmf:button name='ok' cssclass="buttonLink" nlsid='MSG_SAVE' onclick='onOk' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_SAVE'/ -->
</dmfx:clientenvpanel>
</td>
<td width=5></td>
<td>
<dmf:button name='cancel' cssclass='buttonLink' nlsid='MSG_CANCEL' onclick='onCancel' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>
</td>
<td width=5></td>
<td>
<!-- dmf:button name='close' cssclass="buttonLink" nlsid='MSG_CLOSE' onclick='onClose' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CLOSE_TIP'/ -->
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
