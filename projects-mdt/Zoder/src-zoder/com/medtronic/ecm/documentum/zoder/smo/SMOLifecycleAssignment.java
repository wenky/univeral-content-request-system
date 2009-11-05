package com.medtronic.ecm.documentum.zoder.smo;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.zoder.processors.conversion.LifecycleAssignment;

public class SMOLifecycleAssignment extends LifecycleAssignment
{
    
    public String getLifecycleMapReference(IDfSession session, Map document, Map version, Map context, IDfSysObject so) throws Exception
    {
        // how will this work? same for all?
        return ("qad_quality_doc");
    }
    public String getAliasMapReference(IDfSession session, Map document, Map version, Map context, IDfSysObject so) throws Exception
    {
        return ("qad_smo_qdoc");
    }

    public String getStateMapReference(IDfSession session, Map document, Map version, Map context, IDfSysObject so) throws Exception
    {
        String oldid = so.getId("m_old_object_id").getId();
        List olddata = (List)context.get("OldData");
        for (int i=0; i < olddata.size(); i++) {
            String[] row = (String[])olddata.get(i);
            if (row[0].equals(oldid)) {
                // ? match on... aclname?
                return row[2]; // <-- guessing for now
            }
        }
        throw new Exception("For id "+so.getObjectId().getId()+" name "+so.getObjectName()+", oldid "+oldid+" not found in old data list");
    }
    

}
