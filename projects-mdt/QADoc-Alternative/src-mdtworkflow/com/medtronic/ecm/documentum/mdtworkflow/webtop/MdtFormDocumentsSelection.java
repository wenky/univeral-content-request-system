package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.form.control.databound.DatagridRow;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.control.action.ActionMultiselectCheckbox;
import com.documentum.webcomponent.navigation.objectgrid.ObjectGrid;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;

public class MdtFormDocumentsSelection extends ObjectGrid
{
    private HashSet m_visibleAttrsList;
    private static final String STR_SOURCE_PARENT_TRUE_ATTR = "'1' as is_source_parent,";
    private static final String STR_SOURCE_PARENT_FALSE_ATTR = "'0' as is_source_parent,";
    private static final String INTERNAL_ATTRS = "sysobj.r_object_id,rel.r_object_id as relationobjectid,object_name,r_link_cnt,r_is_virtual_doc,owner_name,r_object_type,a_content_type,r_lock_owner,r_content_size,i_is_reference,r_assembled_from_id,r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id ";
    private static final String PREF_CLASSIC_RELATIONSHIPS = "application.display.classic_relationships_columns";
    
    String m_strObjectId = "";
    String m_mdtapp = "";
    
    Map m_selecteddocuments = new TreeMap();
    
    public MdtFormDocumentsSelection()
    {
        m_visibleAttrsList = new HashSet(13, 1.0F);
    }

    public void onInit(ArgumentList args)    
    {
        /*-dbg-*/Lg.dbg("get objid");
        m_strObjectId = args.get("objectId");
        /*-dbg-*/Lg.dbg("objid - %s",m_strObjectId);
        
        String[] objids = args.getValues("objectIds");
        if (objids != null) {
            for (int i=0; i < objids.length; i++) {
                m_selecteddocuments.put(objids[i], null);
            }
        }
        
        /*-dbg-*/Lg.dbg("set visible attributes - object_name, title, path are always there");
        m_visibleAttrsList.add("object_name");
        m_visibleAttrsList.add("title");
        m_visibleAttrsList.add("path");
        
        try {
            IDfSysObject doc = (IDfSysObject)getDfSession().getObject(new DfId(m_strObjectId));
            m_mdtapp = doc.getString("m_application");
        } catch (DfException dfe) {
            throw new WrapperRuntimeException("Invalid object id "+m_strObjectId,dfe);
        }
        
        super.onInit(args);
        setVisibleAttributes(m_visibleAttrsList);
        refreshSelectedDoclist();        
    }
    
    public String getCustom1Label()
    {
        return "custom1"; // TODO: make it config-based
    }

    public String getCustom2Label()
    {
        return "custom2"; // TODO: make it config-based
    }
    
    public String getCustom3Label()
    {
        return "custom3"; // TODO: make it config-based
    }

    // query that will return columns expected in grid description in jsp file
    // TODO: parameterized / config'd custom columns (three for now)
    protected String getQuery(String strVisibleAttrs, ArgumentList args)
    {
        String strObjectId = args.get("objectId");
        m_strObjectId = strObjectId;
        if(strObjectId == null)
            throw new IllegalArgumentException(getString("MSG_MISSING_MANDATORY_PARAMS"));
        
        IDfSession dfSession = getDfSession();
        try { 
            IDfSysObject formdoc = (IDfSysObject)dfSession.getObject(new DfId(strObjectId));
            String idlist = null;
            for (int i=0; i < formdoc.getValueCount("m_attachments"); i++) // TODO <-- param'd attachments attribute
            {
                String docname = formdoc.getRepeatingString("m_attachments",i);
                IDfSysObject attdoc = AttachmentUtils.lookupAttachmentByName(formdoc, docname);
                if (idlist == null) 
                    idlist = "'"+attdoc.getObjectId().getId()+"'";
                else
                    idlist += ",'"+attdoc.getObjectId().getId()+"'";
            }
            if (idlist == null) idlist = "'0000000000000000'";
            /*-dbg-*/Lg.dbg("id list: %s",idlist);
                       
            String query = "SELECT r_object_id,title,object_name,r_link_cnt,r_is_virtual_doc,owner_name,r_object_type,a_content_type,r_lock_owner,r_content_size,i_is_reference,r_assembled_from_id," +
                              "r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id,subject as custom1, subject as custom2, subject as custom3 " +
                           "FROM mdt_qad_doc " +
                           "WHERE r_current_state = 0 AND m_application = '"+m_mdtapp+"' ORDER BY object_name";
            
            /*-dbg-*/Lg.dbg("OBJECTGRID query: %s",query);            
            
            return query;
        } catch (Exception e) {
            throw new WrapperRuntimeException("Error getting lookup query for form attachments list",e);
        }
        
    }
    
    public void onCompleteAddDocument(Button control, ArgumentList args){
      
    }
    

