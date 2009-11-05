
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<%@ page import="com.documentum.web.form.control.databound.DataProvider"%>
<%@ page import="com.documentum.web.form.control.databound.Datagrid"%>
<%@ page import="com.medtronic.ecm.documentum.mdtworkflow.webtop.MdtFormSupportingDocumentsSelection"%>
<%@ page import="com.documentum.web.form.Form"%>
<dmf:html>
<dmf:head>
  <dmf:webform />
  <script language='JavaScript1.2' src='<%=Form.makeUrl(request,"/wdk/include/dynamicAction.js")%>'></script>
  <script language='JavaScript1.2'>
    function onCEMSelectObject(event)
    {
      //alert("hello");
      var objectids = null;
      if (event.count == 1) {
        var args = event.datagrid.data.getItemActionArgs(event.startIndex, event.type);
        // process args
        // how do we make a server call?
        objectids = args[0];
      } else {
        for (var i=0; i < event.count; i++) {
          var args = event.datagrid.data.getItemActionArgs(event.startIndex+i,event.type);
          // process args
          if (objectids == null) 
            objectids = args[0];
          else
            objectids += ","+args[0];
        }
      }
      postServerEvent(null, null, null, "removeAttachment","objectIds", objectids);                    
    }
    
    function onSelectObjectSample(event)
    {
      var okbtn = document.getElementById("okbtn");
      var args = grid.data.getItemActionArgs(grid.getFocusIndex(), event.type);
      var addObjIds = document.getElementById("addObjIds");
    }
    

    function onClickOk(obj, id, type, isFolder)
    {
      var okbtn = document.getElementById("okbtn");
      if(okbtn != null && okbtn.disabled) {return;}
      fireAppIntgEvent("event=HideDialog", "busyCursor=true");
      if(isSaveAsDialog)
      {
        postServerEvent(null, null, null, "onClickOk","objectId", selectedObjId, "type", selectedObjType);
      } else {
        postServerEvent(null, null, null, "onClickObject","objectId", selectedObjId, "type", selectedObjType);
      }
    }
    
  </script>   
