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

 Filename       $RCSfile: finishesgn.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:39 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmanager.FinishWorkflowTask" %>
<%@ page import="com.medtronic.documentum.mrcs.client.ESignHelper" %>
<dmf:webform/>
<dmf:form>
<table width='100%'>
<tr><td style='padding-left:10px'>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<tr>
<td colspan='2' class='taskmgrSectionHeading' height='24'>
<dmf:label nlsid='MSG_FINISH_ACTION_HDR'/>
</td>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
<tr>
<td colspan='2'>
<dmf:label nlsid='MSG_FINISH_TASK_INFO'/>
</td>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
<tr>
<td colspan='2'>
<dmf:label name='<%=ESignHelper.DOCREJECT_HEADER_LBL_CONTROL_NAME%>' nlsid='MSG_DOCRR_LBL'/>
</td>
</tr>
<tr>
<td height='5'>&nbsp;</td>
</tr>
<tr>
<td colspan='1'>
<dmf:label cssclass="defaultDocbaseAttributeStyle" name='<%=ESignHelper.DOCREJECT_DOCINFOHDR_LBL_CONTROL_NAME%>'  nlsid='MSG_DOCHDR_LBL'/>
</td>
<td colspan='1'>
<dmf:label cssclass="defaultDocbaseAttributeStyle" name='<%=ESignHelper.DOCREJECT_DOCINFORSNLBL_CONTROL_NAME%>'/>
</td>
</tr>
<tr>
<td colspan='1'>
<dmf:label cssclass="defaultDocbaseAttributeStyle" name='<%=ESignHelper.DOCREJECT_RSNHDR_LBL_CONTROL_NAME%>'  nlsid='MSG_DOCRSNHDR_LBL'/>
</td>
<td colspan='1'>
<dmf:label cssclass="defaultDocbaseAttributeStyle" name='<%=ESignHelper.DOCREJECT_RSNLBL_CONTROL_NAME%>'/>
</td>
</tr>
<dmf:panel name='<%=FinishWorkflowTask.SIGNOFF_PANEL_CONTROL_NAME%>'>
<tr>
<td colspan='2' height='24' class='taskmgrSectionHeading' style='padding-top: 10px; padding-bottom: 5px'>
<dmf:label nlsid='MSG_SIGNOFF_REQUIRED_HDR'/>
</td>
</tr>
<tr>
<td>
<jsp:include page="/mrcs/pages/esignPanel.jsp" />
<%-- invisible button used here as a proxy for handler invocation--%>
<dmf:button label='default' visible='false' enabled='true' default='true' onclick='onAttemptFinish'/>
</td>
</tr>
<tr>
<td height='10'>&nbsp;</td>
</tr>
</dmf:panel>
</table>
</td></tr>
</table>
</dmf:form>
