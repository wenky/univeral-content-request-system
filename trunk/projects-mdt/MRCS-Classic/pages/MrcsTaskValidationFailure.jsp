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

 Filename       $RCSfile: MrcsTaskValidationFailure.jsp,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/01/12 22:02:40 $

***********************************************************************
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<dmf:html>
    <dmf:head>
        <dmf:webform validation='false' />
    </dmf:head>
    <dmf:body cssclass='contentBackground'>
        <dmf:form keepfresh='true'>
            The selected workflow task cannot be opened. Validation of the task preconditions by MRCS failed.<BR><BR>
            Check that all requirements have been fulfilled, renditions generated, or that the previous task completed properly.<BR><BR>
            <dmf:button name="Return" onclick="cancelValidation" nlsid="MSG_BUTTON_FINISH" 
                        cssclass='buttonLink' height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_FINISH_TIP'/>     
        </dmf:form>
    </dmf:body>
</dmf:html>