</dmf:head>
<dmf:body cssclass='contentBackground' marginheight='0' marginwidth='0' topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>
  <dmf:form>
  
    <table cellpadding="0" cellspacing="0" border="0">
        <tr>
            <td colspan="3" height="7" class="spacer">
                &nbsp;
            </td>
        </tr>

         <tr>
            <td>
                &nbsp;
                <dmf:button name='adddoc' nlsid='MSG_ADD' onclick='addAttachment' visible='false'/>
            </td>
            <td width="10" class="spacer" nowrap>
                &nbsp;
            </td>
            <td>
                <b>SELECTED ATTACHMENTS:</b> <BR>
                <dmf:label name="mdtselecteddocs"/>
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
    </table>
    <hr>
    <dmf:button name='attachok' nlsid='MSG_OK' onclick='onAttachmentOk'/> &nbsp;&nbsp;&nbsp;&nbsp;
    <dmf:button name='attachcancel' nlsid='MSG_CANCEL' onclick='onAttachmentCancel'/>
    <hr>
    [click on a row to add the document, or if its in the list already, click on the row again it to remove it]<br>
    <dmf:panel name='thepanel'>
    <dmfx:actionmultiselect name='multiselect'>
      <dmfx:argument name='objectId' contextvalue='objectId' />
      <dmfx:argument name='type' contextvalue='type' />
      <dmfx:argument name='folderId' contextvalue='folderId' />
      <dmf:datagrid name='<%=MdtFormSupportingDocumentsSelection.GRID_NAME%>' paged='true' preservesort='false' cssclass='contentBackground' width='100%' cellspacing='0' cellpadding='0' bordersize='0' fixedheaders="true" focus="true">
        <%
            MdtFormSupportingDocumentsSelection form = (MdtFormSupportingDocumentsSelection) pageContext.getAttribute(Form.FORM,PageContext.REQUEST_SCOPE);
            DataProvider dataProvider = ((Datagrid) form.getControl(MdtFormSupportingDocumentsSelection.GRID_NAME,Datagrid.class)).getDataProvider();
        %>
        <%-- header row with paging controls --%>
        <tr class='pagerBackground'>
          <td colspan=20 align=center height=24>
            <table width=100% border=0 cellspacing=0 cellpadding=0>
              <tr>
                <td align=center width=100%>
                  <dmf:datapaging name='pager1' gotopageclass='doclistPager' />
                </td>
                <td class="rightAlignment" nowrap>
                  <dmf:label nlsid='MSG_SHOW_ITEMS' />
                  &nbsp;
                </td>
                <td valign="middle" nowrap>
                  <dmf:datapagesize name='sizer' preference='application.display.classic' tooltipnlsid='MSG_SHOW_ITEMS' />
                  &nbsp;
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td height="1" class="spacer" colspan="20">
            &nbsp;
          </td>
        </tr>
        <%-- header row with column titles --%>
        <tr class="colHeaderBackground">
          <dmf:datagridTh nowrap="true" scope='col'>
          </dmf:datagridTh>
          <%-- object_name is an attribute always displayed  --%>
          <dmf:datagridTh scope='col'>&nbsp;</dmf:datagridTh>
          <dmf:datagridTh nowrap="true" scope='col' cssclass='leftAlignment doclistfilenamedatagrid objectlistheaderspacing' valign='top' resizable="true">
            <dmf:datasortlink name='object_name' nlsid='MSG_OBJECT_NAME' column='object_name' cssclass='doclistbodyDatasortlink' />
          </dmf:datagridTh>
          <dmf:columnpanel columnname='title'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='leftAlignment doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='title' nlsid='MSG_DOCTITLE' column='owner_name' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>         
          <dmf:columnpanel columnname='owner_name'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='leftAlignment doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='owner_name' nlsid='MSG_OWNER_NAME' column='owner_name' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='group_name'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:datasortlink name='group_name' nlsid='MSG_GROUP_NAME' column='group_name' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_creator_name'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:datasortlink name='r_creator_name' nlsid='MSG_CREATOR_NAME' column='r_creator_name' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_lock_owner'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:datasortlink name='r_lock_owner' nlsid='MSG_LOCK_OWNER' column='r_lock_owner' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_object_type'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:datasortlink name='r_object_type' nlsid='MSG_OBJECT_TYPE' column='r_object_type' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_content_size'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='leftAlignment doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='size' nlsid='MSG_SIZE' column='r_content_size' mode='numeric' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='a_content_type'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='content_type' nlsid='MSG_FORMAT' column='a_content_type' mode='text' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_creation_date'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:datasortlink name='creation_date' nlsid='MSG_CREATION_DATE' column='r_creation_date' mode='numeric' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_modify_date'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:datasortlink name='modify_date' nlsid='MSG_MODIFIED_DATE' column='r_modify_date' mode='numeric' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_modifier'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='modifier' nlsid='MSG_MODIFIER' column='r_modifier' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_access_date'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:datasortlink name='access_date' nlsid='MSG_ACCESS_DATE' column='r_access_date' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='custom1'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='leftAlignment doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='custom1' label='<%=form.getCustom1Label()%>' column='owner_name' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>         
          <dmf:columnpanel columnname='custom2'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='leftAlignment doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='custom2' label='<%=form.getCustom2Label()%>' column='owner_name' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>         
          <dmf:columnpanel columnname='custom3'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='leftAlignment doclistfilenamedatagrid' resizable="true">
              <dmf:datasortlink name='custom3' label='<%=form.getCustom3Label()%>' column='owner_name' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>         
          
          <%-- OTHER DISPLAYED COLUMNs--%>
          <dmf:columnpanel columnname='path'>
            <dmf:datagridTh nowrap="true" scope='col' valign='top' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
              <dmf:label name='relationtype' nlsid='MSG_PATH' cssclass='doclistbodyDatasortlink' />
            </dmf:datagridTh>
          </dmf:columnpanel>
          <dmf:datagridTh width="89%">&nbsp;</dmf:datagridTh>
        </tr>
        <%-- row block contains databound controls and action controls --%>
        <dmf:datagridRow cssclass="defaultDatagridRowStyle" altclass="defaultDatagridRowAltStyle">
          <dmf:datagridRowTd height='24' cssclass="doclistcheckbox">
            <dmfx:actionmultiselectcheckbox name='check' value='false'>
              <dmf:argument name='objectId' datafield='r_object_id' />
              <dmf:argument name='relationObjectId' datafield='relationobjectid' />
              <dmf:argument name='type' datafield='r_object_type' />
              <dmf:argument name='title' datafield='title' />
              <dmf:argument name='lockOwner' datafield='r_lock_owner' />
              <dmfx:argument name='folderId' contextvalue='folderId' />
              <dmf:argument name='ownerName' datafield='owner_name' />
              <dmf:argument name='contentSize' datafield='r_content_size' />
              <dmf:argument name='contentType' datafield='a_content_type' />
              <dmf:argument name='startworkflowId' value='startworkflow' />
              <dmf:argument name='workflowRuntimeState' value='-1' />
              <dmf:argument name='isReference' datafield='i_is_reference' />
              <dmf:argument name='isReplica' datafield='i_is_replica' />
            </dmfx:actionmultiselectcheckbox>
            <dmf:datagridRowEvent eventname="select" eventhandler="onCEMSelectObject" runatclient="true">
              <dmf:argument name='objectId'   datafield='r_object_id'/>
              <dmf:argument name='objectName' datafield='object_name'/>
            </dmf:datagridRowEvent>            
          </dmf:datagridRowTd>
          <dmf:datagridRowTd nowrap="true" cssclass="doclistlocicon">
            <dmfx:docbaselockicon datafield='r_lock_owner' size='16' />
          </dmf:datagridRowTd>
          <dmf:datagridRowTd nowrap="true" scope='row' cssclass='doclistfilenamedatagrid'>
            <dmf:stringlengthformatter maxlen='32'>
              <dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly' isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16' />
              <%
                  if (dataProvider.getDataField("object_name") == null || dataProvider.getDataField("object_name").length() < 1) {
              %>
              <dmf:datagridRowEvent eventname="dblclick">
                <dmf:link name='object_name' nlsid='MSG_OBJ_NOT_NAMED' onclick='onClickObject'>
                  <dmf:argument name='objectId' datafield='r_object_id' />
                  <dmf:argument name='type' datafield='r_object_type' />
                </dmf:link>
              </dmf:datagridRowEvent>
              <%
                  } else {
              %>
              <dmf:datagridRowEvent eventname="dblclick">
                <dmf:link name='object_name' datafield='object_name' onclick='onClickObject'>
                  <dmf:argument name='objectId' datafield='r_object_id' />
                  <dmf:argument name='type' datafield='r_object_type' />
                </dmf:link>
              </dmf:datagridRowEvent>
              <%
                  }
              %>
            </dmf:stringlengthformatter>
          </dmf:datagridRowTd>
          <dmf:columnpanel columnname='title'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='title' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>          
          <dmf:columnpanel columnname='owner_name'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='owner_name' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='group_name'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='group_name' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_creator_name'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='r_creator_name' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_lock_owner'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='r_lock_owner' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_object_type'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='r_object_type' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_content_size'>
            <dmf:datagridRowTd nowrap="true" cssclass='doclistfilenamedatagrid'>
              <dmfx:docsizevalueformatter datafield='r_object_type'>
                <dmf:label datafield='r_content_size' />
              </dmfx:docsizevalueformatter>
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='a_content_type'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmfx:docformatvalueformatter>
                <dmf:label datafield='a_content_type' />
              </dmfx:docformatvalueformatter>
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_creation_date'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:datevalueformatter>
                <dmf:label datafield='r_creation_date' />
              </dmf:datevalueformatter>
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_modify_date'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:datevalueformatter>
                <dmf:label datafield='r_modify_date' />
              </dmf:datevalueformatter>
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_modifier'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='r_modifier' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='r_access_date'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:datevalueformatter>
                <dmf:label datafield='r_access_date' />
              </dmf:datevalueformatter>
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='custom1'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='r_modifier' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='custom2'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='r_modifier' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:columnpanel columnname='custom3'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid'>
              <dmf:label datafield='r_modifier' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>

          <%-- other displayed columns --%>
          <dmf:columnpanel columnname='path'>
            <dmf:datagridRowTd cssclass='doclistfilenamedatagrid nowrap'>
              <dmfx:primaryfolderpathlink datafield='r_object_id' showfullpath='false' onclick='onClickObject' />
            </dmf:datagridRowTd>
          </dmf:columnpanel>
          <dmf:datagridRowTd width="89%">&nbsp;</dmf:datagridRowTd>
          </tr>
        </dmf:datagridRow>
        <dmf:nodataRow>
          <td colspan='37' height='24' class='doclistfilenamedatagrid'>
            <dmf:label nlsid='MSG_EMPTY' />
          </td>
        </dmf:nodataRow>
      </dmf:datagrid>
    </dmfx:actionmultiselect>
    </dmf:panel>    
  </dmf:form>
</dmf:body>
</dmf:html>
