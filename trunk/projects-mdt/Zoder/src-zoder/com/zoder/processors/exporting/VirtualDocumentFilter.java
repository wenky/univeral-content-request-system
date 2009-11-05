package com.zoder.processors.exporting;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.util.Context;

public class VirtualDocumentFilter extends AbstractVersionProcessor 
{
    public void processVersion(DctmAccess access, Map script,Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try {
            session = access.getSession();                                    
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            if (so.isVirtualDocument()) {
                throw new DocumentError("VirtualDocumentFilter - virtual document detected on id: "+so.getObjectId()+ " chronid: "+so.getChronicleId(),document);
            }        
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
    }


}
