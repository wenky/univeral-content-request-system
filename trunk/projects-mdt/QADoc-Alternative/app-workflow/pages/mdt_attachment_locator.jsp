
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page errorPage="/wdk/errorhandler.jsp"%>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf"%>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx"%>
<%@ page import="com.documentum.web.form.Form,com.documentum.web.common.BrandingService,com.documentum.web.form.control.Panel"%>
<%@ page import="com.documentum.web.form.control.Hidden"%>
<%@ page import="com.documentum.webcomponent.library.locator.LocatorContainer"%>
<%@ page import="com.documentum.webcomponent.library.locator.ObjectLocator"%>
<%@ page import="com.documentum.webcomponent.library.locator.SysObjectLocator"%>
<dmf:html>
<dmf:head>
  <dmf:webform />
  <script language='JavaScript1.2' src='<%=Form.makeUrl(request,"/webcomponent/library/locator/locator.js")%>'></script>
  <%
      ObjectLocator locatorComp = (ObjectLocator) pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
  %>
  <script type="text/javascript">
    setInLocatorContainer(<%=locatorComp.getTopForm() instanceof LocatorContainer ? "true" : "false"%>);
    setMultiSelectEnabled(<%=locatorComp.isMultiSelectEnabled() ? "true" : "false"%>);
    addHorizontalHeaderScroll("listingheaderarea","listingcontentarea");
  </script>
