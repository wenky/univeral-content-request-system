package com.medtronic.ecm.documentum.mdtworkflow.common.plugins.attachment;

import java.util.Map;

import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSysObject;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.common.plugins.IMdtRemoveAttachment;

public class MdtRemoveRelationship implements IMdtRemoveAttachment
{
    public void remove(IDfSysObject formdoc, IDfSysObject attachment, Map crconfig, Map pluginconfig)
    {
        try { 
            /*-dbg-*/Lg.dbg("get relation type");
            String relationtypename = (String)pluginconfig.get("RelationType");
            IDfRelation relation = (IDfRelation)formdoc.getSession().getObjectByQualification(
                                                          "dm_relation where relation_name = '"+relationtypename+"' and " +
            		                                      "parent_id = '"+formdoc.getObjectId().getId()+"' and " +
            		                                      "child_id in (SELECT r_object_id from dm_sysobject(all) where i_chronicle_id = '"+attachment.getChronicleId().getId()+"')");
            /*-dbg-*/Lg.dbg("destroy relation %s",relation);
            relation.destroy();
            /*-dbg-*/Lg.dbg("destroyed");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("MdtRemoveRelationship : Exception Occurred ",e);
            throw EEx.create("MdtRemoveRelationship-Error","MdtRemoveRelationship : Exception Occurred ",e);
        }

    }
    

}
