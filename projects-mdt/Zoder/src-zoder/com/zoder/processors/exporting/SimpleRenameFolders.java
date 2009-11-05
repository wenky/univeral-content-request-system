package com.zoder.processors.exporting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.zoder.main.IProcessor;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;
import com.zoder.util.FolderPathUtils;

// "basic" renaming functionality...
// TODO: overwrite warning detection...

public class SimpleRenameFolders implements IProcessor
{

    public void process(Map script, Context context) throws Exception 
    {
        // get configured map of paths --> newnames
        Map renamings = (Map)context.get("FolderRenames");
        Map folderpaths = (Map)context.get("Folders.FolderPaths");
        Map idmap = (Map)context.get("Folders.IdMap");
               
        // iterate through, and apply changes -- use regexp for additional flexibility
        // regexps CANNOT have /'s in them! So no, you cannot add/expand the paths this way.
        // regexps have the string ::: in front of them...
        // these macros may overwrite each other, we will "warn"
        Map overwritewarning = new HashMap();
        Iterator i = renamings.keySet().iterator();
        int rulenum = 0;
        while (i.hasNext()) {
            String from = (String)i.next();
            String to = (String)renamings.get(from);
            String[] matcharr = FolderPathUtils.normalizePath(from).substring(1).split("/");

            // sizes should match...
            // iterate through from, matching where appropriate
            Iterator p = folderpaths.keySet().iterator();
            while (p.hasNext()) {
                String path = (String)p.next();
                String[] patharr = path.substring(1).split("/");
                if (patharr.length == matcharr.length) {
                    boolean match = true;
                    for (int z=0; z<patharr.length; z++) {
                        String zpath = patharr[z];
                        String zmatch = matcharr[z];
                        if (zmatch.length() >=3 && ":::".equals(zmatch.substring(0,3))) {
                            if (!zpath.matches(zmatch.substring(3))) {
                                match = false;
                                z = patharr.length;
                            } 
                        } else { // string match
                            if (!zpath.equals(zmatch)) {
                                match = false;
                                z = patharr.length;
                            }
                        }
                    }
                    if (match) {
                        // apply the change to the source folder
                        String id = (String)folderpaths.get(path);
                        overwritewarning.put(id,rulenum);
                        Map folder = (Map)idmap.get(id);
                        String oldname = (String)folder.get("ObjectName");
                        Map target = CollUtils.getMap(folder, "Target");
                        if (to.length() >=3 && ":::".equals(to.substring(0,3))) {
                            // regex!
                            String[] regex = to.substring(3).split("/");
                            String newname = oldname.replaceAll(regex[0], regex[1]);
                            target.put("ObjectName", newname);
                        } else {
                            target.put("ObjectName", to);
                        }
                            
                    }
                }
            }
            rulenum++;
        }
        
    }
    

}
