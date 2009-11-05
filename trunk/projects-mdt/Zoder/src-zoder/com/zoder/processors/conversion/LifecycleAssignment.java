package com.zoder.processors.conversion;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;

public class LifecycleAssignment extends AbstractVersionProcessor 
{

    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
                
            String lcname = getLifecycleName(session,document,version,context,so);
            String lcstate = getLifecycleState(session,document,version,context,so);
            String lcalias = getLifecycleAlias(session,document,version,context,so);
            
            IDfSysObject lifecycle = (IDfSysObject)session.getObjectByQualification("dm_policy where object_name = '"+lcname+"'");
            
            so.attachPolicy(lifecycle.getObjectId(), lcstate, lcalias);
            so.save();
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
    }
    
    public String getLifecycleName(IDfSession session, Map document, Map version, Map context, IDfSysObject so)throws Exception
    {
        Map mappings = (Map)context.get("LifecycleMap");
        String mapkey = getLifecycleMapReference(session,document,version,context,so);
        if (mappings != null) {
            String lcname = (String)mappings.get(mapkey);
            return lcname;
        } else {
            return mapkey;
        }
    }
    
    public String getLifecycleState(IDfSession session, Map document, Map version, Map context, IDfSysObject so)throws Exception
    {
        Map mappings = (Map)context.get("StateMap");
        String mapkey = getStateMapReference(session,document,version,context,so);
        if (mappings != null) {
            String statename = (String)mappings.get(mapkey);
            return statename;        
        } else {
            return mapkey;
        }
    }

    public String getLifecycleAlias(IDfSession session, Map document, Map version, Map context, IDfSysObject so)throws Exception
    {
        Map mappings = (Map)context.get("AliasMap");
        String mapkey = getAliasMapReference(session,document,version,context,so);
        if (mappings != null) {
            String aliasname = (String)mappings.get(mapkey);
            return aliasname;        
        } else {
            return mapkey;
        }
    }

    public String getLifecycleMapReference(IDfSession session, Map document, Map version, Map context, IDfSysObject so) throws Exception
    {
        return so.getTypeName();
    }

    public String getStateMapReference(IDfSession session, Map document, Map version, Map context, IDfSysObject so) throws Exception
    {
        return so.getACLName();
    }

    public String getAliasMapReference(IDfSession session, Map document, Map version, Map context, IDfSysObject so) throws Exception
    {
        return so.getAliasSet();
    }

}
