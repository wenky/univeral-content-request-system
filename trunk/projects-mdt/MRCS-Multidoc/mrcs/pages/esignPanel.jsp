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

 Filename       $RCSfile: esignPanel.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2007/03/07 20:02:04 $

***********************************************************************
--%>

<%
//<dmf:requiredfieldvalidator name="usrval" controltovalidate= "attribute_object_usr" errormessage= "You must enter your user name" /></td>
//<dmf:requiredfieldvalidator name="pswdval" controltovalidate= "attribute_object_pswd" errormessage= "You must supply your current password" />

%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmanager.RepeatWorkflowTask,
 com.medtronic.documentum.mrcs.client.ESignHelper" %>

<dmf:head>
<dmf:webform validation="false"/>
<script language="JavaScript1.1">
<!--

function setReason(){
var rsnTxt =  document.getElementById("<%=ESignHelper.REASONSELECT_CONTROL_NAME%>").value ;
	if(rsnTxt == "<%=ESignHelper.DEFAULT_REASON%>"){
		document.getElementById("<%=ESignHelper.REASONTXT_CONTROL_NAME%>").value = "" ;
		}
	else{
		document.getElementById("<%=ESignHelper.REASONTXT_CONTROL_NAME%>").value = rsnTxt;
		}

}


// -->
</script>
</dmf:head>


<table width='100%'>
<tr><td style='padding-left:10px'>
<table border="0" cellpadding="2" cellspacing="0" width="100%">

<tr>
<td>
<dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_USR"/>
&nbsp;&nbsp;<dmf:text name="<%=ESignHelper.USRTXT_CONTROL_NAME%>"  id = "<%=ESignHelper.USRTXT_CONTROL_NAME%>" size="40" tooltipnlsid='MSG_USR' />
&nbsp;
<dmf:requiredfieldvalidator name="usrval" controltovalidate= "<%=ESignHelper.USRTXT_CONTROL_NAME%>" nlsid= "MSG_USRNAME_REQ" /></td>

</tr>

<tr>
<td>
<dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_PSWD"/>
&nbsp;&nbsp;&nbsp;&nbsp;<dmf:password name="<%=ESignHelper.PASSWORD_CONTROL_NAME%>" id = "<%=ESignHelper.PASSWORD_CONTROL_NAME%>" tooltipnlsid='MSG_PSWD'/>
<dmf:requiredfieldvalidator name="pswdval" controltovalidate= "<%=ESignHelper.PASSWORD_CONTROL_NAME%>" nlsid= "MSG_PSWD_REQ" />
</td>
</tr>

<tr>
<td>
<dmf:label cssclass="defaultDocbaseAttributeStyle" nlsid="MSG_REASON"/>

&nbsp;&nbsp;<dmf:dropdownlist name="<%=ESignHelper.REASONSELECT_CONTROL_NAME%>" id ="<%=ESignHelper.REASONSELECT_CONTROL_NAME%>"
tooltipnlsid="MSG_REASON" onselect="setReason" runatclient="true" value="Select or Enter the Reason for Signing">
<dmf:option value="Select or Enter the Reason for Signing" label="Select or Enter the Reason for Signing"/>
</dmf:dropdownlist>

<dmf:text name="<%=ESignHelper.REASONTXT_CONTROL_NAME%>"  id="<%=ESignHelper.REASONTXT_CONTROL_NAME%>" cssclass="defaultDocbaseAttributeStyle" size="50" tooltipnlsid="MSG_NEWTEXT" focus="false" />
&nbsp;
<dmf:requiredfieldvalidator name="rsnval" controltovalidate= "<%=ESignHelper.REASONTXT_CONTROL_NAME%>" nlsid= "MSG_REASON_REQ" /></td>
<%-- invisible button used here as a proxy for handler invocation--%>
<dmf:button label='default' visible='false' enabled='true' default='true' onclick='onAttemptFinish'/>
</td>
</tr>

</table>
</table>

