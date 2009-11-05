package com.zoder.processors.exporting;

import java.util.Map;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractFolderProcessor;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;

public class FolderAttributes extends AbstractFolderProcessor
{

    public void processFolder(DctmAccess access, Map script, Context context, Map folder, String folderid) throws Exception 
    {
        Map targetdata = CollUtils.getMap(folder, "Target");
        IDfSession session = null;
        try {
            session = access.getSession();
            IDfFolder fldobj = (IDfFolder)session.getObject(new DfId((String)folder.get("ObjectId")));

            targetdata.put("Source.ObjectId", fldobj.getObjectId().getId());
            targetdata.put("Source.ObjectName", fldobj.getObjectName());
            targetdata.put("Source.Type", fldobj.getTypeName());
            targetdata.put("Source.ACL", fldobj.getACLName());
            targetdata.put("Source.AliasSet", fldobj.getAliasSet());
            targetdata.put("Source.Links", folder.get("LinkOrder"));
        
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
        
        
    }

}