</dmf:head>
<dmf:body id="modalLocator" topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" showdialogevent='true' width='900' height='500'>
  <dmf:form>
    <%
        ObjectLocator form = (ObjectLocator) pageContext.getAttribute(Form.FORM,PageContext.REQUEST_SCOPE);
        boolean isMultiSelectEnabled = form.isMultiSelectEnabled();
        String strDefaultCols = "*";
        if (isMultiSelectEnabled) {
            strDefaultCols = "*,345";
        }
    %>
    <dmf:paneset name="innerworkareacontent" cols='<%=strDefaultCols%>' cssclass="contentBackground" toppadding="10" bottompadding="10" rightpadding="10" minheight="0" minwidth="0" leftpadding="10">
      <dmf:paneset name="listingInnerworkareacontent" rows="35,26,*,35" cssclass='locatorborderstyle'>
        <dmf:datagrid name='<%=ObjectLocator.ADDOBJECT_GRID%>' paged='true' preservesort='false' rendertable='false' cssclass='doclistbodyDatagrid' width='100%' cellspacing='0' cellpadding='0' bordersize='0' rowselection='true'>
          <dmf:pane name="dropdownconent" overflow="hidden">
            <table cellspacing='0' cellpadding='0' border='0' width="100%">
              <tr valign='middle'>
                <td valign='middle' class="pagerBackground locatorPatternFilterLeftPadding" height="35">
                  <dmf:panel name='<%=ObjectLocator.NAMEFILTER_PANEL%>'>
                    <table class="leftAlignment" cellspacing='0' cellpadding='0' border='0'>
                      <tr valign='middle'>
                        <td class="leftAlignment" valign='middle'>
                          <dmf:promptedtext name="<%=ObjectLocator.NAMEFILTERSTRING%>" size="24" defaultonenter="true" nlsid="MSG_FILTER" autocompleteid="ac_sysobjnamefilter" onclick="onClickStartsWith" />
                        </td>
                      </tr>
                    </table>
                  </dmf:panel>
                </td>
                <td width="80%" class="pagerBackground rightAlignment locatorAttributeFilterRightPadding">
                  <dmf:panel name='<%=SysObjectLocator.PANEL_ATTRIBUTEFILTERS + "0"%>'>
                    <table cellspacing='0' cellpadding='0' border='0'>
                      <tr>
                        <td align='center' valign='middle' colspan=20>
                          <dmf:datadropdownlist name='<%=SysObjectLocator.DROPDOWN_ATTRIBUTEFILTERS + "0"%>' onselect="onChangeAttributeFilter" tooltipnlsid="MSG_FILTER_1">
                            <dmf:dataoptionlist>
                              <dmf:option datafield="filterid" labeldatafield="name" />
                            </dmf:dataoptionlist>
                          </dmf:datadropdownlist>
                        </td>
                      </tr>
                    </table>
                  </dmf:panel>
                </td>
                <td width="80%" class="pagerBackground rightAlignment locatorAttributeFilterRightPadding">
                  <dmf:panel name='<%=SysObjectLocator.PANEL_ATTRIBUTEFILTERS + "1"%>'>
                    <table valign='top' cellspacing='0' cellpadding='0' border='0'>
                      <tr valign='middle'>
                        <td class="rightAlignment" valign='middle' colspan=20>
                          <dmf:datadropdownlist name='<%=SysObjectLocator.DROPDOWN_ATTRIBUTEFILTERS + "1"%>' onselect="onChangeAttributeFilter" tooltipnlsid="MSG_FILTER_2">
                            <dmf:dataoptionlist>
                              <dmf:option datafield="filterid" labeldatafield="name" />
                            </dmf:dataoptionlist>
                          </dmf:datadropdownlist>
                        </td>
                      </tr>
                    </table>
                  </dmf:panel>
                </td>
                <td width="80%" class="pagerBackground rightAlignment locatorAttributeFilterRightPadding">
                  <dmf:panel name='<%=SysObjectLocator.PANEL_ATTRIBUTEFILTERS+ "2"%>'>
                    <table class="rightAlignment" valign='middle' cellspacing='0' cellpadding='0' border='0'>
                      <tr valign='middle'>
                        <td class="rightAlignment" valign='middle' colspan=20>
                          <dmf:datadropdownlist name='<%=SysObjectLocator.DROPDOWN_ATTRIBUTEFILTERS+ "2"%>' onselect="onChangeAttributeFilter" tooltipnlsid="MSG_FILTER_3">
                            <dmf:dataoptionlist>
                              <dmf:option datafield="filterid" labeldatafield="name" />
                            </dmf:dataoptionlist>
                          </dmf:datadropdownlist>
                        </td>
                      </tr>
                    </table>
                  </dmf:panel>
                </td>
                <td width="80%" class="pagerBackground rightAlignment locatorAttributeFilterRightPadding">
                  <dmf:panel name='<%=SysObjectLocator.PANEL_VERSIONFILTERS%>'>
                    <table class="rightAlignment" valign='middle' cellspacing='0' cellpadding='0' border='0'>
                      <tr valign='middle'>
                        <td class="rightAlignment" valign='middle' colspan=20>
                          <dmf:dropdownlist name='<%=SysObjectLocator.DROPDOWN_VERSIONFILTERS%>' onselect="onChangeVersionFilter" tooltipnlsid="MSG_VERSION_FILTER">
                            <dmf:option value='CURRENT' nlsid='MSG_CURRENT_VERSIONS' />
                            <dmf:option value='ALL' nlsid='MSG_ALL_VERSIONS' />
                          </dmf:dropdownlist>
                        </td>
                      </tr>
                    </table>
                  </dmf:panel>
                </td>
                <td width="80%" class="pagerBackground rightAlignment locatorAttributeFilterRightPadding">
                  <dmf:panel name='panel_contenttype' visible='false'>
                    <table class="rightAlignment" valign='middle' cellspacing='0' cellpadding='0' border='0'>
                      <tr valign='middle'>
                        <td class="rightAlignment" valign='middle' colspan=20>
                          <dmf:datadropdownlist width='400' name='<%=SysObjectLocator.CONTROL_CONTENTTYPES%>' onselect='onSelectContentTypeFilter' tooltipnlsid="MSG_FILE_FILTER" cssclass='defaultDropDownFilter'>
                            <dmf:dataoptionlist>
                              <dmf:option datafield="name" labeldatafield="description" />
                            </dmf:dataoptionlist>
                          </dmf:datadropdownlist>
                        </td>
                      </tr>
                    </table>
                  </dmf:panel>
                </td>
                </td>
              </tr>
            </table>
          </dmf:pane>
          <dmf:pane name="listingheaderarea" overflow="hidden">
            <table cellspacing='0' cellpadding='0' border='0' width='100%' class="colHeaderBackground">
              <tr>
                <td colspan="20" height="1" class="spacer" bgcolor="#ffffff">
                  &nbsp;
                </td>
              </tr>
              <tr valign='middle'>
                <th scope='col' nowrap width="22" height=25>
                  &nbsp;
                </th>
                <th scope='col' nowrap width="18">
                  &nbsp;
                </th>
                <th scope='col' nowrap width="18" align='center' valign="center">
                  &nbsp;
                  <dmf:button name="<%=ObjectLocator.UPONELEVEL_BUTTON%>" tooltipnlsid="MSG_UP_ONE_LEVEL_TIP" onclick="onClickUpOneLevel" src="icons/navigation/upOneLevel.gif" srcdisabled='icons/navigation/upOneLevelDisabled.gif' enabled="false" visible="false" />
                </th>
                <th style="display: none" nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                  &nbsp;
                </th>
                <th style="display: none" nowrap class="doclistfilenamedatagrid" width="165">
                  &nbsp;
                </th>
                <th style="display: none" nowrap class="doclistfilenamedatagrid" width="165">
                  &nbsp;
                </th>
                <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                  <b><dmf:datasortlink name='ad_sort_nm' nlsid='MSG_OBJECT_NAME' column='object_name' cssclass='doclistbodyDatasortlink' />
                  </b>
                </th>
                <dmf:columnpanel columnname='r_version_label'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="100">
                    <b><dmf:label name='ad_sort_vl' nlsid='MSG_VERSION_LABEL' cssclass='doclistbodyLabel' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='owner_name'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_on' nlsid='MSG_OWNER_NAME' column='owner_name' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='group_name'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_gn' nlsid='MSG_GROUP_NAME' column='group_name' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_creator_name'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_cn' nlsid='MSG_CREATOR_NAME' column='r_creator_name' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_object_type'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_ty' nlsid='MSG_OBJECT_TYPE' column='r_object_type' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_creation_date'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_cd' nlsid='MSG_CREATION_DATE' column='r_creation_date' mode='numeric' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_modify_date'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_md' nlsid='MSG_MODIFIED_DATE' column='r_modify_date' mode='numeric' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_modifier'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_mod' nlsid='MSG_MODIFIER' column='r_modifier' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_access_date'>
                  <th scope='col' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                    <b><dmf:datasortlink name='ad_sort_ad' nlsid='MSG_ACCESS_DATE' column='r_access_date' mode='numeric' cssclass='doclistbodyDatasortlink' />
                    </b>
                  </th>
                </dmf:columnpanel>
                <%
                    //
                %>
                <th scope='col' class="leftAlignment" width=60%>
                </th>
              </tr>
            </table>
          </dmf:pane>
          <dmf:hidden name='<%=ObjectLocator.ADDOBJIDS%>' id='addObjIds' onchange='onAddedObjectsChanged' />
          <dmf:pane name="listingcontentarea" overflow="auto">
            <table cellspacing='0' cellpadding='0' border='0' height='100%' width='100%'>
              <dmf:datagridRow height='24' cssclass='defaultDatagridRowStyle' altclass='defaultDatagridRowAltStyle'>
                <td height=24 width="22" class="doclistcheckbox">
                  <dmf:panel datafield='selectable'>
                    <dmf:datagridRowModifier skipbody="false">
                      <dmf:checkbox value='false' name='locator_checkbox' onclick='onAddCellClicked' runatclient='true'>
                        <dmf:argument name='objectId' datafield='r_object_id' />
                      </dmf:checkbox>
                    </dmf:datagridRowModifier>
                  </dmf:panel>
                </td>
                <td style="display: none" width="18">
                  <dmf:hidden datafield='r_object_id' />
                </td>
                <td style="display: none" width="18">
                  <dmf:hidden datafield='object_name' />
                </td>
                <td style="display: none" width="18">
                  <dmf:hidden datafield='r_version_label' />
                </td>
                <td class="doclistlocicon" nowrap width="18">
                  <dmfx:docbaselockicon datafield='r_lock_owner' size='16' />
                </td>
                <td align="center" nowrap>
                  <dmf:panel datafield='selectable'>
                    <dmfx:docbaseicon formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly' isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16' />
                  </dmf:panel>
                  <dmf:panel reversevisible="true" datafield='selectable'>
                    <dmfx:docbaseicon cssclass="nonselectable" formatdatafield='a_content_type' typedatafield='r_object_type' linkcntdatafield='r_link_cnt' isvirtualdocdatafield='r_is_virtual_doc' assembledfromdatafield='r_assembled_from_id' isfrozenassemblydatafield='r_has_frzn_assembly' isreplicadatafield='i_is_replica' isreferencedatafield='i_is_reference' size='16' />
                  </dmf:panel>
                </td>
                <td scope='row' nowrap class="doclistfilenamedatagrid leftAlignment" width="165">
                  <dmf:panel datafield='navigatable'>
                    <dmf:stringlengthformatter maxlen='25'>
                      <dmf:datagridRowEvent eventname="dblclick">
                        <dmf:link name='locator_link' onclick='onClickContainer' datafield='object_name' tooltipdatafield='object_name'>
                          <dmf:argument name='objectId' datafield='r_object_id' />
                          <dmf:argument name='objectName' datafield='object_name' />
                        </dmf:link>
                      </dmf:datagridRowEvent>
                    </dmf:stringlengthformatter>
                  </dmf:panel>
                  <dmf:panel datafield='notnavigatable'>
                    <dmf:stringlengthformatter maxlen="25">
                      <dmf:label datafield='object_name' />
                    </dmf:stringlengthformatter>
                  </dmf:panel>
                </td>
                <dmf:columnpanel columnname='r_version_label'>
                  <td nowrap class="doclistfilenamedatagrid" width="100">
                    <dmfx:folderexclusionformatter datafield='r_object_type'>
                      <dmf:stringlengthformatter maxlen="12">
                        <dmf:label datafield='r_version_label' />
                      </dmf:stringlengthformatter>
                    </dmfx:folderexclusionformatter>
                    &nbsp;
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='owner_name'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:stringlengthformatter maxlen="25">
                      <dmf:label datafield='owner_name' />
                    </dmf:stringlengthformatter>
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='group_name'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:stringlengthformatter maxlen="25">
                      <dmf:label datafield='group_name' />
                    </dmf:stringlengthformatter>
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_creator_name'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:stringlengthformatter maxlen="25">
                      <dmf:label datafield='r_creator_name' />
                    </dmf:stringlengthformatter>
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_object_type'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:stringlengthformatter maxlen="25">
                      <dmf:label datafield='r_object_type' />
                    </dmf:stringlengthformatter>
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_creation_date'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:datevalueformatter>
                      <dmf:label datafield='r_creation_date' />
                    </dmf:datevalueformatter>
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_modify_date'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:datevalueformatter>
                      <dmf:label datafield='r_modify_date' />
                    </dmf:datevalueformatter>
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_modifier'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:stringlengthformatter maxlen="25">
                      <dmf:label datafield='r_modifier' />
                    </dmf:stringlengthformatter>
                  </td>
                </dmf:columnpanel>
                <dmf:columnpanel columnname='r_access_date'>
                  <td nowrap class="doclistfilenamedatagrid" width="165">
                    <dmf:datevalueformatter>
                      <dmf:label datafield='r_access_date' />
                    </dmf:datevalueformatter>
                  </td>
                </dmf:columnpanel>
                <td width=60%>
                  <img src='<%=BrandingService.getThemeResolver().getResourcePath("images/space.gif",pageContext,false)%>' width='1' height='1' onload='checkAddCell(this)' alt=''>
                </td>
              </dmf:datagridRow>
              <dmf:nodataRow height='24' cssclass='contentBackground'>
                <td style="padding: 3px" colspan=20 valign='top'>
                  <dmf:label nlsid='MSG_NO_DATA' />
                </td>
              </dmf:nodataRow>
              <tr class='contentBackground'>
                <td colspan=20 valign=bottom height="100%">
                  &nbsp;
                </td>
              </tr>
            </table>
          </dmf:pane>
          <dmf:pane name="pagerarea" overflow="hidden">
            <table cellspacing='0' cellpadding='0' border='0' width="100%">
              <tr height=35 class="pagerBackground">
                <td align=center valign="middle" nowrap width="95%">
                  <dmf:datapaging name='pagerAdd' showdisplayinginfo='false' />
                </td>
                <td class="spacer" width="10">
                  &nbsp;
                </td>
                <td class="rightAlignment locatorPagingControlRightPadding" valign="middle" nowrap>
                  <table class="rightAlignment" border='0' cellpadding='0' cellspacing='0'>
                    <tr>
                      <td nowrap valign="middle">
                        <dmf:label cssclass='drilldownFileInfo' nlsid='MSG_SHOW_ITEMS' />
                        &nbsp;
                      </td>
                      <td nowrap valign="middle">
                        <dmf:datapagesize name='sizer' preference='application.display.classic' tooltipnlsid='MSG_SHOW_ITEMS' />
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </dmf:pane>
          <img src='<%=BrandingService.getThemeResolver().getResourcePath("images/space.gif",pageContext,false)%>' width='1' height='1' onload='initAddList()' alt=''>
        </dmf:datagrid>
      </dmf:paneset>
      <dmf:panel name='<%=ObjectLocator.REMOVEBODY_PANEL%>'>
        <dmf:paneset name="addRemoveAndSelectedArea" cols='45,*'>
          <dmf:pane name="addRemoveArea" overflow="hidden">
            <table height='100%' align='center' valign='top' border='0'>
              <tr>
                <td valign='bottom'>
                  <dmf:button name='addbutton' default="true" nlsid="MSG_ADD" id='addbtn' onclick="onClickAdd" src="images/button/btn_add.gif" srcdisabled="images/button/btn_add_off.gif" tooltipnlsid="MSG_ADD" />
                </td>
              </tr>
              <tr>
                <td valign='top'>
                  <dmf:button name='removebutton' default="true" nlsid="MSG_REMOVE" id='removebtn' onclick="onClickRemove" src="images/button/btn_remove.gif" srcdisabled="images/button/btn_remove_off.gif" tooltipnlsid="MSG_REMOVE" />
                </td>
              </tr>
            </table>
          </dmf:pane>
          <dmf:paneset name="multiselectGridArea" rows="*" cssclass='borderstyle'>
            <jsp:include page="sysobjMultiselectGrid.jsp" flush="true" />
          </dmf:paneset>
        </dmf:paneset>
      </dmf:panel>
    </dmf:paneset>
    <dmf:panel>
    <table cellpadding="0" cellspacing="0" border="0">
        <tr>
            <td colspan="3" height="7" class="spacer">
                &nbsp;
            </td>
        </tr>
         <tr>
            <td>
                &nbsp;
            </td>
            <td width="10" class="spacer" nowrap>
                &nbsp;
            </td>
            <td>
                <dmf:label name='selectedDocName'/>
                <dmf:button name='addRelation' nlsid='MSG_RETURN' onclick='doReturn'/>
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
    </dmf:panel>
    
  </dmf:form>
</dmf:body>
</dmf:html>
