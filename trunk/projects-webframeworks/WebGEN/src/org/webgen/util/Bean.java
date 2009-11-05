package org.webgen.util;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.servlet.ServletCategory;
import groovy.util.GroovyScriptEngine;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.webgen.core.GroovyProcess;
import org.webgen.core.WebgenLogger;

public class Bean 
{
	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(Bean.class));		
	
	public static void Blah(Object o)
	{
		int a=1;
		int b=2;
	}
	
	public static Object Create(String classname)
	{
		/*-CFG-*/String m="Create(str)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"instantiating bean "+classname);		
		try { 			
			return Class.forName(classname).newInstance();
		} catch (ClassNotFoundException cnfe) {
			/*-ERR-*/log.err(m+"Class could not be found "+classname,cnfe);
        	RuntimeException re = new RuntimeException("Class could not be found "+classname,cnfe);
        	throw re;			
		} catch (Exception e) {
			/*-ERR-*/log.err(m+"Access or instantiation exception on class "+classname,e);
        	RuntimeException re = new RuntimeException("Access or instantiation exception on class "+classname,e);
        	throw re;						
		}
	}
	
	public static Map Definition(String classname)
	{
		/*-CFG-*/String m="Definition(binding,str)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"loading bean definiton "+classname);		
		// load the bean defintion script for this bean
		// this class isn't supposed to be used outside of WebGEN, 
		// so we'll use the WebGEN GroovyProcess's script engine
		// - probably should rearchitect to make it cleaner...
		/*-TRC-*/if(log.tOn)log.trc(m+"getting GroovyProcess's scripting engine");		
		GroovyScriptEngine gse = GroovyProcess.gse;
		/*-TRC-*/if(log.tOn)log.trc(m+"compiling bean definition script");		
		Date starttime = new Date();
		final Binding binding = new Binding();
		final String script = "beans/"+classname+".groovy"; 
        Closure closure = new Closure(gse) { // funky...
            public Object call() {
                try {
                    return ((GroovyScriptEngine) getDelegate()).run(script, binding);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Date finishtime = new Date();
		/*-TRC-*/if(log.tOn)log.trc(m+"bean definition compile time - "+(finishtime.getTime() - starttime.getTime()));
		/*-TRC-*/if(log.tOn)log.trc(m+"loading bean definition script");		
        starttime = new Date();
		GroovyCategorySupport.use(ServletCategory.class, closure);
		finishtime = new Date();
		/*-TRC-*/if(log.tOn)log.trc(m+"bean definition load time - "+(finishtime.getTime() - starttime.getTime()));
        Map beandefinition = (Map)binding.getVariable("BeanDefinition");
		/*-TRC-*/if(log.tOn)log.trc(m+"returning bean definition");		
        return beandefinition;		
	}

	public static void Init(final Binding binding, Object bean, final String initscript)
	{
		/*-CFG-*/String m="Init(binding,str)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"initializing bean - set bean in binding");
		Object origbean = null;
		if (binding.getVariable("TheBean") != null) origbean = binding.getVariable("TheBean");
		binding.setVariable("TheBean",bean);
		/*-TRC-*/if(log.tOn)log.trc(m+"getting GroovyProcess's scripting engine");
		// yeah, this again...(runs for cover)...
		GroovyScriptEngine gse = GroovyProcess.gse;
		/*-TRC-*/if(log.tOn)log.trc(m+"compiling bean initialization script");		
		Date starttime = new Date();
        Closure closure = new Closure(gse) { // funky...
            public Object call() {
                try {
                    return ((GroovyScriptEngine) getDelegate()).run(initscript, binding);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Date finishtime = new Date();
		/*-TRC-*/if(log.tOn)log.trc(m+"bean initialization compile time - "+(finishtime.getTime() - starttime.getTime()));
		/*-TRC-*/if(log.tOn)log.trc(m+"loading bean initialization script");		
        starttime = new Date();
		GroovyCategorySupport.use(ServletCategory.class, closure);
		finishtime = new Date();
		/*-TRC-*/if(log.tOn)log.trc(m+"cleaning up binding...");	
		if (origbean != null)
			binding.setVariable("TheBean",origbean);
		else
			binding.getVariables().remove("TheBean");
		/*-TRC-*/if(log.tOn)log.trc(m+"all done");	
	}

}
