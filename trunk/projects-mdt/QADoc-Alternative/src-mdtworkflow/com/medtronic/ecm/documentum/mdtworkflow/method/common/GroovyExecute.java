package com.medtronic.ecm.documentum.mdtworkflow.method.common;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.util.DctmUtils;

// groovy is more complicated that velocity...
// the goal is to avoid downloading and recompiling the code each time.
// ideally:
// - script request is made by calling getScript() which returns the groovy script to execute locally 
// - check if the tracker has it. if not, getfile and cache the script and version
// - if it has been downloaded, check the version in the cache against the version in DCTM
// - if it has changed, refetch the content to the same file
// - invoke the script, GroovyScriptEngine should compile and cache the compilation 

public class GroovyExecute 
{
    public static Map scriptcache = new HashMap();
    public static GroovyScriptEngine gse = null;
    public static String cacheroot = null;
    
    public static synchronized void lazyInitializeGSE()
    {
        File baseroot = new File("testfile");
        String scriptpath = baseroot.getAbsolutePath();
        scriptpath = scriptpath.substring(0,scriptpath.lastIndexOf(File.separatorChar));                
        
        String theroot = scriptpath + File.separatorChar + "mdtgroovyscriptcache";
        File cachedir = new File(theroot);
        int i=0; 
        while (cachedir.exists()) {
            theroot = scriptpath + File.separatorChar + "mdtgroovyscriptcache"+i;
            cachedir = new File(theroot);
            i++;
        }
        cacheroot = theroot;
        cachedir.mkdir();
        
        String[] roots = new String[1];
        roots[0] = cacheroot;
        try { 
            gse = new GroovyScriptEngine(roots);
        } catch (IOException ioe) {
            /*-ERROR-*/Lg.err("GroovyScriptEngine instantiation IOException on root %s",roots[0],ioe);
            throw EEx.create("GroovyInitialization", "GroovyScriptEngine instantiation IOException on root %s",roots[0],ioe);                         
        }        
    }
    
    /**
     * 
     * @param session
     * @param templatepath
     * @return
     * @throws DfException
     * 
     * This method performs the work necessary to translate a DCTM script request into an actual
     * local script that can be executed, ideally in a compiled fashion if it is a script that 
     * has already run and the DCTM object has not been updated.
     * 
     * For a given script request, it checks the cache to see if it has already been downloaded,
     * checks if it has been updated/changed in documentum and redownloads it if necessary, and
     * returns the script name (which is varied if there are several scripts named the same) for
     * GroovyScriptEngine can execute. 
     */
    
    public static String getScript(IDfSession session, String templatepath) throws DfException
    {
        if (gse == null) {                    
            lazyInitializeGSE();
        }        
        String execfile = null;
        if (scriptcache.containsKey(templatepath)) {
            String[] cacheentry = (String[])scriptcache.get(templatepath);
            IDfSysObject template = (IDfSysObject)session.getObjectByPath(templatepath);
            String id = template.getObjectId().getId(); 
            if (!id.equals(cacheentry[0])) {
                // cache is stale, get newer version in DCTM
                String scriptfilename = cacheentry[1];
                String newscriptcontent = DctmUtils.loadFileContents(session, templatepath);
                
                File scriptfile = new File(cacheroot + File.separatorChar + scriptfilename);
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(scriptfile));
                    out.write(newscriptcontent);
                    out.close();
                } catch (IOException ioe) {
                    /*-ERROR-*/Lg.err("File load error %s",scriptfile,ioe);
                    throw EEx.create("GroovyExecErr","File load error %s",scriptfile,ioe);                          
                }
                
                // update cache entry
                cacheentry[0] = id;
                execfile = cacheentry[1];
            } else {
                execfile = cacheentry[1];
            }
        } else {            
            String[] cacheentry = new String[2];
            IDfSysObject template = (IDfSysObject)session.getObjectByPath(templatepath);
            String id = template.getObjectId().getId(); 
            String newscriptcontent = DctmUtils.loadFileContents(session, templatepath);
            String origname = template.getObjectName();
            String scriptfilename = origname;
            File scriptfile = new File(cacheroot + File.separatorChar + scriptfilename);
            int i=0;
            while(scriptfile.exists()) {                
                scriptfilename = origname+i;
                scriptfile = new File(cacheroot + File.separatorChar + scriptfilename);
                i++;
            }
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(scriptfile));
                out.write(newscriptcontent);
                out.close();
            } catch (IOException ioe) {
                /*-ERROR-*/Lg.err("File load error %s",scriptfile,ioe);
                throw EEx.create("GroovyExecErr","File load error %s",scriptfile,ioe);                          
            }

            // parse the scriptname from the filename
            execfile = scriptfilename;
            cacheentry[0] = id;
            cacheentry[1] = execfile;
            scriptcache.put(templatepath, cacheentry);
        }
        return execfile;
    }
    
    /**
     * 
     * @param session
     * @param templatepath
     * @param binding
     * @return
     * @throws DfException
     * 
     * This simply runs the given script. The user should examing the binding post-execution for any necessary 
     * information, or use the return value returned by the script to get results from the script execution.
     */
    
    public static Object runScript(IDfSession session, String templatepath, Binding binding) throws DfException
    {
        String execfile = null;
        execfile = getScript(session,templatepath);
        try{
            Object returnval = gse.run(execfile, binding);
            return returnval;
        } catch (ScriptException se) {
            /*-ERROR-*/Lg.err("Groovy Script Execution Error on script %s for template %s",execfile,templatepath,se);
            throw EEx.create("GroovyExecErr","Groovy Script Execution Error on script %s for template %s",execfile,templatepath,se);                          
        } catch (ResourceException re) {
            /*-ERROR-*/Lg.err("Groovy Script Resource Error on script %s for template %s",execfile,templatepath,re);
            throw EEx.create("GroovyExecErr","Groovy Script Resource Error on script %s for template %s",execfile,templatepath,re);                                      
        }
    }
    
    /**
     * 
     * @param session
     * @param templatepath
     * @param binding
     * @return
     * @throws DfException
     * 
     * This runs a groovy script as a view. A variable "out" is automatically placed in the binding, and the string
     * formed by that variable is returned as the view execution result.
     * 
     */

    public static String groovyTemplate(IDfSession session, String templatepath, Binding binding) throws DfException
    {
        String execfile = null;
        execfile = getScript(session,templatepath);
        try{
            // add out to binding
            StringWriter strout = new StringWriter();
            binding.setVariable("out", strout);
            gse.run(execfile, binding);
            return strout.toString();
        } catch (ScriptException se) {
            /*-ERROR-*/Lg.err("Groovy Script Execution Error on script %s for template %s",execfile,templatepath,se);
            throw EEx.create("GroovyExecErr","Groovy Script Execution Error on script %s for template %s",execfile,templatepath,se);                          
        } catch (ResourceException re) {
            /*-ERROR-*/Lg.err("Groovy Script Resource Error on script %s for template %s",execfile,templatepath,re);
            throw EEx.create("GroovyExecErr","Groovy Script Resource Error on script %s for template %s",execfile,templatepath,re);                                      
        }
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
