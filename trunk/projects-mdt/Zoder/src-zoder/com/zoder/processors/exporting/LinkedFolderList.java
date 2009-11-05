package com.zoder.processors.exporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.util.Context;

// filter folders the versions are linked to, stripping out the folders that aren't being migrated
// - flag documents that are no longer linked as failures

public class LinkedFolderList extends AbstractVersionProcessor 
{
    public void processVersion(DctmAccess access, Map script,Context context, Map document, Map version, int veridx) throws Exception {
        IDfSession session = null;
        boolean mustlink = context.containsKey("MustBeLinked");
        Map folderidmap = (Map)context.get("Folders.IdMap");        
        try {
            session = access.getSession();
                        
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            boolean hasfolder = false;
            List validfolders = new ArrayList();
            for (int i=0; i < so.getFolderIdCount(); i++)
            {
                IDfId folderid = so.getFolderId(i);
                if (folderidmap.containsKey(folderid.getId())) {
                    validfolders.add(folderid.getId());
                    hasfolder = true;
                }
                                    
            }
            
            if (mustlink) {
                if (!hasfolder ) {
                    // uhoh, all the folders it linked to were excluded or not included                    
                    throw new DocumentError("LinkedFolderList - All linked folders were excluded for version: "+(version!=null&&version.containsKey("r_object_id")?version.get("r_object_id"):null),document);
                }
            }
            
            version.put("FolderLinks",validfolders);                
                
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
    }

}
