package com.zoder.processors.exporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.util.Context;

// This assumes a well-behaved folder structure has already been setup and doesn't need our nannying


public class LinkedPaths extends AbstractVersionProcessor 
{
    public void processVersion(DctmAccess access, Map script,Context context, Map document, Map version, int veridx) throws Exception {
        IDfSession session = null;
        boolean mustlink = context.containsKey("MustBeLinked");
        
        List excludelist = context.getList("Exclude");
        List includelist = context.getList("Include");
        try {
            session = access.getSession();
                        
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            boolean hasfolder = false;
            List validfolders = new ArrayList();
            for (int i=0; i < so.getFolderIdCount(); i++)
            {
                IDfFolder folder = (IDfFolder)session.getObject(so.getFolderId(0));
                for (int ii=0; ii < folder.getFolderPathCount(); ii++)
                {
                    boolean include = false;
                    String folderpath = folder.getFolderPath(ii);
                    if (includelist != null) {
                        for (int inc = 0; inc < includelist.size(); inc++) {
                            Object o = includelist.get(inc);
                            if (o instanceof String) {
                                String basedirstr = (String)o;
                                if (folderpath.length() > basedirstr.length()) { 
                                    if (folderpath.startsWith(basedirstr)) {
                                        include = true;
                                        break; // break from include scan
                                    }
                                }
                            } else if (o instanceof Pattern) {
                                Pattern p = (Pattern)o;
                                if (p.matcher(folderpath).matches()) {
                                    include = true;
                                    break; // break from include scan
                                }
                            }
                        }
                    } else {
                        include = true;
                    }
                    
                    if (include && excludelist != null) {
                        for (int ex = 0; ex < excludelist.size(); ex++) {
                            Object o = excludelist.get(ex);
                            if (o instanceof String) {
                                String basedirstr = (String)o;
                                if (folderpath.length() > basedirstr.length()) { 
                                    if (folderpath.startsWith(basedirstr)) {
                                        include = false;
                                        break; // break from exclude scan
                                    }
                                }
                            } else if (o instanceof Pattern) {
                                Pattern p = (Pattern)o;
                                if (p.matcher(folderpath).matches()) {
                                    include = false;
                                    break; // break from exclude scan
                                }
                            }
                        }
                        
                    }
                    if (include) {
                        validfolders.add(folderpath);
                        hasfolder =  true;
                    }
                }
            }
            
            if (mustlink) {
                if (!hasfolder ) {
                    // uhoh, all the folders it linked to were excluded or not included                    
                    throw new DocumentError("LinkedPaths - All linked folders were excluded for version: "+(version!=null&&version.containsKey("r_object_id")?version.get("r_object_id"):null),document);
                }
            }
            
            version.put("FolderPaths",validfolders);                
                
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }
        
        
    }

}
