
<!--
	 *****************************************************************************
	 *
	 * Project        RGDS
	 * Module         Products
	 * File           products.jsp
	 * Description    Products Component Implementation
	 * Created on     June 13 2007
	 *
	 *****************************************************************************
-->
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>

<%@ page import="com.documentum.web.common.ArgumentList" %>
<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="com.documentum.web.form.control.Link" %>
<%@ page import="com.documentum.web.form.control.databound.Datagrid" %>
<%@ page import="com.documentum.web.form.control.databound.DataDropDownList" %>
<%@ page import="com.documentum.web.form.control.databound.DataListBox" %>
<%@ page import="com.documentum.web.samples.DataboundExample" %>


<dmf:html>
<dmf:head>
	<!--------------------------------------------------------------------->
	<!-- Project   RGDS                                                  -->
	<!-- Module    products                                              -->
	<!-- File      products.jsp                                          -->
	<!--------------------------------------------------------------------->

	<dmf:webform validation='false' />
</dmf:head>
	&nbsp;
	<dmf:body topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" cssclass='contentBackground' id='productchange' showdialogevent='true'
	width='900' height='490'>

	<dmf:form>
      <dmf:datagrid name='docgrid1' cellspacing='1' cellpadding='2' bordersize='0' width='700' paged='true' pagesize='10' sortcolumn='object_name'>
         
         <tr class='databoundExampleRow' valign='top'>
             <td align='center' class='databoundExampleRowFont' width='225'>
                 <b><dmf:datasortlink name='sortcol1' label='Object Name' column='object_name' mode='caseinstext'/></b>
             </td>
             <td align='center' class='databoundExampleRowFont' width='150'>
                 <b><dmf:datasortlink name='sortcol2' label='Object Type' column='r_object_type' mode='text'/></b>
             </td>
             <td align='center' class='databoundExampleRowFont' width='150'>
                 <b><dmf:datasortlink name='sortcol3' label='Owner Name' column='owner_name' mode='text'/></b>
             </td>
             <td align='center' class='databoundExampleRowFont' width='150'>
                 <b><dmf:datasortlink name='sortcol4' label='Effective Date' column='r_creation_date' mode='numeric'/></b>
             </td>             
         </tr>
         
         <!-- no data row -->
         <dmf:nodataRow cssclass='databoundExampleDatagridRow'>
             <td colspan='4'>
                 No documents found.
             </td>
         </dmf:nodataRow>
         
         <dmf:datagridRow>
             <td>
                 <dmf:link name="lnk" onclick="onClickLink" datafield='object_name'/>
             </td>
             <td>
                 <dmf:label datafield='r_object_type'/>
             </td>
             <td>
                 <dmf:label datafield='owner_name'/>
             </td>
             <td>
             	<dmf:datevalueformatter type='short'>					
                    <!-- dmf:dateinput name="setdate"> dmf:label datafield='r_creation_date'/</dmf:dateinput -->
                    <dmf:dateinput name="setdate" datafield='r_creation_date'/>
		</dmf:datevalueformatter>
             </td>
         </dmf:datagridRow>
         
         <!-- footer containing paging controls -->
         <tr class='databoundExampleRow' height='24'>
            <td colspan='10'>
                <dmf:datapaging cssclass='databoundExampleDataPaging' name='pager1'/>
            </td>
         </tr>

      </dmf:datagrid>
      						
	</dmf:form>

</dmf:body>
</dmf:html>

