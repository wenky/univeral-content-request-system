<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form,
com.documentum.web.formext.component.DialogContainer" %>
<%@ page import="com.documentum.webcomponent.library.propertysheetwizardcontainer.PropertySheetWizardContainer" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<dmf:macclientdetect/>
<dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
</dmf:head>
<dmf:body id="modal" marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>
<dmf:form>
<dmf:paneset name="mainPaneset" rows="59,*,45" cssclass='defaultPanesetBackground'
toppadding="0" bottompadding="0" leftpadding="70" rightpadding="70" minheight="400" minwidth="550">
<dmf:pane name="headerareaPane" overflow="hidden">
<dmfx:fragment src='modal/modalContainerStart.jsp'/>
<dmf:label cssclass='dialogTitle' name="title"/>
<dmfx:fragment src='modal/modalTabbarStart.jsp'/>
<dmf:tabbar name='<%=PropertySheetWizardContainer.TABBAR_CONTROL_NAME%>' tabposition='top' onclick='onTabSelected' scrollable='true' />
<dmfx:fragment src='modal/modalTabbarEnd.jsp'/>
</dmf:pane>
<dmf:paneset name="contentareaPaneset" cols="18,*,18" cssclass="contentBackground">
<dmf:pane name="leftcolumn" overflow="hidden">
<dmfx:fragment src='modal/modalEdgesStart.jsp'/>
</dmf:pane>
<dmf:pane name="scrollingcontent" overflow="auto">
<dmfx:fragment src='modal/modalContentGutterStart.jsp'/>
<dmf:label cssclass='doclistFolderPath' name="docbasePath"/><br>
<dmfx:containerinclude/>
<dmfx:fragment src='modal/modalContentGutterEnd.jsp'/>
</dmf:pane>
<dmf:pane name="rightcolumn" overflow="hidden">
<dmfx:fragment src='modal/modalEdgesEnd.jsp'/>
</dmf:pane>
</dmf:paneset>
<dmf:pane name="buttonareaPane" overflow="hidden">
<dmfx:fragment src='modal/modalButtonbarStart.jsp'/>
<!--
<dmf:button name='<%=PropertySheetWizardContainer.PREV_BUTTON_CONTROL_NAME%>' cssclass="buttonLink" nlsid='MSG_PREV' onclick='onPrevComponent'
height='16' tooltipnlsid='MSG_PREV_TIP'/>
-->
<dmf:button name='<%=PropertySheetWizardContainer.NEXT_BUTTON_CONTROL_NAME%>' cssclass='buttonLink' nlsid='MSG_NEXT' onclick='onNextComponent'
height='16' tooltipnlsid='MSG_NEXT_TIP'/>
<dmf:button name='<%=PropertySheetWizardContainer.OK_BUTTON_CONTROL_NAME%>' cssclass="buttonLink" nlsid='MSG_OK' onclick='onOk'
height='16' tooltipnlsid='MSG_OK_TIP'/>
<dmf:button name='<%=PropertySheetWizardContainer.CANCEL_BUTTON_CONTROL_NAME%>' cssclass="buttonLink" nlsid='MSG_CANCEL' onclick='onCancel'
height='16' tooltipnlsid='MSG_CANCEL_TIP'/>
<dmf:button name='close' cssclass="buttonLink" nlsid='MSG_CLOSE' onclick='onClose'
height='16' tooltipnlsid='MSG_CLOSE_TIP'/>
<dmfx:fragment src='modal/modalButtonbarEnd.jsp'/>
<dmfx:fragment src='modal/modalContainerEnd.jsp'/>
</dmf:pane>
</dmf:paneset>
</dmf:form>
</dmf:body>
</dmf:html>
