package com.zoder.processors.exporting;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.thoughtworks.xstream.XStream;
import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;

public class ExportFolderInfo implements IProcessor
{
    
    public void process(Map script, Context context) throws Exception
    {
        DctmAccess access = (DctmAccess)context.getNoNull("SourceAccess");
        
        IDfSession session = null;
        try {
            session = access.getSession();                                    
            serializeFolderData(session,context);
        
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
    }

    public void serializeFolderData(IDfSession session, Context context) throws Exception
    {
        Map pathmap   = (Map)context.get("Folders.FolderPaths");    // folderpaths --> folder data
        Map idpathmap = (Map)context.get("Folders.IdChains");  // id path --> folder data
        Map idmap     = (Map)context.get("Folders.IdMap");      // id --> folder data

        XStream xs = new XStream();
        String basedir = (String)context.get("BaseDirectory");

        Map outputidmap = (Map)new HashMap(idmap.size());
        Iterator i = idmap.keySet().iterator();
        while (i.hasNext()) {
            String id = (String)i.next();
            Map map = (Map)idmap.get(id);
            // strip out the runtime info
            Map targetdata = (Map)map.get("Target");            
            outputidmap.put(id, targetdata);
        }
        xs.toXML(outputidmap, new FileWriter(new File(basedir+"Folders.IdMap.xml")));
        xs.toXML(context.get("Folders.IdChains"), new FileWriter(new File(basedir+"Folders.IdChains.xml")));
        xs.toXML(context.get("Folders.FolderPaths"), new FileWriter(new File(basedir+"Folders.FolderPaths.xml")));
                
        // probably not used in import formally, this is more of a human-readable reference
        List folderlist = new ArrayList(pathmap.keySet());                
        xs.toXML(folderlist, new FileWriter(new File(basedir+"Folders.FolderList.xml")));        
    }

}
