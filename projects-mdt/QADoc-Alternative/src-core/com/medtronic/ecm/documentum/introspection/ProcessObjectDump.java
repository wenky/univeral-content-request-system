package com.medtronic.ecm.documentum.introspection;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;

public class ProcessObjectDump {

    public static String dump(DctmAccess access,String baseurl,String credurl,String id) throws DfException
    {
        IDfSession session = access.accessSession();
        IDfPersistentObject t = session.getObject(new DfId(id));
        String dump = t.dump();
        
        dump = dump.replaceAll(" ","&nbsp;");
        dump = dump.replaceAll("\n","<br>");
        
        // try to process id attributes into links
        IDfPersistentObject perobj = session.getObject(new DfId(id));
        String baselink = "<a href=\""+baseurl+"?do=doDumpObjectById.gvw&"+credurl;             
        for (int i=0; i < perobj.getAttrCount(); i++)
        {
            IDfAttr attr = perobj.getAttr(i);
            if (attr.getDataType() == IDfAttr.DM_ID)
            {
                if (attr.isRepeating())
                {
                    for (int j=0; j < perobj.getValueCount(attr.getName()); j++)
                    {
                        IDfId anid = perobj.getRepeatingId(attr.getName(),j);
                        if (!anid.getId().equals("0000000000000000")) {
                            String search = "&nbsp;"+anid.getId();
                            String repl = "&nbsp;"+baselink+"&id="+anid.getId()+"\">"+anid.getId()+"</a>";
                            int pos = dump.indexOf(search);
                            if (pos != -1)
                            {
                                dump = dump.substring(0,pos) + repl + dump.substring(pos+search.length()); 
                            }
                        }
                    }
                }
                else
                {
                    IDfId anid = perobj.getId(attr.getName());
                    if (!anid.getId().equals("0000000000000000")) {
                        String search = "&nbsp;"+anid.getId();
                        String repl = "&nbsp;"+baselink+"&id="+anid.getId()+"\">"+anid.getId()+"</a>";
                        int pos = dump.indexOf(search);
                        if (pos != -1)
                        {
                            dump = dump.substring(0,pos) + repl + dump.substring(pos+search.length()); 
                        }
                    }
                }
            }
        }
        return dump;
        
    }
    
}
