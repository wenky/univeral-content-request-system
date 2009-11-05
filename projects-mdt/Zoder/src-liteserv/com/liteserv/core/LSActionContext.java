package com.liteserv.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// implements a complicated subset of hashmap behavior to simplify the process of getting config and context information in 
// progit pipelines. The action context for a progit consists of the shared hashmap (which can be written to), the config
// hashmap as defined in the progit config in xml (read only), and the arguments hashmap which is constructed at runtime, 
// but does not pollute the shared progit context pipeline hashmap.

// essentially, what we want is a get call that will check config, arguments, and context for information, but a put that
// places values in context. this may be very useful for velocity templates, since all the items are amalgamated for 
// substitution 

public class LSActionContext
{
    protected Map arguments = null;
    protected Map config = null;
    protected Map context = new HashMap();
    
    void setArgumentsMap(Map map) {arguments = map;}
    void setConfigMap(Map map) {config = map;}
    void setContextMap(Map map) {context = map;}
    
    protected String redirectaction = null;    
    public void setRedirect(String newredirectaction)
    {
        redirectaction = newredirectaction;
    }
    
    public Object get(Object key) {
        // priority: arguments, then config, then context
        if (arguments != null && arguments.containsKey(key)) {
            return arguments.get(key);
        } 
        if (config != null && config.containsKey(key)) {
            return config.get(key);
        } 
        if (context != null && context.containsKey(key)) {
            return context.get(key);
        } 
        return null;
    }
    
    public Object put(Object key, Object value)
    {
        return context.put(key, value);
    }
    
    public Object remove(Object key)
    {
        return context.remove(key);
    }
    
    public Set keySet()
    {
        // expensive...amalgamate the keys into one set. context keys will be overwritten by config and arguments keys if there are duplicates
        HashSet keys = new HashSet(context.keySet());
        keys.addAll(config.keySet());
        keys.addAll(arguments.keySet());
        return keys;
    }
    
    public boolean containsKey(Object key)
    {
        if (arguments != null && arguments.containsKey(key)) {
            return true;
        } 
        if (config != null && config.containsKey(key)) {
            return true;
        } 
        if (context != null && context.containsKey(key)) {
            return true;
        } 
        return false;
    }

}
