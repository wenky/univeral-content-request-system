package com.medtronic.ecm.documentum.introspection;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class GroovyIntrospection 
{
    public static GroovyScriptEngine gse = null;
    
    public static void initGroovy(HttpServletRequest httpreq) throws IOException
    {
        if (gse != null) return;
        String resourcepath = httpreq.getScheme()+"://"+httpreq.getServerName()+":"+httpreq.getServerPort()+httpreq.getContextPath()+"/introspection/groovy/";
        gse = new GroovyScriptEngine(resourcepath);

    }
    
    public static String callGroovy(String script, Binding binding) throws ResourceException, ScriptException
    {        
        StringWriter strout = new StringWriter();
        binding.setVariable("out", strout);
        binding.setVariable("scriptbinding",binding);
        gse.run(script, binding);
        return strout.toString();
    }

    public static String include(String script, Map bindings) throws ResourceException, ScriptException
    {        
        StringWriter strout = new StringWriter();
        Binding binding = createBindingFromMap(bindings);
        binding.setVariable("out", strout);
        binding.setVariable("scriptbinding",binding);
        gse.run(script, binding);
        return strout.toString();
    }


    public static Binding createBindingFromMap(Map bindings)
    {
        Binding b = new Binding();
        Iterator i = bindings.keySet().iterator();
        while (i.hasNext()){
            String key = (String)i.next();
            b.setVariable(key, bindings.get(key));
        }
        return b;        
    }

    public static Binding createBinding(Object... args)
    {
        Binding b = new Binding();
        for (int i=0; i < args.length/2; i++) {
            String key = (String)args[i*2];
            Object value = args[i*2+1];
            b.setVariable(key, value);
        }
        return b;        
    }


}
