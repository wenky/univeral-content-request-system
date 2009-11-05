<%--
 
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
 * Project        Component Library
 * Module         Relationships component
 * File           relationships.jsp
 *
 * Description    Displays the audit trail for a sysobject. This component is 
 *                contained within the Properties container in Web Component 
 *                Library. 
 * Created on     13 March 2002
 * Tab width      3
 *
 *****************************************************************************
 *
 * VCS Maintained Data
 *
 * Revision       $Revision: 1.3 $
 * Modified on    $Date: 2008/07/08 18:21:17 $
 *
 * Log at EOF
 *
 *****************************************************************************                          
 --%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<%@ taglib uri="/WEB-INF/tlds/dmda_1_0.tld" prefix="dmda"%>
<%@ page import="com.documentum.web.form.control.databound.DataProvider"%>
<%@ page import="com.documentum.web.form.control.databound.Datagrid"%>
<%@ page import="com.medtronic.ecm.documentum.core.webtop.MdtRelationships"%>
<dmf:html>
<dmf:head>
    <dmf:webform />
    <script language='JavaScript1.2'
        src='<%=MdtRelationships.makeUrl(request,"/wdk/include/dynamicAction.js")%>'></script>
</dmf:head>
<dmf:body cssclass='contentBackground' marginheight='0' marginwidth='0'
    topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>

    <dmf:form>
            <dmf:panel name='<%=MdtRelationships.STR_VERSIONS_PANEL%>'>
                <table cellpadding="0" cellspacing="0" border="0">
                     <tr>
                        <td>
                            &nbsp;
                           
                        </td>
                        <td width="10" class="spacer" nowrap>
                            &nbsp;
                        </td>
                        <td>
                            <dmf:button name='addRelation' nlsid='MSG_ADD_RELATION_BUTTON' onclick='onCompleteAddDocument'/>
                        </td>
                    </tr>
                </dmf:panel>
                </table>
 				<dmfx:componentinclude component="relationships" name="relationshipscomp"/>

</dmf:form>
</dmf:body>
</dmf:html>