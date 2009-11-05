<%
/**
 *****************************************************************************
 *
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 *****************************************************************************
 *
 * Project        WDK
 * Module         New
 * File           productChangeContainer.jsp
 * Description    Container page for New components
 * Created on     April 22, 2002
 * Tab width      3
 * 
 *****************************************************************************
 *
 * PVCS Maintained Data
 *
 * Revision       $Revision: 17$
 * Modified on    $Date: 4/21/2005 10:03:51 AM$
 *
 * Log at EOF
 *
 *****************************************************************************
 */
%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>

<%@ page import="com.documentum.web.form.Form,
                 com.documentum.web.formext.component.DialogContainer" %>

<dmf:html>
   <dmf:head>
      <!--------------------------------------------------------------------->
      <!-- Confidential Property of Documentum, Inc.                       -->
      <!-- (c) Copyright Documentum, Inc. 2001.                            -->
      <!-- All Rights reserved.                                            -->
      <!-- May not be used without prior written agreement                 -->
      <!-- signed by a Documentum corporate officer.                       -->
      <!--------------------------------------------------------------------->
      <!-- Project   WDK                                                   -->
      <!-- Module    New                                             -->
      <!-- File      newContainer.jsp                                    -->
      <!--------------------------------------------------------------------->

      <dmf:webform/>
      <dmf:macclientdetect/>

      <dmf:title><dmf:label nlsid='MSG_TITLE'/></dmf:title>
   </dmf:head>

   <dmf:body cssclass='defaultPanesetBackground' marginheight='0' marginwidth='0'
      topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>

   <dmf:form>
      <dmf:paneset name="newContainerPane" rows="59,*,45" cssclass='defaultPanesetBackground'
         toppadding="25" leftpadding="70" rightpadding="70" bottompadding="25" minheight="400" minwidth="550">

      <!-- start header area-->
      <dmf:pane name="headerareaPane" overflow="hidden">
         <dmfx:fragment src='modal/modalContainerStart.jsp'/>
                  <dmf:label cssclass='dialogTitle' name="title"/>
         <dmfx:fragment src='modal/modalTabbarStart.jsp'/>            
         <dmfx:fragment src='modal/modalTabbarEnd.jsp'/>
      </dmf:pane>
      <!--end headerarea-->	

      <!--start content area-->
      <dmf:paneset name="contentareaPaneset" cols="11,*,11" cssclass="contentBackground">
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
      <!--end contentarea-->

      <!--start button area-->
      <dmf:pane name="buttonareaPane" overflow="hidden">
         <dmfx:fragment src='modal/modalButtonbarStart.jsp'/>
            <table cellpadding="0" cellspacing="0" border="0">
               <tr>
                  <td>
                     <dmf:button name='btnOk' cssclass="buttonLink" nlsid='MSG_OK' onclick='onOk'
                        height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_OK_TIP'/>
                  </td>
                  <td width=5></td>
                  <td>
                     <dmf:button name='btnCancel' cssclass="buttonLink" nlsid='MSG_CANCEL' onclick='onCancel'
                        height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>
                  </td>
                  <td width=5></td>
                  <td>
                     <dmf:button name='close' cssclass="buttonLink" nlsid='MSG_CLOSE' onclick='onClose'
                        height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CLOSE_TIP'/>
                  </td>
                  </tr>
               </table>
            <dmfx:fragment src='modal/modalButtonbarEnd.jsp'/>
            <dmfx:fragment src='modal/modalContainerEnd.jsp'/>
         </dmf:pane>
        <!--end buttonarea-->
       </dmf:paneset>
   </dmf:form>
   </dmf:body>
</dmf:html>
