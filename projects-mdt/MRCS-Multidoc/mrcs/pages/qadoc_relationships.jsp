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
 * Revision       $Revision: 24$
 * Modified on    $Date: 8/12/2005 2:24:24 PM$
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
<%@ page import="com.medtronic.documentum.mrcs.client.QADocRelationships"%>
<dmf:html>
<dmf:head>
    <dmf:webform />
    <script language='JavaScript1.2'
        src='<%=QADocRelationships.makeUrl(request,"/wdk/include/dynamicAction.js")%>'></script>
</dmf:head>
<dmf:body cssclass='contentBackground' marginheight='0' marginwidth='0'
    topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>

    <dmf:form>
            <dmf:panel name='<%=QADocRelationships.STR_VERSIONS_PANEL%>'>
                <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                        <td colspan="3" height="7" class="spacer">
                            &nbsp;
                        </td>
                    </tr>
                    <tr>
                        <td>
                            &nbsp;
                            <dmf:label name='versionLabel' nlsid='MSG_RELATION_TYPES' />
                        </td>
                        <td width="10" class="spacer" nowrap>
                            &nbsp;
                        </td>
                        <td>
                            <dmf:datadropdownlist
                                name='<%=QADocRelationships.STR_VERSIONS_FILTER%>'
                                onselect='onSelectVersionFilter'
                                tooltipnlsid='MSG_SELECTED_VERSION'>
                                <dmf:dataoptionlist>
                                    <dmf:option datafield="r_object_id"
                                        labeldatafield="relation_name" />
                                </dmf:dataoptionlist>
                            </dmf:datadropdownlist>
                        </td>
                     </tr>
                     <tr>
                        <td>
                            &nbsp;
                            <dmf:label name='adddocLabel' nlsid='MSG_ADDDOC_LABEL'/>
                        </td>
                        <td width="10" class="spacer" nowrap>
                            &nbsp;
                        </td>
                        <td>
                            <dmfx:actionimage name='addWfAttachmentImg' nlsid='MSG_ADD_ATTACHMENT' action='addwfattachment' oncomplete='onComplete' src='icons/add.gif' showifdisabled='false'/>
                            <dmf:label name='selectedDocName'/>
                            <dmf:button name='addRelation' nlsid='MSG_ADD_RELATION_BUTTON' onclick='onCompleteAddDocument'/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3" height="7" class="spacer">
                            &nbsp;
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3" height="7" class="spacer">
                            &nbsp;
                        </td>
                    </tr>
                    </dmf:panel>
                </table>

                <%@ include file='/wdk/fragments/modal/modalDatagridContainerStart.jsp'%>
                <!-- action multi-select wrapper -->
                <dmfx:actionmultiselect name='multi'>
                    <dmf:datagrid name='<%=QADocRelationships.GRID_NAME%>' paged='true' preservesort='false' cssclass='contentBackground' cellspacing='0' cellpadding='0' bordersize='0'>
                        <tr class='pagerBackground'>
                            <td colspan=20 align=center height=30 valign="middle" width="450">
                                <table width="100%" border=0 cellspacing=5 cellpadding=0>
                                    <tr valign="top">
                                        <td>
                                            &nbsp;
                                        </td>
                                        <td>
                                            &nbsp;
                                        </td>
                                        <td>
                                            &nbsp;
                                        </td>
                                        <td width="10" class="spacer">
                                            &nbsp;
                                        </td>
                                        <td align=center width=100%>
                                            <dmf:datapaging name='pager1' gotopageclass='doclistPager' />
                                        </td>
                                        <td width="10" class="spacer">
                                            &nbsp;
                                        </td>
                                        <td align=right nowrap valign="top">
                                            <dmf:label nlsid='MSG_SHOW_ITEMS' />
                                            &nbsp;
                                        </td>
                                        <td valign="middle" nowrap>
                                            <dmf:datapagesize name='sizer'
                                                preference='application.display.classic'
                                                tooltipnlsid='MSG_SHOW_ITEMS' />
                                            &nbsp;
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="20" class="spacer contentBackground">
                                &nbsp;
                            </td>
                        </tr>

                        <tr class="colHeaderBackground">
                            <dmf:columnpanel columnname='relationPanel'>
                                <th scope='col' align='left' class="doclistfilenamedatagrid">
                                    <dmf:datasortlink name='sort0' label="Relation Name"
                                        column='relation_name' cssclass='doclistbodyDatasortlink' />
                                </th>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='parentchildPanel'>
                                <th scope='col' align='left' class="doclistfilenamedatagrid">
                                    <dmf:datasortlink name='sort1' label="Parent/Child"
                                        column='reltype' cssclass='doclistbodyDatasortlink' />
                                </th>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='docnamePanel'>
                                <th scope='col' align='left' class="doclistfilenamedatagrid">
                                    <dmf:datasortlink name='sort1' label="Doc Name"
                                        column='docname' cssclass='doclistbodyDatasortlink' />
                                </th>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='linkPanel'>
                                <th scope='col' align='left' class="doclistfilenamedatagrid">
                                    <dmf:datasortlink name='sort1' label="Relation Link"
                                        column='rellink' cssclass='doclistbodyDatasortlink' />
                                </th>
                            </dmf:columnpanel>
                        </tr>

                        <dmf:datagridRow>
                            <dmf:columnpanel columnname='relation_name'>
                                <td nowrap class="doclistfilenamedatagrid">
                                    <dmf:label datafield='relation_name' />
                                </td>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='reltype'>
                                <td nowrap class="doclistfilenamedatagrid">
                                    <dmf:label datafield='reltype' />                                
                                </td>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='docname'>
                                <td nowrap class="doclistfilenamedatagrid">
                                    <dmf:label datafield='docname' />
                                </td>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='rellink'>
                                <td nowrap class="doclistfilenamedatagrid">
                                    <dmf:label datafield='rellink' />
                                </td>
                            </dmf:columnpanel>
                        </dmf:datagridRow>
                        

                    </dmf:datagrid>


                </dmfx:actionmultiselect>
                <%@ include
                    file='/wdk/fragments/modal/modalDatagridContainerEnd.jsp'%>


</dmf:form>
</dmf:body>
</dmf:html>