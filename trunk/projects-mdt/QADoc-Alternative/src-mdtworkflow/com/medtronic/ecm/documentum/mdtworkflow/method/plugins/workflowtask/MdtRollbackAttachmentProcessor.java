package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;


// big huge todo: ?rollback - discuss, rethink with MJH when have time...

public class MdtRollbackAttachmentProcessor extends MdtProcessAttachments
{
	// ?transaction safe?
	public void processAttachment(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc, IDfSysObject attachment, Map context)
    {
        try { 
        	// TODO:
        	// - relationship triggers?
        	// - copies?
            /*-INFO-*/Lg.inf("ROLLBACK invoked on %s",attachment);
            /*-dbg-*/Lg.dbg("deleting current version");
            attachment.destroy();
            /*-dbg-*/Lg.dbg("current version deleted successfully");
        } catch (DfException dfe) {
    	    /*-ERROR-*/Lg.err("error while rollback/destroying current version of attachment document %s",attachment,dfe);
            throw EEx.create("RollbackAttachment","error while rollback/destroying current version of attachment document %s",attachment,dfe);        	
        }
		
    }	


}
