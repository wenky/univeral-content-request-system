package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Map;

import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

public class MdtAddRelationship extends MdtProcessAttachments
{
    // ?transaction safe?
    public void processAttachment(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc, IDfSysObject attachment, Map context)
    {
        try {
            /*-dbg-*/Lg.wrn("get relation type");
            String relationtypename = (String)context.get("RelationType");            
            /*-dbg-*/Lg.wrn("create relation of type %s from psi %s to doc %s",relationtypename,controldoc,attachment);
            IDfRelation docRelation = controldoc.addChildRelative(relationtypename, attachment.getObjectId(), null, false,"");
            /*-dbg-*/Lg.wrn("saving relation");
            docRelation.save();
            /*-dbg-*/Lg.wrn("saved");                   
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while rollback/destroying current version of attachment document %s",attachment,dfe);
            throw EEx.create("RollbackAttachment","error while rollback/destroying current version of attachment document %s",attachment,dfe);          
        }
        
    }   


}
