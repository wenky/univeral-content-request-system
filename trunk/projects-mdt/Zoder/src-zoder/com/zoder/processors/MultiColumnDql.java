package com.zoder.processors;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;
import com.zoder.util.DctmUtils;

public class MultiColumnDql implements IProcessor 
{

    public void process(Map script, Context context) throws Exception 
    {
        String accesskey = context.containsKey("AccessKey")?(String)context.get("AccessKey"):"SourceAccess";
        String targetkey = (String)context.get("OutputKey");
        String dql = (String)context.get("Dql");
        
        DctmAccess access = (DctmAccess)context.get(accesskey);
        IDfSession session = null;
        try {
            session = access.getSession();
            List results = DctmUtils.execMultiColumnQuery(session,dql); // actually a ResultSetList in case we need the column names
            context.put(targetkey,results);            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
    }

}
