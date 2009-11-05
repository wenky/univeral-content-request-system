package com.zoder.processors;

import java.util.Map;

import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;

public class InitDctmAccess implements IProcessor 
{

    public void process(Map script, Context context) throws Exception 
    {
        String user = (String)context.get("user");
        String pass = (String)context.get("pass");
        String base = (String)context.get("base");
        String contextkey = (String)context.get("contextkey");
        
        DctmAccess dctmaccess = new DctmAccess(user,pass,base);
        
        context.put(contextkey, dctmaccess);
        
    }

}
