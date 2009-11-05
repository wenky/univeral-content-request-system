<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<%@ taglib uri="/WEB-INF/tlds/dmda_1_0.tld" prefix="dmda"%>
<%@ page import="com.documentum.web.form.control.databound.DataProvider"%>
<%@ page import="com.documentum.web.form.control.databound.Datagrid"%>
<%@ page import="com.medtronic.documentum.mrcs.client.QADocApprovalComponent"%>

<dmf:html>
	<dmf:head>
		<dmf:webform/>
	</dmf:head>
	<dmf:body cssclass='contentBackground' marginheight='0' marginwidth='0' topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>
		<dmf:form>
        
            <BR><b>Approvers and Signature Reasons:</b>
			<dmf:datagrid name='<%= QADocApprovalComponent.SIGNATURES_GRID %>' paged='false' preservesort='false' cssclass='contentBackground' cellspacing='0' cellpadding='0' bordersize='0'>
			<table cellpadding="0" cellspacing="0" border="0" width="300">
			<tr><td colspan="5" height="7" class="spacer">&nbsp;</td></tr>
			<tr><td colspan="5">&nbsp;</td></tr>
			<tr><td colspan="5" height="1" class="infotableborder">&nbsp;</td></tr>
			<tr>
				<td width="1" class="infotableborder">&nbsp;</td>
				<td align="center" colspan="3">
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
						<tr class="colHeaderBackground">
							<dmf:columnpanel columnname='user_name'>
								<td nowrap class="doclistfilenamedatagrid"><nobr><dmf:datasortlink name='user_name' nlsid='MSG_USER_NAME' column='user_name' /></nobr></td>
							</dmf:columnpanel>
							<dmf:columnpanel columnname='string_1'>
								<td nowrap class="doclistfilenamedatagrid"><nobr><dmf:datasortlink name='string_1' nlsid='MSG_SIGNER' column='string_1' /></nobr></td>
							</dmf:columnpanel>
                            <dmf:columnpanel columnname='string_2'>
                                <td nowrap class="doclistfilenamedatagrid"><nobr><dmf:datasortlink name='string_2' nlsid='MSG_REASON' column='string_2' /></nobr></td>
                            </dmf:columnpanel>
						</tr>
						<dmf:datagridRow>
							<dmf:columnpanel columnname='user_name'>
								<td nowrap class="doclistfilenamedatagrid"><dmf:label datafield='user_name'/></td>
							</dmf:columnpanel>
							<dmf:columnpanel columnname='string_1'>
								<td nowrap class="doclistfilenamedatagrid"><dmf:label datafield='string_1'/></td>
							</dmf:columnpanel>
                            <dmf:columnpanel columnname='string_2'>
                                <td nowrap class="doclistfilenamedatagrid"><dmf:label datafield='string_2'/></td>
                            </dmf:columnpanel>
						</dmf:datagridRow>
						<dmf:nodataRow>
						<tr>
							<td colspan="10" class="signingreasonsdatagrid" align="left">
								<dmf:label nlsid='MSG_EMPTY'/>
							</td>
						</tr>
						</dmf:nodataRow>
					</table>
				</td>
                <td width="1" class="infotableborder">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="5" height="1" class="infotableborder">&nbsp;</td>
            </tr>
            </dmf:datagrid>

            <BR><b>Other Documents in the Approval Group:</b>
            <dmf:datagrid name='<%= QADocApprovalComponent.GRID_NAME %>' paged='false' preservesort='false' cssclass='contentBackground' cellspacing='0' cellpadding='0' bordersize='0'>
            <table cellpadding="0" cellspacing="0" border="0" width="300">
            <tr><td colspan="5" height="7" class="spacer">&nbsp;</td></tr>
            <tr><td colspan="5">&nbsp;</td></tr>
            <tr><td colspan="5" height="1" class="infotableborder">&nbsp;</td></tr>
            <tr>
                <td width="1" class="infotableborder">&nbsp;</td>
                <td align="center" colspan="3">
                    <table cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr class="colHeaderBackground">
                            <dmf:columnpanel columnname='r_object_id'>
                                <td nowrap class="doclistfilenamedatagrid"><nobr><dmf:datasortlink name='r_object_id' nlsid='MSG_OBJECT_LINK' column='r_object_id' /></nobr></td>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='object_name'>
                                <td nowrap class="doclistfilenamedatagrid"><nobr><dmf:datasortlink name='object_name' nlsid='MSG_OBJECT_NAME' column='object_name' /></nobr></td>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='r_version_label'>
                                <td nowrap class="doclistfilenamedatagrid"><nobr><dmf:datasortlink name='r_version_label' nlsid='MSG_VERSION' column='r_version_label' /></nobr></td>
                            </dmf:columnpanel>
                        </tr>
                        <dmf:datagridRow>
                            <dmf:columnpanel columnname='r_object_id'>
                                <td nowrap class="doclistfilenamedatagrid"><dmf:label datafield='r_object_id'/></td>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='object_name'>
                                <td nowrap class="doclistfilenamedatagrid"><dmf:label datafield='object_name'/></td>
                            </dmf:columnpanel>
                            <dmf:columnpanel columnname='r_version_label'>
                                <td nowrap class="doclistfilenamedatagrid"><dmf:label datafield='r_version_label'/></td>
                            </dmf:columnpanel>
                        </dmf:datagridRow>
                        <dmf:nodataRow>
                        <tr>
                            <td colspan="10" class="doclistfilenamedatagrid" align="left">
                                <dmf:label nlsid='MSG_EMPTY'/>
                            </td>
                        </tr>
                        </dmf:nodataRow>
                    </table>
                </td>
                <td width="1" class="infotableborder">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="5" height="1" class="infotableborder">&nbsp;</td>
            </tr>
            </dmf:datagrid>

</dmf:form>
</dmf:body>
</dmf:html>

