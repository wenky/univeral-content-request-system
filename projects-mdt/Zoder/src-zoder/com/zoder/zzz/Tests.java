package com.zoder.zzz;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.XStream;
import com.zoder.main.Migrate;
import com.zoder.processors.InitDctmAccess;
import com.zoder.struct.Plugin;

public class Tests 
{
    
    public static void main(String[] args)
    {
        String oldname = "2_On_Approval";
        String match = "[0-9]_.*";
        String to = "";
        
        String[] regex = to.split("/");
        String newname = oldname.replaceAll(regex[0], regex[1]);
        int i=0;
        i++;
        i++;
        

    }
    
    public static void testJOSQL()
    {
        // get sort by version label using josql
        
        //Query q = new Query();
        //q.parse("SELECT get(r_object_id),get(version) FROM java.util.Map ORDER BY get(version)");
        //q.parse("SELECT get(r_object_id),get(version) FROM java.util.Map ORDER BY get(version)");
        //SELECT * FROM java.util.Map WHERE (SELECT * FROM ingredients WHERE name='butter')
        String xml = "<list>" +
        		        "<map><entry><string>val</string><string>3</string></entry>" +
                        "<map><entry><string>val</string><string>16</string></entry>" +
                        "<map><entry><string>val</string><string>2</string></entry>" +
                        "<map><entry><string>val</string><string>4</string></entry>" +
        		     "</list> ";
        
        XStream xs = new XStream();
        List s = (List)xs.fromXML(new StringReader(xml));
        
    }
    
    public static void testRegexSerialize()
    {
        Pattern p = Pattern.compile(".*");
        
        XStream xs = new XStream();
        String s = xs.toXML(p);
        
        Map m = new HashMap();
        m.put("SomeKey",null);
        s= xs.toXML(m);
        
        int i =0; 
        
        i++;
    }
    
    public static void runMigrate()
    {   
        try { 
            Map script = new HashMap();        
            List plugins = new ArrayList();
            script.put("Export",plugins);
            
            Plugin p = new Plugin();
            p.Class = InitDctmAccess.class.getName();
            p.Config = new HashMap();
            p.Config.put("user", "ecsadmin");
            p.Config.put("pass", "spring2005");
            p.Config.put("base", "mqadoc_dev");
            plugins.add(p);
            
            Migrate m = new Migrate();        
            m.execute(script);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void runMigrate2()
    {   
        try { 
            Map script = new HashMap();        
            List plugins = new ArrayList();
            script.put("Export",plugins);
            
            Plugin p = new Plugin();
            p.Class = InitDctmAccess.class.getName();
            p.Config = new HashMap();
            p.Config.put("user", "ecsadmin");
            p.Config.put("pass", "spring2005");
            p.Config.put("base", "mqadoc_dev");
            plugins.add(p);
            
            Migrate m = new Migrate();        
            m.execute(script);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