    protected void readVisibleAttrs()
    {
        IConfigElement columnConfig = lookupElement("columns");
        if(columnConfig != null)
        {
            Iterator columns = columnConfig.getChildElements("column");
            do
            {
                if(!columns.hasNext())
                    break;
                IConfigElement child = (IConfigElement)columns.next();
                Boolean bVisible = child.getValueAsBoolean();
                if(bVisible == null || bVisible.booleanValue())
                {
                    String strAttr = child.getAttributeValue("attribute");
                    m_visibleAttrsList.add(strAttr);
                    Boolean isDocbaseObjectAttr = child.getAttributeValueAsBoolean("is_docbase_object_attr");
                    if(isDocbaseObjectAttr != null && !isDocbaseObjectAttr.booleanValue())
                        m_nonDocbaseObjectAttrs.add(strAttr);
                }
            } while(true);
        }
    }

    protected String getVisibleAttrsQueryString()
    {
        StringBuffer sbReturnList = new StringBuffer(128);
        Iterator itrKeys = m_visibleAttrsList.iterator();
        do
        {
            if(!itrKeys.hasNext())
                break;
            String strAttrName = (String)itrKeys.next();
            if(!m_nonDocbaseObjectAttrs.contains(strAttrName))
            {
                sbReturnList.append(strAttrName);
                sbReturnList.append(',');
            }
        } while(true);
        return sbReturnList.toString();
    }

    private void setVisibleAttributes(Set attrList)
    {
        if(attrList != null && attrList.size() != 0)
            m_datagrid.setVisibleColumns(attrList);
    }

    protected String getColumnsPreferenceId()
    {
        String prefId = lookupString("columnpreferenceid", false);
        return prefId != null && prefId.length() != 0 ? prefId : "application.display.classic_relationships_columns";
    }

    public void addAttachment(Button control, ArgumentList args)
    {
        Datagrid docselection = (Datagrid)this.getControl(MdtFormDocumentsSelection.GRID_NAME,Datagrid.class);
        List rows =docselection.getDatagridRows();
        
        Iterator z = docselection.getContainedControls();
        while (z.hasNext()) {
            Control c2 = (Control)z.next();
            if (c2 instanceof DatagridRow) {
                DatagridRow dr = (DatagridRow)c2;
                Iterator x = dr.getContainedControls();
                while (x.hasNext()) {
                    Control c3 = (Control)x.next();
                    int y=0; 
                    y++;
                    
                }
            }
        }
        
        
        DataProvider provider = docselection.getDataProvider();
        for (int i=0; i < rows.size(); i++)
        {
            DatagridRow row = (DatagridRow)rows.get(i);
            Iterator ctrls = row.getContainedControls();
            while (ctrls.hasNext())
            {
                Control ctrl = (Control)ctrls.next();
                String name = ctrl.getName();
                String datafield = ctrl.getDatafield();
                if (ctrl instanceof ActionMultiselectCheckbox) {
                    ActionMultiselectCheckbox ch = (ActionMultiselectCheckbox)ctrl;
                    boolean selected = ch.getValue();
                    int j=0; 
                    j++;
                }                
                if (ctrl instanceof Checkbox) {
                    Checkbox ch = (Checkbox)ctrl;
                    boolean selected = ch.getValue();
                    int j=0; 
                    j++;
                }
            }
        }
        
    }
    
    public void refreshSelectedDoclist() 
    {
        Iterator i = m_selecteddocuments.keySet().iterator();
        String labelhtml = "";
        while (i.hasNext()) {
            String id = (String)i.next();
            try { 
                IDfSysObject so = null;
                if (m_selecteddocuments.get(id) == null) {
                    so = (IDfSysObject)getDfSession().getObject(new DfId(id));
                    m_selecteddocuments.put(id, so);
                } else {
                    so = (IDfSysObject)m_selecteddocuments.get(id);
                }
                labelhtml += so.getObjectName() +"<br>";
            } catch (DfException dfe) {
                throw new WrapperRuntimeException("Error refreshing selected documents list",dfe);                
            }
           
        }
        
        Label selecteddocs = (Label)this.getControl("mdtselecteddocs",Label.class);
        selecteddocs.setLabel(labelhtml);
        
    }
    

    public void removeAttachment(Button control, ArgumentList args)
    {
        String objectids = args.get("objectIds");
        String[] objids = objectids.split(",");
        for (int i=0; i < objids.length; i++) {
            if (m_selecteddocuments.containsKey(objids[i]))
            {
                m_selecteddocuments.remove(objids[i]);
            } else {
                m_selecteddocuments.put(objids[i],null);
            }
        }
        refreshSelectedDoclist();        
    }
    
    public void onRowSelect(DatagridRow row, ArgumentList args)
    {
        int a=0;
        a++;
        
    }

    public void onClickObject(DatagridRow row, ArgumentList args)
    {
        int a=0;
        a++;
        
    }
    
    public void onAttachmentOk(Button control, ArgumentList args)
    {
        try { 
            IDfSysObject so = (IDfSysObject)getDfSession().getObject(new DfId(this.m_strObjectId));
            AttachmentUtils.removeAllAttachmentsFromForm(so);
            Iterator i = m_selecteddocuments.keySet().iterator();
            AttachmentUtils.addAttachmentsToForm(so, i);            
        } catch (DfException e) {
            throw new WrapperRuntimeException("Error saving attachment list to form document",e);                            
        }
        this.setComponentReturn();
        
    }

    public void onAttachmentCancel(Button control, ArgumentList args)
    {
        this.setComponentReturn();
        
    }

}
