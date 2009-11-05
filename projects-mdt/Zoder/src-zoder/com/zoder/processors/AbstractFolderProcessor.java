package com.zoder.processors;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.struct.ErrorDetail;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;

public abstract class AbstractFolderProcessor implements IProcessor {

    public void process(Map script, Context context) throws Exception 
    {
        String accesskey = (String)context.get("AccessKey");
        if (accesskey == null)accesskey = "SourceAccess";
        DctmAccess access = (DctmAccess)context.get(accesskey);
        
        
        Map idmap = CollUtils.getMap(context,"Folders.IdMap");
        
        Iterator i = idmap.keySet().iterator();
        while (i.hasNext()) 
        {
            String folderid = (String)i.next();
            
            Map folder = (Map)idmap.get(folderid);
            if (folder == null) {
                /*log a warning*/;
            } else {
                processFolder(access,script,context,folder,folderid);
            }
        }        
    }
    
    public abstract void processFolder(DctmAccess access, Map script, Context context, Map folder, String folderid) throws Exception;
    
}
