package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.scheduledtask;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;


// base method that iterates on the most recent version of an attachment in the dmi_attachment table
public class MdtPromoteAttachment extends MdtProcessAttachments
{
	
	// ?transaction safe?
	public void processAttachment(IDfSessionManager smgr, String docbase, String mdtapp, IDfSysObject jobobject, IDfSysObject formobj, IDfSysObject attachment, Map jobparameters, Map context)
    {
		// promote the attachment
        try { 
	        // promote it?
		    /*-dbg-*/Lg.dbg("check if promotable",null,null);
	        if (attachment.canPromote())
	        {
	            /*-dbg-*/Lg.dbg("invoking promote from workflow");
	            attachment.promote(null,true,false);
	            /*-dbg-*/Lg.dbg("promote returned successfully");
	        } else {
	        	/*-WARN-*/Lg.wrn("attachment document canPromote() returned false %s",attachment);
	        }
        } catch (DfException dfe) {
    	    /*-ERROR-*/Lg.err("error while promoting attachment document %s",attachment,dfe);
            throw EEx.create("PromoteAttachment","error while promoting attachment document %s",attachment,dfe);        	
        }
		
    }	

}
