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

 Filename       $RCSfile: MrcsAttributesBasic.jsp,v $
 Author         $Author: wallam1 $
 Revision       $Revision: 1.4 $
 Modified on    $Date: 2007/03/14 16:04:37 $

***********************************************************************
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<dmf:html>
<dmf:head>
    <dmf:webform />
</dmf:head>
<dmf:body cssclass='contentBackground'>
    <dmf:form>
        <!-- dmf:label nlsid="MRCS_ATTR_DOCNAME_LABEL"/ --><!-- dmf:label name="labeldocumentname"/ --><!-- br -->
        <dmfx:docbaseobject name="obj" />
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
            <tr>
                <td colspan="3" align=right scope="row">
                    <!--dmf:checkbox name='show_all' onclick='onShowAllClicked' nlsid='MSG_SHOW_ALL_PROPERTIES' /-->
                </td>
            </tr>
            <tr>
                <td>
                    <dmfx:docbaseattributelist name="attrlist" object="obj" attrconfigid="mrcs_newdoc_attrlist_config" />
                </td>
            </tr>
        </table>
        <BR>
        <table border="0" cellpadding="2" cellspacing="0" width="100">
	        <tr>

<%-- Modified by Sujeet on 03/02 to fix the Defect 365
	            <td>
			        <dmf:button name="Finish" cssclass="buttonLink" imagefolder="images/dialogbutton" height="16"
			                    onclick="processAttributes" nlsid="MSG_FINISH_BUTTON" tooltipnlsid="MSG_NEXT_TIP"/>

	            </td>
--%>
		    <td>
			        <dmf:button name="Finish" cssclass="buttonLink" imagefolder="images/dialogbutton" height="16"
			                    onclick="processAttributes" nlsid="MSG_FINISH_BUTTON" tooltipnlsid="MSG_SAVE_TIP"/>
		    </td>

<%-- Modified by Sujeet on 03/02 to fix the Defect 365
	            <td>
			        <dmf:button name="Cancel" cssclass="buttonLink" imagefolder="images/dialogbutton" height="16"
	                            onclick="cancelAttributes" nlsid="MSG_CANCEL_BUTTON" tooltipnlsid="MSG_NEXT_TIP"/>
	            </td>
--%>
	            <td>
			        <dmf:button name="Cancel" cssclass="buttonLink" imagefolder="images/dialogbutton" height="16"
	                            onclick="cancelAttributes" nlsid="MSG_CANCEL_BUTTON" tooltipnlsid="MSG_CANCEL_TIP"/>
	            </td>

	        </tr>
        </table>    
    </dmf:form>
</dmf:body>
</dmf:html>
