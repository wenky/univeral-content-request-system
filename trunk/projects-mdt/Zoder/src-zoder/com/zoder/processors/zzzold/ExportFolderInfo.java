package com.zoder.processors.zzzold;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.IDfId;
import com.thoughtworks.xstream.XStream;
import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;
import com.zoder.util.DctmUtils;

// CEM: Very impressive. Also now, totally useless...

// export a list of folders with linking instructions, metadata, reference data, 
// and sorted in an order such that all folders a folder links to are guaranteed 
// to be created/converted/etc in the target system, if created in the given order

public class ExportFolderInfo implements IProcessor
{
    
    
    public void process(Map script, Context context) throws Exception
    {
        DctmAccess access = (DctmAccess)context.getNoNull("SourceAccess");
        
        IDfSession session = null;
        try {
            session = access.getSession();
            
            compileAllFolders(session,context);
                        
            serializeFolderData(session,context);
        
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
    }

    public void serializeFolderData(IDfSession session, Context context) throws Exception
    {
        Map pathmap   = (Map)context.get("FolderPathMap");    // folderpaths --> folder data
        Map idpathmap = (Map)context.get("FolderIdPathMap");  // id path --> folder data
        Map idmap     = (Map)context.get("FolderIdMap");      // id --> folder data
        
        // pathmap was treemapped, so that would theoretically give us a good initial sort for dependency ordering
        List folderlist = new ArrayList();
        Map foldermap = new HashMap();
        List waiting = new ArrayList();
        
        // use this to track dependencies when looping through the waiting queue: 
        // - if the folder is linked to an id not yet in this set, it needs to go to waiting...
        // - if the folder id is already in this set (paths are many to one with linking), then ignore it
        Set idtracker = new HashSet();   
        
        // initial population of waiting queue
        {
            Iterator i = pathmap.keySet().iterator();
            int j=0;
            while (i.hasNext()) {
                String curpath = (String)i.next();
                waiting.add(curpath);
            }
        }
        
        // perform one or more passes of the waiting queue until we have a proper creation/scan order for the folders
        while (true) {
            // loop exit condition
            if (waiting.size() == 0) break;
            
            // scan current waiting list...
            List stillwaiting = new ArrayList();
            for (int i=0; i < waiting.size(); i++) {
                String curpath = (String)waiting.get(i);
                Map pathinfo = (Map)pathmap.get(curpath);
                String folderid = (String)pathinfo.get("ObjectId");
                if (!idtracker.contains(folderid)) {
                    Set links = (Set)pathinfo.get("Links");
                    if (links == null || links.size() == 0) {
                        addToFolderList(folderid,pathinfo,folderlist,idtracker);
                    } else if (idtracker.containsAll(links)) {
                        addToFolderList(folderid,pathinfo,folderlist,idtracker);
                    } else {
                        // back of the line, honey
                        stillwaiting.add(curpath);
                    }
                }
            }
            waiting = stillwaiting;
        }
        
        XStream xs = new XStream();
        String basedir = (String)context.get("BaseDirectory");
        xs.toXML(folderlist, new FileWriter(new File(basedir+"FolderCreationList.xml")));
        
    }
    
    public void addToFolderList(String folderid,Map pathinfo, List folderlist, Set idtracker) throws Exception
    {
        idtracker.add(folderid);
        Map map = new HashMap();
        folderlist.add(map);
        
        IDfFolder obj = (IDfFolder)pathinfo.get("folderobj");
        Set linkfolders = (Set)pathinfo.get("Links");

        // reference data
        map.put("ObjectId", obj.getObjectId().getId()); // f*%&, what if they version folders???
        map.put("Name", obj.getObjectName());
        map.put("Links", linkfolders); // folders linked to        
        
        // TODO reference, metadata, remap, etc etc etc
    }
    
    public void compileAllFolders(IDfSession session, Context context) throws Exception
    {
        Map pathmap = new TreeMap();    // folderpaths --> folder data
        Map idpathmap = new HashMap();  // id path --> folder data
        Map idmap = new HashMap();      // id --> folder data
        context.put("FolderPathMap", pathmap);
        context.put("FolderIdPathMap", idpathmap);
        context.put("FolderIdMap", idpathmap);
        
        // add user-specified ones to set in FolderNames
        // user-specified may be via query via SingleColumnDql, hardcoded, etc
        // NOTE: user-specified MUST HAVE CABINET as the first folder path!
        
        Map folderinfos = (Map)context.get("FolderNames");
        Set folders = new HashSet(folderinfos.keySet());
        List configlist = (List)context.get("FolderList");
        if (configlist != null) {
            folders.add(configlist);
        }
        Iterator i = folders.iterator();        
        while (i.hasNext()) {
            String foldername = (String)i.next();
            // split path to individual folders
            String[] parsedfolderpath = null;
            if (foldername.charAt(0) == '/' && foldername.charAt(foldername.length()-1) == '/') {
                parsedfolderpath = foldername.substring(1,foldername.length()-1).split("/");
            } else if (foldername.charAt(0) == '/') {
                parsedfolderpath = foldername.substring(1).split("/");                
            } else if (foldername.charAt(foldername.length()-1) == '/') {
                parsedfolderpath = foldername.substring(0,foldername.length()-1).split("/");                                
            } else {
                parsedfolderpath = foldername.split("/");                                
            }
            if (parsedfolderpath != null) {                    
                // check through each folder path's constituent folders to make sure 
                // we get all the folders and links represented by all of them.
                String curpath = "/";   // map of these may be many-to-less
                String curidpath = ""; // map of these may be many-to-less
                String curid = "";     // map of these will be 1-1
                boolean first = true;
                for (int f=0; f < parsedfolderpath.length; f++)
                {
                    String curname = parsedfolderpath[f];
                    String lastpath = curpath;
                    String lastid = curid;
                    String lastidpath = curidpath;
                    curpath += parsedfolderpath[f];
                    IDfSysObject fldobj = null;
                    if (pathmap.containsKey(curpath)) {
                        // lookup folder object with previously retrieved path
                        Map pmap = (Map)pathmap.get(curpath);
                        fldobj = (IDfSysObject)pmap.get("folderobj");
                    } else {
                        // lookup folder via path
                        fldobj = session.getFolderByPath(curpath); 
                        if (fldobj == null) {
                            if (f==0) {
                                // probably a cabinet, which doesn't seem to work with folder lookups
                                String dql = "SELECT r_object_id FROM dm_cabinet WHERE object_name = '"+curname+"'";
                                IDfId cabinetid = DctmUtils.execSingleValueQuery(session, dql).asId();
                                fldobj = (IDfSysObject)session.getObject(cabinetid);
                            } else {
                                // not found...that's weird - TODO - log warn? error? do more poking around? 
                                ;
                            }
                            
                        }
                    }
                    curid = fldobj.getObjectId().getId();
                    if (first) first=false; else curidpath += '-';
                    curidpath+=curid;
                    
                    Map folderdata = new HashMap();
                    if (idmap.containsKey(curid)) {
                        folderdata = (Map)idmap.get(curid);
                        Map linkpaths = (Map)folderdata.get("Paths");
                        linkpaths.put(curpath,curidpath);
                        if (!"".equals(lastid)) {
                            Set linkfolders = (Set)folderdata.get("Links");
                            linkfolders.add(lastid);
                        }
                    } else {
                        folderdata = new HashMap();
                        idmap.put(curid, folderdata);
                        folderdata.put("folderobj", fldobj);
                        folderdata.put("Name", curname);
                        folderdata.put("ObjectId", curid);
                        Map linkpaths = new HashMap();
                        linkpaths.put(curpath,curidpath);
                        folderdata.put("Paths", linkpaths);
                        Set linkfolders = new HashSet();
                        if (!"".equals(lastid)) {
                            linkfolders.add(lastid);
                        }
                        folderdata.put("Links", linkfolders);
                    }
                    pathmap.put(curpath, folderdata);
                    idpathmap.put(curidpath, folderdata);
                    curpath+='/';
                }                    
            }
        }
    }
    
}
