package com.zoder.processors.exporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;
import com.zoder.util.FolderPathUtils;

public class ScanFolderStructure implements IProcessor 
{
    public void process(Map script, Context context) throws Exception 
    {
        DctmAccess access = (DctmAccess)context.getNoNull("SourceAccess");
        // get list of cabinets to descend (this is the initial search seed list, not a filter)
        // ...technically, does this need to be a cabinet list, roots could be full folder paths
        // ...must begin with /, not end in /, but I guess we can check...
        List rootlist = (List)context.getNoNull("PathList");
        // descend cabinets: convert to folderpaths, idchains, and master id reference map
        Map folderpathmap = new TreeMap();
        Map idchainmap = new HashMap();
        Map idmap = new HashMap();

        IDfSession session = null;
        try {
            session = access.getSession();

            // begin initial load
            for (int i=0; i < rootlist.size(); i++) {
                String rootpath = FolderPathUtils.normalizePath((String)rootlist.get(i));
                IDfFolder[] rootobjects = FolderPathUtils.pathToFolderArray(session, rootpath);
                String curpath = "";
                String curchain = "";
                
                // in case some of the folder paths aren't just cabinets, add all referenced folders
                // to the lookups. However, these will not be descended...
                for (int f=0; f < rootobjects.length; f++)
                {
                    IDfFolder fldobj = rootobjects[f];
                    String curid = fldobj.getObjectId().getId();
                    String curname = fldobj.getObjectName(); 
                    String curtype = fldobj.getTypeName(); 
                    curpath += '/'+curname;
                    if (f == 0) 
                        curchain += curid;
                    else 
                        curchain += '-'+curid;
                    
                    folderpathmap.put(curpath,curid);
                    idchainmap.put(curpath,curchain);
                    if (!idmap.containsKey(curid)) {
                        Map map = new HashMap();
                        map.put("ObjectId",curid);
                        map.put("ObjectName", curname);
                        map.put("ObjectType", curtype);
                        idmap.put(curid, map);
                    }
                }
                // curpath should == rootpath now...
                
                // descend the folder
                processSubfolders(session,curpath,curchain,rootobjects[rootobjects.length-1],folderpathmap,idchainmap,idmap);
            }
            
            // Postprocess folder links to try to ensure primary/secondary link order on the folders when recreated
            Iterator it = idmap.keySet().iterator();
            while (it.hasNext()) {
                String id = (String)it.next();
                Map map = (Map)idmap.get(id);
                IDfFolder fldobj = (IDfFolder)session.getObject(new DfId((String)map.get("ObjectId")));                
                List linkorder = new ArrayList();
                map.put("LinkOrder", linkorder);
                for (int f=0; f < fldobj.getFolderIdCount(); f++)
                {
                    String curid = fldobj.getFolderId(f).getId();
                    if (idmap.containsKey(curid)) {
                        linkorder.add(curid);
                    }
                }
            }

        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
        context.put("Folders.FolderPaths", folderpathmap);
        context.put("Folders.IdChains", idchainmap);
        context.put("Folders.IdMap", idmap);
        
    }
    
    public void processSubfolders(IDfSession session, String curpath, String curchain, IDfFolder fldobj, Map folderpaths, Map idchains, Map ids) throws DfException
    {
        // iterate through the folders
        IDfCollection contents = null;
        try {
            contents = fldobj.getContents("object_name, r_object_type, r_object_id");
            while (contents.next()) {
                IDfId subid = contents.getId("r_object_id");
                String subname = contents.getString("object_name");
                String subtype = contents.getString("r_object_type");
                if (FolderPathUtils.isFolderType(subid.getId())) {
                    String subpath = curpath+'/'+subname;
                    String subchain = (curchain.length() > 0)?curchain+'-'+subid.getId():subid.getId();
                    IDfFolder subobj = FolderPathUtils.getFolderObject(session, subpath);
                    folderpaths.put(subpath,subid.getId());
                    idchains.put(subpath, subchain);
                    if (!ids.containsKey(subid.getId())) {
                        Map map = new HashMap();
                        map.put("ObjectId",subid.getId());
                        map.put("ObjectName", subname);
                        map.put("ObjectType", subtype);
                        ids.put(subid.getId(), map);
                    }
                    processSubfolders(session,subpath,subchain,subobj,folderpaths,idchains,ids);
                }
            }
        } finally {
            try {contents.close();}catch(Exception e){}
        }
    }
}
