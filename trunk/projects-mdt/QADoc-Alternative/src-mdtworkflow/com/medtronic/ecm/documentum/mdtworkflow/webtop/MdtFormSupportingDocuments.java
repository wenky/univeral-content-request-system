package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.navigation.objectgrid.ObjectGrid;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;

public class MdtFormSupportingDocuments extends ObjectGrid
{
    private HashSet m_visibleAttrsList;
    private static final String STR_SOURCE_PARENT_TRUE_ATTR = "'1' as is_source_parent,";
    private static final String STR_SOURCE_PARENT_FALSE_ATTR = "'0' as is_source_parent,";
    private static final String INTERNAL_ATTRS = "sysobj.r_object_id,rel.r_object_id as relationobjectid,object_name,r_link_cnt,r_is_virtual_doc,owner_name,r_object_type,a_content_type,r_lock_owner,r_content_size,i_is_reference,r_assembled_from_id,r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id ";
    private static final String PREF_CLASSIC_RELATIONSHIPS = "application.display.classic_relationships_columns";
    
    protected int m_customcolumncount = 1;    
    String m_strObjectId = "";
    
    protected String getDocAttr() { return "m_supporting_documents"; }
    protected String getAddKey() { return "SupportingDocumentAdditionPlugins"; }
    protected String getRemoveKey() { return "SupportingDocumentRemovalPlugins"; }
    
    
    public MdtFormSupportingDocuments()
    {
        m_visibleAttrsList = new HashSet(13, 1.0F);
    }
    
    public boolean isEditable() 
    {
        try { 
            IDfSysObject formdoc = (IDfSysObject)getDfSession().getObject(new DfId(m_strObjectId));
            if (formdoc.getPermit() >= IDfACL.DF_PERMIT_WRITE) {
                return true;
            }
            return false;
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error in checking editablity rights on form %s",m_strObjectId,dfe);
            throw new WrapperRuntimeException("Error in checking editablity rights on form "+m_strObjectId,dfe);
        }
        
    }

    public void onInit(ArgumentList args)
    {
        /*-dbg-*/Lg.dbg("get objid");
        m_strObjectId = args.get("objectId");
        /*-dbg-*/Lg.dbg("objid - %s",m_strObjectId);
        
        // check to see if m_attachments attribute has a single, empty value in it
        IDfSysObject formdoc = null;
        try { 
            formdoc = (IDfSysObject)getDfSession().getObject(new DfId(m_strObjectId));
            if (formdoc.getValueCount(getDocAttr()) == 1 && "".equals(formdoc.getRepeatingString(getDocAttr(),0).trim())) {
                formdoc.remove(getDocAttr(), 0);
                formdoc.save();
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error in getting form document %s",m_strObjectId,dfe);
            throw new WrapperRuntimeException("Error in getting form document "+m_strObjectId,dfe);
        }
            
        
        
        // check for multiselect objectids 
        if ("multi".equals(args.get("objectIds"))) {
            // parse componentargs
            String[] compargs = args.getValues("componentArgs");
            if (compargs != null) {
                for (int a=0; a < compargs.length; a++) {
                    // get objectId from comparg
                    String carg = compargs[a];
                    // add to m_attachments...
                    int start = carg.indexOf("objectId~");
                    String id = carg.substring(start+9,start+25);
                    compargs[a] = id;
                    
                }
                for (int b=0; b < compargs.length; b++) {
                    String curid = compargs[b];
                    AttachmentUtils.addDocumentToDocumentAttribute(formdoc, curid, getDocAttr(), getAddKey());
                }                    
            }
        }
                
        m_visibleAttrsList.add("object_name");
        m_visibleAttrsList.add("title");
        m_visibleAttrsList.add("path");
        
        //List customcolumns = getAttachmentsCustomColumnList(formdoc);
        
        super.onInit(args);
        setVisibleAttributes(m_visibleAttrsList);
        
        if (!isEditable()) {
            Button b = (Button)getControl("addSupporting",Button.class);
            b.setVisible(false);
            b.setEnabled(false);
        }

    }

    // query that will return columns expected in grid description in jsp file
    protected String getQuery(String strVisibleAttrs, ArgumentList args)
    {        
        try {                        
            IDfSysObject formdoc = (IDfSysObject)getDfSession().getObject(new DfId(m_strObjectId));
            //String customcolumns = getAttachmentsCustomColumnListForQuery(formdoc);            
            //List customtypes = getAttachmentsCustomTypeList(formdoc); <-- for add attachments (UNION of types and other where clauses)
            
            String query = "SELECT r_object_id,object_name,title,r_link_cnt,r_is_virtual_doc,owner_name,r_object_type,a_content_type,r_lock_owner,r_content_size,i_is_reference,r_assembled_from_id," +
                              "r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id, subject as custom1 " +
                           "FROM mdt_document " +
                           "WHERE r_object_id in (select r_object_id from mdt_document where object_name in (select "+getDocAttr()+" from "+formdoc.getTypeName()+"(all) where r_object_id = '"+m_strObjectId+"')) ";            
            /*-dbg-*/Lg.dbg("OBJECTGRID query: %s",query);                        
            return query;
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("Error getting lookup query for form attachments list",e);
            throw new WrapperRuntimeException("Error getting lookup query for form attachments list");
        }
        
    }
    
    public void onCompleteAddDocument(Button control, ArgumentList args){
        try { 
            /*-dbg-*/Lg.dbg("onCompleteAddDocument clicked - get ");                        
            IDfSysObject formdoc = (IDfSysObject)getDfSession().getObject(new DfId(m_strObjectId));
            String[] idlist = new String[formdoc.getValueCount(getDocAttr())];
            for (int i=0; i < formdoc.getValueCount(getDocAttr()); i++) // TODO <-- param'd attachments attribute
            {
                String docname = formdoc.getRepeatingString(getDocAttr(),i);
                IDfSysObject attdoc = AttachmentUtils.lookupAttachmentByName(formdoc, docname);
                idlist[i] = attdoc.getObjectId().getId();
            }        
            args.add("objectId", m_strObjectId);
            args.add("objectIds", idlist);
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error prepping object list for jump to add attachments component",dfe);
            throw new WrapperRuntimeException("Error prepping object list for jump to add attachments component");            
        }
        
        setComponentNested("mdtformsupportingdocumentsselection", args, getContext(), this);
        
        Datagrid displaygrid = (Datagrid)getControl(GRID_NAME,Datagrid.class);
        displaygrid.getDataProvider().refresh();
      
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
    
    /*
    class returnlistener implements IReturnListener
    {
        public void onReturn(Form form, Map map) {
            Datagrid displaygrid = (Datagrid)form.getControl(GRID_NAME,Datagrid.class);
            displaygrid.getDataProvider().refresh();            
        }
        
    }*/

}
