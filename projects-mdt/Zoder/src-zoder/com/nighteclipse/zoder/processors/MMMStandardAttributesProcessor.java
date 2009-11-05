package com.nighteclipse.zoder.processors;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.nighteclipse.zoder.util.DOMUtils;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;

public class MMMStandardAttributesProcessor extends AbstractVersionProcessor
{

    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            mapStandardAttrs(version,so,context);
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
    }
    
    void mapStandardAttrs(Map version, IDfSysObject so, Context context) throws Exception
    {
        Element curobj = (Element)version.get("DOM");
        DOMUtils.put(curobj,"objectName",so.getObjectName());
        DOMUtils.put(curobj,"objectType",translate(so.getTypeName(),"r_object_type",context));
        DOMUtils.put(curobj,"vaultRef",translate(so.getSession().getDocbaseName(),"docbase_name",context));
        
        Node ownerElement = DOMUtils.getmake(curobj,"owner");
        DOMUtils.put(ownerElement,"userRef",so.getOwnerName());
        IDfFolder folder = (IDfFolder)so.getSession().getObject(so.getFolderId(0));
        String dctmpath = folder.getFolderPath(0);
        DOMUtils.put(curobj,"policyRef",translate(so.getACLName(),"acl_name",context));
    }

    String translate(String value, String attrname, Context config) throws Exception
    {
        
        // attempt a value lookup table translation
        if (config.containsKey(attrname))
        {
            Map table = (Map)config.get(attrname);
            if (table.containsKey(value))
            {
                return (String)table.get(value);
            }
        }
        // not found? return original value (? or throw exception?)
        return value;
        
    }
    


}
