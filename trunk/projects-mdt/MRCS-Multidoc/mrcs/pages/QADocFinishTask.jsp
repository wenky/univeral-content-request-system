<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<%@ page import="com.documentum.web.form.Form"%>
<%@ page import="com.documentum.webcomponent.library.workflow.taskmanager.FinishWorkflowTask"%>
<%@ page import="com.medtronic.documentum.mrcs.client.QADocFinishWFT"%>
<dmf:webform />
<dmf:form>
    <table width='100%'>
        <tr>
            <td style='padding-left:10px'>
                <table border="0" cellpadding="2" cellspacing="0" width="100%">
                    <tr>
                        <td colspan='2' class='taskmgrSectionHeading' height='24'>
                            <dmf:label nlsid='MSG_FINISH_ACTION_HDR' />
                        </td>
                    </tr>
                    <tr>
                        <td height='10'>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan='2'>
                            <dmf:label nlsid='MSG_FINISH_TASK_INFO' />
                        </td>
                    </tr>
                    <tr>
                        <td height='10'>
                            &nbsp;
                        </td>
                    </tr>
                    <dmf:panel name='<%=FinishWorkflowTask.SIGNOFF_PANEL_CONTROL_NAME%>'>
                        <tr>
                            <td colspan='2' height='24' class='taskmgrSectionHeading' style='padding-top: 10px; padding-bottom: 5px'>
                                <dmf:label nlsid='MSG_SIGNOFF_REQUIRED_HDR' />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <!--  part 11 compliant signoff fields -->
                                <dmf:label nlsid='MSG_USERNAME_FIELD_LABEL' />&nbsp;&nbsp;<dmf:text name='<%=QADocFinishWFT.USERNAME_CONTROL_NAME%>'             defaultonenter='true'  tooltipnlsid='MSG_PASSWORD_FIELD_LABEL' /><br>
                                <dmf:label nlsid='MSG_PASSWORD_FIELD_LABEL' />&nbsp;&nbsp;<dmf:password name='<%=QADocFinishWFT.PASSWORD_CONTROL_NAME%>'         defaultonenter='false' tooltipnlsid='MSG_PASSWORD_FIELD_LABEL' /><br>
                                <dmf:label nlsid='MSG_REASON_FIELD_LABEL'   />&nbsp;&nbsp;<dmf:dropdownlist name='<%=QADocFinishWFT.REASONSELECT_CONTROL_NAME%>' tooltipnlsid='MSG_REASON_FIELD_LABEL'   /><br>
                                <dmf:password name='<%=FinishWorkflowTask.PASSWORD_CONTROL_NAME%>' visible='false' defaultonenter='false' tooltipnlsid='MSG_PASSWORD_FIELD_LABEL' />
                                <%-- invisible button used here as a proxy for handler invocation--%>
                                <dmf:button label='default' visible='false' enabled='true' default='true' onclick='onAttemptFinish' />
                            </td>
                        </tr>
                        <tr>
                            <td height='10'>
                                &nbsp;
                            </td>
                        </tr>
                    </dmf:panel>
                </table>
            </td>
        </tr>
    </table>
</dmf:form>
