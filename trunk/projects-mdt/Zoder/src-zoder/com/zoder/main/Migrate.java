package com.zoder.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.zoder.struct.Plugin;
import com.zoder.util.Context;
import com.zoder.util.Lg;

public class Migrate 
{
    
    String[] defaultstages = {"PreExport","Export","PostExport","PreImport","Import","PostImport"};
    
    public void execute(Map script) throws Exception
    {
        // prep the context for script, execcontext, and future pluginconfig values
        Map execcontext = new HashMap();
        Context maincontext = new Context();
        maincontext.simpleInit(2,"pluginconfig",null,"stageconfig",null,"context",execcontext,"script",script);
        
        if (script.containsKey("Globals")) {
            maincontext.addContextMap((Map)script.get("Globals"), "globals");
        }
        
        // get stages configuration (in case there is a custom set of stages)
        List stagelist = (List)script.get("StageList");
        if (stagelist == null) {
            stagelist = new ArrayList();
            for (int i=0; i < defaultstages.length; i++) stagelist.add(defaultstages[i]);
        }
        
        // execute the six steps, where configured:
        //  PreExport, Export, PostExport
        //  PreImport, Import, PostImport
        
        for (int i=0; i < stagelist.size(); i++) {
            String stage = (String)stagelist.get(i);
            processStage(script,maincontext,stage);
        }
        
    }
    
    public void processStage(Map script,Context maincontext,String stagename) throws Exception
    {
        Map stageconfig = (Map)script.get(stagename);
        if (stageconfig == null) {
            /*-log-*/Lg.log("Stage "+stagename+" not configured");            
        }else{
            maincontext.setContextMap(stageconfig, "stageconfig");
            List processors = (List)stageconfig.get("Processors");
            /*-log-*/Lg.log("EXEC Stage");
            for (int i=0; i< processors.size(); i++)
            {
                try {
                    /*-log-*/Lg.log("exec #"+i);
                    Plugin plugin = (Plugin)processors.get(i);
                    maincontext.put("pluginclass",plugin.Class);                
                    maincontext.put("pluginindex",i);                
                    /*-log-*/Lg.log("load class - "+plugin.Class);
                    IProcessor processor = (IProcessor)Class.forName(plugin.Class).newInstance();               
                    maincontext.setContextMap(plugin.Config, "pluginconfig");            
                    /*-log-*/Lg.log("exec plugin - "+plugin.Class);
                    processor.process(script,maincontext);
                    /*-log-*/Lg.log("done");
                } catch (Exception e) {
                    /*-log-*/Lg.log("FATAL EXCEPTION IN STAGE EXECUTION - "+stagename);
                    throw e;    
                }
                // clear out stagecontext
            }
            maincontext.setContextMap(null, "empty", 1);
            /*-log-*/Lg.log("END PREEXPORT EXEC");
        }
        
    }
    
    public Map loadScriptFile(String scriptfile) throws FileNotFoundException
    {
        /*-log-*/Lg.log("LOAD SCRIPT FILE: "+scriptfile);
        XStream xs = new XStream();
        
        xs.alias("Plugin", Plugin.class);
        Map script = (Map)xs.fromXML(new FileReader(scriptfile));
        return script; 
    }
    
    public static void main(String[] args) throws Exception
    {
        //String scriptfile = args[0];
        //String scriptfile = "C:/Dev/migrate3/Zoder/sampleexport-1-folders.xml";
        //String scriptfile = "C:/Dev/migrate3/Zoder/sampleexport-2-documents.xml";
        //String scriptfile = "C:/Dev/migrate3/Zoder/sampleimport-1-folders.xml";
        //String scriptfile = "C:/Dev/migrate3/Zoder/sampleimport-2-documents.xml";
        //String scriptfile = "C:/Dev/migrate3/Zoder/conversion-1-documents-changetypes.xml";
        String scriptfile = "C:/Dev/migrate3/TestMigration3CSV/conversion-1.xml";
        
        
        Migrate m = new Migrate();        
        Map script = m.loadScriptFile(scriptfile);
        m.execute(script);
    }

}
