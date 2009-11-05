package com.medtronic.ecm.documentum.mdtworkflow.common.plugins.attachment;

import java.util.Map;

import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSysObject;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.common.plugins.IMdtAddAttachment;

public class MdtAddRelationship implements IMdtAddAttachment 
{
    public void add(IDfSysObject formdoc, IDfSysObject attachment, Map crconfig, Map pluginconfig)
    {
        try { 
            /*-dbg-*/Lg.dbg("get relation type");
            String relationtypename = (String)pluginconfig.get("RelationType");            
            /*-dbg-*/Lg.dbg("create relation of type %s from psi %s to doc %s",relationtypename,formdoc,attachment);
            IDfRelation docRelation = formdoc.addChildRelative(relationtypename, attachment.getObjectId(), null, false,"");
    	    /*-dbg-*/Lg.dbg("saving relation");
            docRelation.save();
    	    /*-dbg-*/Lg.dbg("saved");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("MdtAddRelationship : Exception Occurred ",e);
            throw EEx.create("MdtAddRelationship-Error","MdtAddRelationship : Exception Occurred ",e);
        }
  
    }	
}
