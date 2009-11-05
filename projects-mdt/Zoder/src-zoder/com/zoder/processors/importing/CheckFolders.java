package com.zoder.processors.importing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;

public class CheckFolders implements IProcessor {

    public void process(Map script, Context context) throws Exception 
    {
        // create the folder structure! Because we are preserving primary link, this may take several passes...
        DctmAccess access = (DctmAccess)context.get("SourceAccess");
        IDfSession session = null;
        try {
            session = access.getSession();                                    
        
            // get acldomain for acl lookups/assignments
            IDfTypedObject serverConfig = session.getServerConfig();
            String aclDomain = serverConfig.getString("operator_name");
        
            //central datastructure for tracking which folders have been created, and what their shiny new IDs are...
            Map newfolders = new HashMap();
                    
            // load the usual suspects from the folder exporting process
            List folderlist = (List)context.get("Folders.FolderList");
            Map folderpaths = (Map)context.get("Folders.FolderPaths");
            Map idchains = (Map)context.get("Folders.IdChains"); // much more important for importing...
            Map idmap = (Map)context.get("Folders.IdMap");
    
            // right, then: iterate through folderlist (it's a good initial sort order)
            List queue = folderlist;
            List waiting = new ArrayList();
            
            while (true) {
                for (int i=0; i < queue.size(); i++) {
                    String sourcepath = (String)queue.get(i);
                    String idchain = (String)idchains.get(sourcepath);
                    // split the idchain
                    String[] ids = idchain.split("-");
                    String srcid = ids[ids.length-1];
                    // make sure they've all been created, except the last one (which is the one we're working on right now)
                    boolean allcreated = true;
                    if (ids.length > 1) {
                        for (int j=0; j < ids.length-1; j++) {
                            if (!newfolders.containsKey(ids[j])) {
                                allcreated = false;                        
                            }
                        }
                    }
                    // now verify the links are all there too
                    Map folderdata = (Map)idmap.get(srcid);
                    List linklist = (List)folderdata.get("Source.Links");
                    if (linklist != null) {
                        for (int k=0; k < linklist.size(); k++) {
                            String linkid = (String)linklist.get(k);
                            if (!newfolders.containsKey(linkid)) {
                                allcreated = false;                        
                            }
                        }
                    }
                    if (allcreated) {
                        // create the new folder. simple, right? for now, crude version: just dm_cabinets and dm_folders
                        IDfFolder folderobj = null;
                        String foldertype = null;
                        if (ids.length == 1) { // cabinet...
                            foldertype = "dm_cabinet";
                        } else {
                            foldertype = "dm_folder";                            
                        }
                        
                        foldertype = getFolderTypeName(session, context, folderdata, foldertype);                        
                        
                        folderobj = (IDfFolder)session.newObject(foldertype);
                        String newobjectname = (String)folderdata.get("Target.ObjectName");
                        if (newobjectname == null) {
                            newobjectname = (String)folderdata.get("Source.ObjectName");
                        }
                        folderobj.setObjectName(newobjectname);
                        if (linklist != null) {
                            for (int m=0; m < linklist.size(); m++) {
                                String srclinkid = (String)linklist.get(m);
                                folderobj.link((String)newfolders.get(srclinkid));
                            }
                        }

                        // acl
                        // try to get the acl specified (provide default)?
                        String aclname = getAclName(session,context,folderdata);
                        if (aclname != null) {
                            IDfACL acl = session.getACL(aclDomain, aclname);
                            folderobj.setACL(acl);
                        }

                        folderobj.save();
                        
                        // add to tracker map
                        newfolders.put(srcid,folderobj.getObjectId().getId());                        
                    } else {
                        // if we've failed, queue it for another pass
                        waiting.add(sourcepath);
                    }
                }
                if (waiting.size() == 0) break;
                queue = waiting;
                waiting = new ArrayList();
            }
            
            // store the old folder id <--> new folder id map
            context.put("Folders.OldToNew", newfolders);
            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
    }
    
    public String getAclName(IDfSession session, Context context, Map folderdata) throws Exception{
        String aclname = (String)context.get("DefaultFolderACL");
        if (folderdata.containsKey("Target.ACL"))
        {
            aclname = (String)folderdata.get("Target.ACL");
        }
        return aclname;
        
    }
    
    public String getFolderTypeName(IDfSession session, Context context, Map folderdata, String foldertype) throws Exception {
        
        if (folderdata.containsKey("Target.Type")) {
            foldertype = (String)folderdata.get("Target.Type");
        } else if (folderdata.containsKey("Source.Type")) {
            String sourcetype = (String)folderdata.get("Source.Type");
            if (!foldertype.equals(sourcetype)) {
                // make sure it is a valid type in the target system
                try {
                    IDfType type = session.getType(sourcetype);
                    if (type != null) {
                        foldertype = sourcetype;
                    }
                } catch (DfException dfe) {
                    // TODO: see what happens if the type isn't kosher                                    
                }
            }
        }
        return foldertype;
        
    }
}
