package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.util.DctmUtils;

public class MdtVersionAttachmentProcessor extends MdtProcessAttachments 
{
    public void processAttachment(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc, IDfSysObject attachment, Map context)
    {
        try {
            String versiontype = (String)context.get("Versioning");
            
            IDfSysObject newdocversion = DctmUtils.versionDocument(attachment,versiontype,false);
            
            /*-dbg-*/Lg.dbg("append new docid to the m_rollback attr of the form for rollback/abort tracking");
            controldoc.appendString("m_rollback", newdocversion.getObjectId().getId());
            controldoc.save();
            /*-dbg-*/Lg.dbg("done");

        } catch (DfException e) {
            /*-ERROR-*/Lg.err("Error encountered versioning doc %s",attachment,e);
            throw EEx.create("VersionAttachment","Error encountered versioning doc %s",attachment,e);
        }
    	
    }

}
