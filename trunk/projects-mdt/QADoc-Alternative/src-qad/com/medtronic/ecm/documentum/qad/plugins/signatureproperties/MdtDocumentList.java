package com.medtronic.ecm.documentum.qad.plugins.signatureproperties;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.qad.plugins.IMdtSignaturePropertiesPlugin;

public class MdtDocumentList implements IMdtSignaturePropertiesPlugin {

    public void getProperties(IDfSysObject tboinstance, Map propertymap,
            String username, String justification, String signaturemethod,
            String appprops, String passthru1, String passthru2, Map config)
            throws Exception {
        String relationtypename = (String)config.get("RelationTypeName");
        // look up the form
        String parentquery = "select parent_id from dm_relation where relation_name = '"+relationtypename+"' and child_id = '"+tboinstance.getObjectId().getId()+"'";        
        /*-dbg-*/Lg.wrn("QUERY: %s",parentquery);
        IDfQuery qry = new DfQuery();
        IDfCollection packages =null;
        IDfSysObject psidoc = null;
        try {
            qry.setDQL(parentquery);
            /*-dbg-*/Lg.wrn("exec query");
            packages = qry.execute(tboinstance.getObjectSession(),IDfQuery.DF_READ_QUERY);
            if (packages.next()) 
            {
                /*-dbg-*/Lg.wrn("get next related doc");
                String compid = packages.getId("parent_id").getId();
                /*-dbg-*/Lg.wrn("lookup %s",compid);
                psidoc = (IDfSysObject)tboinstance.getObjectSession().getObject(new DfId(compid));
                /*-dbg-*/Lg.wrn("form obj: %s",psidoc);                
            }
            packages.close(); packages = null;
        } finally {
            try { if (packages != null)packages.close(); } catch (Exception e) {}
        }
                
        // get the list of documents associated with the form.
        List attachmentlist = AttachmentUtils.getAttachmentsByRelationship(tboinstance.getObjectSession(),psidoc,relationtypename);
        // iterate through and make list of names? other attributes? make configurable? name+title for now
        for (int i=0; i< attachmentlist.size(); i++) {
            IDfSysObject attachment = (IDfSysObject)attachmentlist.get(i);
            propertymap.put("mdtattachment"+i+"_objectname",attachment.getObjectName());
            propertymap.put("mdtattachment"+i+"_title", attachment.getTitle());
        }

    }

}
