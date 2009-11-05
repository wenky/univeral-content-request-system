package com.medtronic.ecm.documentum.mdtworkflow.method;

import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.Lg;

public class MethodUtils 
{    
    
    // Assumes docbase has been configured for trusted login for method server actions
    // Known to work on Mdt Sun servers, Linux and Windows are not verified yet 
    public static IDfSessionManager doTrustedLogin(String docbase) throws Exception
    {
        /*-wrn-*/Lg.wrn("Perform trusted authentication");
        String sUserName = System.getProperty("user.name");
        /*-dbg-*/Lg.dbg("username sysprop: %s",sUserName);
        IDfClientX clientx = new DfClientX();
        IDfLoginInfo li = clientx.getLoginInfo();
        li.setDomain("");
        li.setUser(sUserName);
        li.setPassword("");
        IDfClient client = clientx.getLocalClient();
        IDfSessionManager sessionMgr = client.newSessionManager();
        sessionMgr.setIdentity(docbase, li);
        return sessionMgr;
    }

    // Traditional non-trusted means of getting docbase auth in a method:
    // - assumes the user, ticket, and docbase are provided in the method call (true for workflow methods, but not necessarily for jobs)    
    public static IDfSessionManager getUserSessionManager(Map parameters) throws Exception
    {
        /*-dbg-*/Lg.wrn("getting necessary data from parameters map" );
        String user = ((String[])parameters.get("user"))[0];
        String ticket = ((String[])parameters.get("ticket"))[0];
        String docbase = ((String[])parameters.get("docbase_name"))[0];
        /*-dbg-*/Lg.wrn("  --user: %s  docbase: %s  ticket: %s",user,docbase,ticket);

        /*-dbg-*/Lg.wrn("getting local client");
        IDfClient client = DfClient.getLocalClient();
        IDfSessionManager sMgr = null;
        DfLoginInfo loginInfo = new DfLoginInfo();
        
        /*-dbg-*/Lg.wrn("setting logininfo identity");
        loginInfo.setUser(user);
        loginInfo.setPassword(ticket);
        
        /*-dbg-*/Lg.wrn("getting session");
        sMgr = client.newSessionManager();
        sMgr.setIdentity(docbase,loginInfo);
        
        /*-dbg-*/Lg.wrn("done");
        return sMgr;
    }

    public static String getSingleValueParameter(Map params, String key)
    {
        String[] values;
        String value = null;
        //get the parameter value array from the parameters map
        values = (String[]) params.get(key);
        if( values != null && values.length > 0)
        {
            value = values[0];
        }
        return value;
    }
    
    

}
