
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>

<dmf:html>
  <dmf:head>
    <dmf:webform />
  </dmf:head>
  <dmf:body id="modalLocator" topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" showdialogevent='true' width='900' height='500'>
    <dmf:paneset name="mdtattachmentselectorpaneset" cssclass="contentBackground" toppadding="10" bottompadding="10" rightpadding="10" minheight="0" minwidth="0" leftpadding="10">
      <table>
        <tr>
          <td class="leftAlignment" valign='middle'>
            <dmf:panel name="mdtlocatorpanel">
            </dmf:panel>
          </td>
          <td class="rightAlignment" valign='middle'>
            <dmf:panel name="mdtattachementselections">
            </dmf:panel>
          </td>
        </tr>
        <tr>
          <td class="rightAlignment" colspan="2">
            <!-- control buttons -->
          </td>
        </tr>
      </table>
    </dmf:paneset>
  </dmf:body>
</dmf:html>
  