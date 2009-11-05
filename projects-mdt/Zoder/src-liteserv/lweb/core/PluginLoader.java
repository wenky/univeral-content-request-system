package lweb.core;

import lbase.EEx;
import lbase.Lg;
import lweb.core.structs.Plugin;


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
    public static Object loadPlugin(String classname, Object...args)
    {        
        try { 
            /*-trc-*/Lg.trc("default-instantiate %s",classname);
            Object pluginimpl = Class.forName(classname).newInstance();
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

    public static Object loadPlugin(Plugin plugin, Object...args)
    {
        try { 
            /*-trc-*/Lg.trc("default-instantiate %s",plugin.Class);
            Object pluginimpl = Class.forName(plugin.Class).newInstance();
            /*-trc-*/Lg.trc("Plugin instance created via forName().newInstance()");
            return pluginimpl;                
        } catch (ClassNotFoundException cnfe) {
            /*-ERROR-*/Lg.err("Instantiation of plugin failed - class %s not located", plugin.Class,cnfe);
            throw EEx.create("PluginLoader-NotFound", "Instantiation of plugin failed - class %s not located", plugin.Class,cnfe);            
        } catch (IllegalAccessException sece) {
            /*-ERROR-*/Lg.err("Access Exception - instantiation of plugin %s not allowed by JAAS or private/protected rules", plugin.Class,sece);
            throw EEx.create("PluginLoader-NoAccess", "Access Exception - instantiation of plugin %s not allowed by JAAS or private/protected rules", plugin.Class,sece);                        
        } catch (InstantiationException ie) {
            /*-ERROR-*/Lg.err("Instantiation Error - instantiation of plugin %s not allowed, probably due to it being an interface or an abstract class", plugin.Class,ie);
            throw EEx.create("PluginLoader-CannotCreate", "Instantiation Error - instantiation of plugin %s not allowed, probably due to it being an interface or an abstract class", plugin.Class,ie);                                    
        }
        
    }

}
