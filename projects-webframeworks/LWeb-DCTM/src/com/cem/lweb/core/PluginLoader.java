package com.cem.lweb.core;

import java.util.List;

import com.cem.base.EEx;
import com.cem.base.Lg;



// TODO: need to make this extensible, a pseudo-classloader type of thing, so that exotic plugin loads
// like DCTM SBOs and other types can be supported. The default will be a simple class.forName.newInstance

// IDEA: a classloader that loads a class based on configuration, so that if com.company.framework.UNFrameworkClass is requested,
//       but the implementing client wants to override that impl (usually compiled without source), the custom class can 
//       be (uh, injected?) used instead transparently to the code using the class.  
//       i.e. there is a custom class com.client.application.UNFrameworkClass defined by the client
//       - the UN prefix on the base class name indicates that a "universal namespace" should be applied
//       - some config scheme (property files, xml, annotations) that specifies a priority for the custom class
//         indicates it supersedes the base framework definition
//       - problem: what if com.client.app.UNFrameworkClass extends com.company.framework.UNFrameworkClass? ... Ouch ...
//                  ... maybe annotations can indicate that a normal classload should occur when classes extend other classes ...
//                  in yet another way, inheritance SUCKS since mixin composed objects would be perfectly fine here.

// do lazy/fuzzy class matching
// argument validation/verification in config or context?

public class PluginLoader 
{
    public static Object loadPlugin(String classname, Class iface)
    {        
        try { 
            /*-trc-*/Lg.trc("default-instantiate %s",classname);
            Class pluginclass = Class.forName(classname);
            if (iface != null) {
                /*-trc-*/Lg.trc("Validating required interface or class child");
                if (!iface.isAssignableFrom(pluginclass)) {
                	EEx cce = EEx.create("PluginLoadClassCastError","Specified plugin class %s not castable to %s",classname,iface.getName()); 
                    /*-ERROR-*/Lg.err("Specified plugin class %s not castable to %s",classname,iface.getName(),cce);
                	throw cce;
                }
            }
            Object pluginimpl = pluginclass.newInstance();
            /*-trc-*/Lg.trc("Plugin instance created via forName().newInstance()");
            return pluginimpl;                
        } catch (ClassNotFoundException cnfe) {
            /*-ERROR-*/Lg.err("Instantiation of plugin failed - class %s not located", classname,cnfe);
            throw EEx.create("PluginLoader-NotFound", "Instantiation of plugin failed - class %s not located", classname,cnfe);            
        } catch (IllegalAccessException sece) {
            /*-ERROR-*/Lg.err("Access Exception - instantiation of plugin %s not allowed by JAAS or private/protected rules", classname,sece);
            throw EEx.create("PluginLoader-NoAccess", "Access Exception - instantiation of plugin %s not allowed by JAAS or private/protected rules", classname,sece);                        
        } catch (InstantiationException ie) {
            /*-ERROR-*/Lg.err("Instantiation Error - instantiation of plugin %s not allowed, probably due to it being an interface or an abstract class", classname,ie);
            throw EEx.create("PluginLoader-CannotCreate", "Instantiation Error - instantiation of plugin %s not allowed, probably due to it being an interface or an abstract class", classname,ie);                                    
        }
    }

    // TODO: caching (1 map per iface class)  
    public static Object fuzzyLoadPlugin(String classname, Class iface, List packagelist)
    {        
        try {
            /*-trc-*/Lg.trc("explicit-instantiate %s",classname);
            boolean found = false;
        	Class pluginclass = null;
        	try { 
        		pluginclass = Class.forName(classname);
                if (iface != null) {
                    /*-trc-*/Lg.trc("Validating required interface or class child");
                    if (iface.isAssignableFrom(pluginclass)) {
                    	found = true;
                    }
                }
    		} catch (ClassNotFoundException cnfe) {
                /*-trc-*/Lg.trc("explicit lookup of class %s failed",classname);
    		}
    		if (!found) {       
                /*-trc-*/Lg.trc("attempt fuzzy load, iterate on packagelist");
                if (packagelist != null) {
                	for (int i=0; i < packagelist.size(); i++) {
                		String pkg = null;
                		try {
                			pkg = (String)packagelist.get(i);
                			String fullclassname = null;
                			if (!pkg.endsWith(".")) {
                				fullclassname += '.'+classname;
                			} else {
                				fullclassname += classname;
                			}
                			pluginclass = Class.forName(fullclassname);
                            if (iface.isAssignableFrom(pluginclass)) {
                            	found = true;
                            } else {
                                /*-trc-*/Lg.trc("Class %s found in pkg %s, but not of type %s",classname,pkg,iface.getName());                        			
                            }
                		} catch (ClassNotFoundException cnfe) {
                            /*-trc-*/Lg.trc("Class %s not found in pkg %s",classname,pkg);                        			
                		}
                	}
                }
            }
    		if (found) {
	            Object pluginimpl = pluginclass.newInstance();
	            /*-trc-*/Lg.trc("Plugin instance created via forName().newInstance()");
	            return pluginimpl; 
    		} else {
            	EEx cnfe = EEx.create("PluginLoadClassNotFound","Specified plugin class %s not found in given root packages",classname); 
                /*-ERROR-*/Lg.err("PluginLoadClassNotFound","Specified plugin class %s not found in given root packages",classname);
            	throw cnfe;                        	    			
    		}
        } catch (IllegalAccessException sece) {
            /*-ERROR-*/Lg.err("Access Exception - instantiation of plugin %s not allowed by JAAS or private/protected rules", classname,sece);
            throw EEx.create("PluginLoader-NoAccess", "Access Exception - instantiation of plugin %s not allowed by JAAS or private/protected rules", classname,sece);                        
        } catch (InstantiationException ie) {
            /*-ERROR-*/Lg.err("Instantiation Error - instantiation of plugin %s not allowed, probably due to it being an interface or an abstract class", classname,ie);
            throw EEx.create("PluginLoader-CannotCreate", "Instantiation Error - instantiation of plugin %s not allowed, probably due to it being an interface or an abstract class", classname,ie);                                    
        }
    }

}
