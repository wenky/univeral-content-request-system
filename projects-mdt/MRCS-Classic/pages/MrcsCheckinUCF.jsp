<%--
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
<script type="text/javascript">
function onRetainLockClick(chk)
{
__orCheck(chk, document.getElementById("checkinfromfile"), document.getElementById("keeplocalfile"));
}
function onCheckinFromFileClick(chk)
{
__orCheck(chk, document.getElementById("retainlock"), document.getElementById("keeplocalfile"));
}
function __orCheck(chk1, chk2, target)
{
if (target != null)
{
var chk2Checked = (chk2 != null) ? chk2.checked : false;
with (target)
{
disabled = chk1.checked || chk2Checked;
checked = chk1.checked || chk2Checked;
}
}
}
function onSelectFile()
{
document.getElementById("checkinfromfile").checked = true;
onCheckinFromFileClick(document.getElementById("checkinfromfile"));
}
</script>
</dmf:head>
<dmf:body cssclass='contentBackground'
showdialogevent='true' id='save' titlenlsid='MSG_CAPTION'
width='550' height='470'>
<dmf:form>
<dmfx:docbaseobject name="object" modifyonversion="true" />
<%--     Icon, object info --%>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td valign="top"><dmfx:docbaseicon size='32' name="obj_icon"/></td>
<td width="10" class="spacer">&nbsp;</td>
<td scope="row" valign="top">
<dmfx:docbaseattributevalue object="object" name="attribute_object_name" attribute="object_name" required="true" size="57"/><br>
<dmf:label nlsid="MSG_VERSION"/>:&nbsp;<dmfx:docbaseattributevalue object="object" attribute="r_version_label" readonly="true"/><br>
<dmfx:docbaseattribute object="object" attribute="r_object_type" readonly="true" col1=":&nbsp;"/><br>
<dmfx:docbaseattribute object="object" attribute="a_content_type" readonly="true" col1=":&nbsp;"/><br>
</td>
</tr>
</table>
<br>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td></td>
<td></td>
<td>
<dmf:label name="checkinFromFileErrorMsg" nlsid="MSG_CHECKIN_FROM_FILE_NEEDED"  cssclass="validatorMessageStyle"/>
</td>
</tr>
<tr>
<td></td>
<td></td>
<td>
<dmf:label name='sourcenotcheckedoutmsg' cssclass="validatorMessageStyle"/>
</td>
</tr>
</table>
<br>
<dmf:panel name="standardoptions">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<%--      Version --%>
<dmf:panel name="existingobjversion">
<tr>
<td scope="row" align=right valign="middle" nowrap class="fieldlabel">
<dmf:label nlsid="MSG_SAVE_AS"/>
</td>
<td class="defaultcolumnspacer" valign="middle">:&nbsp;</td>
<td valign="top">
<dmf:radio name="sameversion" group="group1" tooltipnlsid="MSG_SAME_VERSION"/>
<span class="defaultcolumnspacer"></span><dmf:label name="sameversionnum"/>
<br>
<dmf:radio name="minorversion" group="group1" tooltipnlsid="MSG_MINOR_VERSION"/>
<span class="defaultcolumnspacer"></span><dmf:label name="minorversionnum"/>
<br>
<dmf:radio name="majorversion" group="group1" tooltipnlsid="MSG_MAJOR_VERSION"/>
<span class="defaultcolumnspacer"></span><dmf:label name="majorversionnum"/>
</td>
</tr>
</dmf:panel>
<dmf:panel name="newobjversion">
<tr>
<td scope="row" align=right nowrap valign="middle" class="fieldlabel">
<dmf:label nlsid="MSG_SAVE_AS"/>
</td>
<td align="left" class="defaultcolumnspacer" valign="middle">:&nbsp;</td>
<td valign="middle">
<dmf:radio name="newversion" group="group2" tooltipnlsid="MSG_NEW_VERSION" />&nbsp;
<dmf:label name="newversionnum"/>
</td>
</tr>
</dmf:panel>
<dmf:panel name="branchversion">
<tr>
<td scope="row" align=right valign="middle" nowrap class="fieldlabel">
<dmf:label nlsid="MSG_SAVE_AS"/>
</td>
<td class="defaultcolumnspacer" align="left" valign="middle">:&nbsp;</td>
<td valign="top">
<dmf:radio name="majorversion" group="group3" tooltipnlsid="MSG_MAJOR_VERSION"/>
<span class="defaultcolumnspacer"></span><dmf:label name="majorversionnum"/>
<br>
<dmf:radio name="branchrevision" group="group3" tooltipnlsid="MSG_BRANCH_REVISION" />
<span class="defaultcolumnspacer"></span><dmf:label name="branchrevisionnum"/>
</td>
</tr>
</dmf:panel>
<%--         Version label --%>
<!--  tr -->
<!--  td scope="row" valign=middle align=right class="fieldlabel" -->
<!--  dmf:label nlsid="MSG_VERSION_LABEL"/ -->
<!--  /td -->
<!--  td class="defaultcolumnspacer" valign="middle" align="left">:&nbsp;</td -->
<!--  td valign="middle" -->
<!--  dmf:text name="symbolicVersionLabel" size="57" tooltipnlsid="MSG_VERSION_LABEL"/ -->
<!--  br --><!--  dmf:utf8stringlengthvalidator name="version_validator" controltovalidate="symbolicVersionLabel" maxbytelength="32" nlsid="MSG_VERSION_LABEL_TOO_LONG"/ -->
<!--  dmfx:symbolicversionlabelvalidator name="symbolicversionlabel_validator" controltovalidate="symbolicVersionLabel"/ -->
<!--  dmf:requiredfieldvalidator name="versionLabelRequireValidator" controltovalidate="symbolicVersionLabel" nlsid="MSG_VERSION_LABEL_NEEDED" / -->
<!--  /td -->
<!--  /tr -->
<%--        Description  --%>
<tr>
<td scope="row" align=right valign="middle" class="fieldlabel">
<dmf:label nlsid="MSG_DESCRIPTION"/>
</td>
<td class="defaultcolumnspacer" align="left">:</td>
<td>
<dmfx:docbaseattributevalue object="object" name="attr_value_description" attribute="log_entry" readonly="false" lines="1" size="57"/>
</td>
</tr>
<%--      Format selection  --%>
<tr>
<td scope="row" align="right" valign="middle" class="fieldlabel">
<dmf:label nlsid="MSG_FORMAT"/>
</td>
<td align="left" class="defaultcolumnspacer" valign="middle">:&nbsp;</td>
<td align="left" valign="middle" >
<dmf:datadropdownlist name="formatlist"  tooltipnlsid="MSG_FORMAT">
<dmf:dataoptionlist>
<dmf:option datafield="name" labeldatafield="description"/>
</dmf:dataoptionlist>
</dmf:datadropdownlist>
</td>
</tr>
<tr>
<td scope="row" align="right" valign="middle" class="fieldlabel">&nbsp;
</td>
<td align="left" class="defaultcolumnspacer" valign="middle">&nbsp;</td>
<td align="left" valign="middle" >
<dmf:label name='unknown_format_info_label' nlsid='MSG_ENFORCE_SELECT_FORMAT' cssclass="validatorMessageStyle" />
</td>
</tr>
<dmfx:docbaseattributelist name="attrlist" object="object" attrconfigid="checkin" showcategorynames="false" pre="<tr><td align=\"right\" class=\"fieldlabel\">" col1="</td><td align=\"left\" class=\"defaultcolumnspacer\" valign=\"middle\">:&nbsp;</td><td>"/>
<%--    full text option for superusers  --%>
<dmf:panel name="fulltext">
<tr>
<td scope="row" align="right" nowrap class="fieldlabel">
<dmfx:docbaseattributelabel object="object" attribute="a_full_text"/>
</td>
<td align="left" class="defaultcolumnspacer" valign="middle">:&nbsp;</td>
<td align="left">
<dmfx:docbaseattributevalue object="object" attribute="a_full_text"/>
</td>
</tr>
</dmf:panel>
</table>
</dmf:panel>
<dmf:panel name="additionaloptions">
<br>
<table border="0" cellpadding="2" cellspacing="0" width="100%">
<%--     Options  --%>
<tr>
<td></td>
<td scope="row" colspan="2" nowrap>
<dmf:link name="showhideoptions" onclick="onClickShowHideOptions"/>
</td>
</tr>
<dmf:panel name="optionspanel">
<dmfx:clientenvpanel environment="appintg">
<dmf:fireclientevent event='aiEvent' includeargname='true'>
<dmf:argument name='event'       value='ShowDialog'/>
<dmf:argument name='id'          value='save'/>
<dmf:argument name='width'       value='550'/>
<dmf:argument name='height'      value='550'/>
<dmf:argument name='sizepreference' value='false'/>
<dmf:argument name='title'       nlsid='MSG_CAPTION'/>
</dmf:fireclientevent>
</dmfx:clientenvpanel>
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="retainlock" id="retainlock" nlsid="MSG_RETAIN_LOCK" onclick="onRetainLockClick" runatclient="true"/>
</td>
</tr>
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="makecurrent" nlsid="MSG_MAKE_CURRENT" onclick="onClickMakeCurrent"/>
</td>
</tr>
<dmfx:clientenvpanel environment="appintg" reversevisible='true'>
<dmf:panel name="keeplocalfilepanel">
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="keeplocalfile" id="keeplocalfile" nlsid="MSG_KEEP_LOCAL_FILE"/>
</td>
</tr>
</dmf:panel>
</dmfx:clientenvpanel>
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="subscribe" nlsid="MSG_SUBSCRIBE_TO_FILE"/>
</td>
</tr>
<dmf:panel name="vdmoptions">
<tr>
<td></td>
<td></td>
<td scope="row">
<dmf:checkbox name="checkindescendents" nlsid="MSG_CHECKIN_DESCENDENTS"/>
</td>
</tr>
</dmf:panel>
<dmfx:clientenvpanel environment="appintg" reversevisible='true'>
<tr>
<td></td>
<td></td>
<td>
<table cellpadding="0" cellspacing="0" border="0">
<tr>
<td class="nowrap" scope="row">
<dmf:checkbox name="checkinfromfile" id="checkinfromfile" nlsid="MSG_CHECKIN_FROM_FILE"
onclick="onCheckinFromFileClick" runatclient="true"/>&nbsp;<dmf:label name="filebrowselabel"/>
</td>
<td>
<dmf:filebrowse name="filebrowse" cssclass="defaultFilebrowseTextStyle" size="50"
onselect="onSelectFile" runatclient="true"/>
</td>
</tr>
<tr>
<td>&nbsp;</td>
<td>
<dmf:requiredfieldvalidator name="validatefilebrowse" controltovalidate="filebrowse"
nlsid="MSG_FILENAME_REQUIRED"/><br>
<dmf:absolutefilepathvalidator name="absolutefilepathvalidator"
controltovalidate="filebrowse"/>
</td>
</tr>
</table>
</td>
</tr>
</dmfx:clientenvpanel>
</dmf:panel>
</table>
</dmf:panel>
</dmf:form>
</dmf:body>
</dmf:html>
