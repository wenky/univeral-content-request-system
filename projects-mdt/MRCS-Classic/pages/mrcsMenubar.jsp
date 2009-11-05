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

 Filename       $RCSfile: mrcsMenubar.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/01/12 22:02:38 $

***********************************************************************
--%>

<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.web.form.Form,
java.util.Enumeration" %>
<%@ page import="com.documentum.web.form.Control" %>
<%@ page import="com.documentum.web.common.ArgumentList" %>
<%
//
%>
<html>
<head>
<dmf:webform/>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
<script>
function onClickHelp()
{
fireClientEvent("InvokeHelp");
}
</script>
</head>
<%
Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
boolean bAccessibleParam = form.isAccessible();
String strBodyOptions = null;
String strMenugroupTableOptions = null;
if (!bAccessibleParam)
{
strBodyOptions = "class='webtopMenubarBackground' marginheight='1' marginwidth='8' leftmargin='8' rightmargin='0' topmargin='1' bottommargin='0'";
strMenugroupTableOptions = "height='100%' border='0' cellpadding='0' cellspacing='0'";
}
else
{
strBodyOptions = "class='contentBackground' marginheight='0' marginwidth='0' topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'";
strMenugroupTableOptions = "width='100%' class='contentBackground' border='0' cellpadding='0' cellspacing='10'";
}
%>
<body <%=strBodyOptions%> >
<dmf:form>
<dmf:panel name='accessheaderpanel'>
<table width='100%' border='0' cellpadding='0'>
<tr class=headerBackground>
<td height=40 colspan=2>
<table cellspacing=0 cellpadding=0 border=0>
<tr>
<td align=left>
<dmf:label name='location' cssclass='webcomponentBreadcrumb'/>
</td>
</tr>
<tr>
<td align=left>
<dmf:label name='title_label' cssclass='webcomponentTitle'/>
</td>
</tr>
</table>
</td>
</tr>
<tr class=headerBackground>
<td>
<table cellspacing=2 cellpadding=2 border=0>
<tr>
<td width='1' class='doclistHeader'><dmfx:docbaseicon size='32' name="icon"/></td>
<td>
<table cellspacing=0 cellpadding=0 border=0>
<tr><td><dmf:label name='object_name' cssclass='doclistHeader' />&nbsp;<dmf:bookmarklink name="bookmark" /></td></tr>
<tr><td><dmf:label nlsid='MSG_OBJ_TYPE' cssclass='doclistHeader' />&nbsp;<dmf:label name='r_object_type' cssclass='doclistHeader'/></td></tr>
<tr><td><dmf:label nlsid='MSG_CONTENT_TYPE' cssclass='doclistHeader' />&nbsp;<dmf:label name='a_content_type' cssclass='doclistHeader'/></td></tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
<tr height='4'><td></td></tr>
</table>
</dmf:panel>
<dmf:panel name='menubarpanel'>
<dmf:menugroup name='menugroup' target='content' imagefolder='images/menubar' accessible='<%=new Boolean(bAccessibleParam).toString()%>'>
<table <%=strMenugroupTableOptions%> >
<dmf:panel name='accessheaderlinkspanel'>
<tr>
<td align='left' colspan='3'>
<dmf:label name='tableTitleLabel' nlsid='MSG_TABLE_TITLE'/>
</td>
<td align='right'>
<table>
<tr>
<td>
<dmf:link name='cancelLink' cssclass='defaultLinkStyle' nlsid='MSG_CANCEL' onclick='onCancel'/>
</td>
<td width=5>
</td>
<td>
<dmf:link name='helpLink' cssclass="defaultLinkStyle" nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'/>
</td>
</tr>
</table>
</td>
</tr>
</dmf:panel>
<tr>
<td><dmf:image name='farleftimg' id='farleftimg' src='images/menubar/farleft.gif'/></td>
<td>
<dmf:menu name='file_menu' nlsid='MSG_FILE' width='50'>
<dmf:menu name='file_new_menu' nlsid='MSG_NEW'>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newdocument' nlsid='MSG_NEW_DOCUMENT' action='newdocument' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newxforms' nlsid='MSG_NEW_XFORMS' action='newxforms' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newfolder' nlsid='MSG_NEW_FOLDER' action='newfolder' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newcabinet' nlsid='MSG_NEW_CABINET' action='newcabinet' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newuser' nlsid='MSG_NEW_USER' action='newuser' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newgroup' nlsid='MSG_NEW_GROUP' action='newgroup' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newrole' nlsid='MSG_NEW_ROLE' action='newrole' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='newpermissionset' nlsid='MSG_NEW_PERMISSION_SET' action='newacl' showifinvalid='false'/>
</dmf:menu>
<dmfx:actionmenuitem dynamic='genericnoselect' name='userimport' nlsid='MSG_USER_IMPORT' action='userimport' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_adduserorgroup' nlsid='MSG_ADD_USER_OR_GROUP' action='adduserorgroup' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_edit' nlsid='MSG_EDIT_FILE' action='editfile' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' id='file_annotate' name='file_annotate' nlsid='MSG_ANNOTATE' action='annotate' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' id='file_view' name='file_view' nlsid='MSG_VIEW_FILE' action='view' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' id='file_saveas' name='file_saveas' nlsid='MSG_SAVE_AS' action='saveasxforms' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_checkin' nlsid='MSG_CHECKIN' action='checkin' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_checkout' nlsid='MSG_CHECKOUT' action='checkout' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_cancelcheckout' nlsid='MSG_CANCEL_CHECKOUT' action='cancelcheckout' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='file_comment' nlsid='MSG_COMMENT' action='comment' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_import' nlsid='MSG_IMPORT' action='import' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_export' nlsid='MSG_EXPORT' action='export' showifinvalid='false' showifdisabled="true" />
<dmfx:actionmenuitem dynamic='singleselect' name='file_exportrendition' nlsid='MSG_EXPORT' action='exportrendition' showifinvalid='false' showifdisabled="true" />
<dmfx:actionmenuitem dynamic='multiselect' name='file_delete' nlsid='MSG_DELETE' action='delete' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='removeuserorgroup' nlsid='MSG_REMOVE_USER_OR_GROUP' action='removeuserorgroup' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_send_locator' nlsid='MSG_SEND_LOCATOR' action='sendlocator' showifinvalid='true'/>
<dmf:menuseparator name='file_sep1'/>
<dmf:menuitem name='file_help' nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'/>
<dmfx:actionmenuitem name='file_about' nlsid='MSG_ABOUT' action='about' showifinvalid='true'/>
<dmf:menuseparator name='file_sep2'/>
<dmfx:actionmenuitem name='file_logout' nlsid='MSG_LOGOUT' action='logout' showifinvalid='true'/>
</dmf:menu>
</td>
<td>
<dmf:menu name='edit_menu' nlsid='MSG_EDIT' width='50'>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_addtoclip' nlsid='MSG_ADD_TO_CLIPBOARD' action='addtoclipboard' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='generic' name='tools_viewclipboard' nlsid='MSG_VIEW_CLIP' action='viewclipboard' showifinvalid='true'/>
<dmf:menuseparator name='edit_sep1'/>
<dmfx:actionmenuitem dynamic='generic' name='tools_link' nlsid='MSG_LINK_FILE' action='link' showifinvalid='true'/>
</dmf:menu>
</td>
<td>
<dmf:menu name='view_menu' nlsid='MSG_VIEW' width='50'>
<dmf:menu name='view_properties_menu' nlsid='MSG_PROPERTIES'>
<dmfx:actionmenuitem dynamic='singleselect' name='view_info' nlsid='MSG_INFO' action='attributes' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_permissions' nlsid='MSG_PERMISSIONS' action='permissions' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_history' nlsid='MSG_HISTORY' action='history' showifinvalid='true'/>
</dmf:menu>
<dmf:menuseparator name='view_sep1'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='userrenamelog' nlsid='MSG_USER_RENAME_LOG' action='userrenamelog' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='changehomedblog' nlsid='MSG_USER_CHANGE_HOME_DB_LOG' action='changehomedblog' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='grouprenamelog' nlsid='MSG_GROUP_RENAME_LOG' action='grouprenamelog' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_versions' nlsid='MSG_VERSIONS' action='versions' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_relationships' nlsid='MSG_RELATIONSHIPS' action='relationships' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' id='view_annotate' name='view_annotate' nlsid='MSG_ANNOTATIONS' action='annotate' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_renditions' nlsid='MSG_RENDITIONS' action='renditions' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_locations' nlsid='MSG_LOCATIONS' action='locations' showifinvalid='true'/>
</dmf:menu>
</td>
<td>
<dmf:menu name='document_menu' nlsid='MSG_DOCUMENT' width='50'>
<dmf:menu name='doc_lifecycle' nlsid='MSG_LIFECYCLE'>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_promotelifecycle' nlsid='MSG_PROMOTE_LIFECYCLE' action='promote' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_schedulepromote' nlsid='MSG_SCHEDULE_PROMOTE' action='schedulepromote' showifinvalid='true'/>
</dmf:menu>
<dmf:panel name='doc_vdm_panel'>
<dmf:menu name='doc_vdm' nlsid='MSG_VDM'>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_vdm_tovd' nlsid='MSG_VDM_MAKE_VIRTUAL' action='makevirtual' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_vdm_todoc' nlsid='MSG_VDM_MAKE_SIMPLE' action='makesimple' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_view' nlsid='MSG_VDM_VIEW' action='viewvirtualdoc' showifinvalid='true'/>
<dmf:menu name='doc_vdm_addcomponent' nlsid='MSG_VDM_ADDCOMPONENT'>
<dmfx:actionmenuitem dynamic='generic' name='doc_vdm_addcomponent_from_clipboard' nlsid='MSG_FROM_CLIPBOARD' action='addcomponentfromclipboard' showifinvalid='false' showifdisabled='true'/>
<dmfx:actionmenuitem dynamic='generic' name='doc_vdm_addcomponent_from_selector' nlsid='MSG_FROM_FILE_SELECTOR' action='addcomponentfromfileselector' showifinvalid='false' showifdisabled='true'/>
</dmf:menu>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_vdm_removecomponent' nlsid='MSG_VDM_REMOVECOMPONENT' action='removecomponent' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_reordercomponents' nlsid='MSG_VDMTOOLS_REORDERCOMPONENTS' action='reordercomponents' showifinvalid='true'/>
</dmf:menu>
</dmf:panel>
<dmf:menu name='doc_create_renderition' nlsid='MSG_CREATE_RENDITION'>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_createpdfrendition' nlsid='MSG_CREATE_PDF_RENDITION' action='createpdfrendition' showifinvalid='true' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_createhtmlrendition' nlsid='MSG_CREATE_HTML_RENDITION' action='createhtmlrendition' showifinvalid='true' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_importrendition' nlsid='MSG_IMPORT_RENDITION' action='importrendition' showifinvalid='true' showifdisabled='false'/>
</dmf:menu>
</dmf:menu>
</td>
<td>
<dmf:menu name='tools_menu' nlsid='MSG_TOOLS' width='50'>
<dmfx:actionmenuitem dynamic='multiselect' name='userchangestate' nlsid='MSG_USER_CHANGE_STATE' action='userchangestate' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='userchangehomedocbase' nlsid='MSG_USER_CHANGE_HOME_DOCBASE' action='changehomedocbase' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='reassignuser' nlsid='MSG_REASSIGN_USER' action='reassignuser' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='grouprename' nlsid='MSG_GROUP_REASSIGN' action='groupreassign' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_subscribe' nlsid='MSG_SUBSCRIBE' action='subscribe' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_unsubscribe' nlsid='MSG_UNSUBSCRIBE' action='unsubscribe' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_submitforcategorization' nlsid='MSG_SUBMIT_FOR_CATEGORIZATION' action='submitforcategorization' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect'  name='retentiondate' nlsid='MSG_RETENTION_DATE' action='setretentiondate' showifdisabled = 'false' showifinvalid='false'/>
<dmf:menuseparator name='tools_sep1'/>
<dmf:menu name='tools_workflow' nlsid='MSG_WORKFLOW'>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_startworkflowfromdoc' nlsid='MSG_START_WORKFLOW_FROM_DOC'  action='startworkflowfromdoc' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_abortworkflow' nlsid='MSG_STOP_WORKFLOW' action='abortworkflow' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_haltworkflow' nlsid='MSG_PAUSE_WORKFLOW' action='haltworkflow' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_resumeworkflow' nlsid='MSG_RESUME_WORKFLOW' action='resumeworkflow' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_sendtodistributionlist' nlsid='MSG_WORKFLOW_QUICKFLOW' action='sendtodistributionlist' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='tools_workflowstatus' nlsid='MSG_WORKFLOW_STATUS' action='workflowstatusclassic' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='tools_workflowreportmain' nlsid='MSG_WORKFLOW_REPORTING' action='reportmainclassic' showifinvalid='true'/>
<dmf:menu name='tools_workflow_report_details' nlsid='MSG_WORKFLOW_REPORTING_DETAILS'>
<dmfx:actionmenuitem dynamic='singleselect' name='tools_workflowreportdetailssummary' nlsid='MSG_WORKFLOW_REPORT_DETAILS_SUMMARY' action='reportdetailssummarylist' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='tools_workflowreportdetailsaudit' nlsid='MSG_WORKFLOW_REPORT_DETAILS_AUDIT' action='reportdetailsauditclassic' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='tools_workflowreportdetailsmap' nlsid='MSG_WORKFLOW_REPORT_DETAILS_MAP' action='reportdetailsmap' showifinvalid='true'/>
</dmf:menu>
</dmf:menu>
</dmf:menu>
</td>
<td><dmf:image name='farrightimg' id='farrightimg' src='images/menubar/farright.gif'/></td>
</tr>
</table>
<dmf:panel name='accessfooterpanel'>
<table class='contentBackground' border='0' cellpadding='0' cellspacing='0'>
<tr>
<td width=5>
</td>
<td>
<dmf:button name='cancelButton' cssclass='buttonLink' nlsid='MSG_CANCEL' onclick='onCancel'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmf:button name='helpButton' cssclass="buttonLink" nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_HELP_TIP'/>
</td>
</tr>
</table>
</dmf:panel>
</dmf:menugroup>
</dmf:panel>
</dmf:form>
</body>
</html>
