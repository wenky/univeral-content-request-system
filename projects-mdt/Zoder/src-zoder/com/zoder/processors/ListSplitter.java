package com.zoder.processors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nighteclipse.migrate.interfaces.IProcessor;
import com.nighteclipse.migrate.struct.MigrationScript;
import com.thoughtworks.xstream.XStream;

public class ListSplitter implements IProcessor
{

    public void process(MigrationScript script, Map config, Map context) throws Exception 
    {
        String listkey = (String)context.get("ListKey");
        int splitsize = Integer.parseInt((String)context.get("SplitSize"));        
        List mainlist = (List)context.get(listkey);
        
        int maincounter = 0;
        int framenumber = 0;
        while (true) {            
            List cursplit = new ArrayList(splitsize);
            int i=0;
            for (i=0; i+maincounter < mainlist.size() && i < splitsize; i++) {
                cursplit.add(mainlist.get(maincounter+i));
            }
            maincounter += i;
            
            outputList(context,framenumber,cursplit);
            framenumber++;
            if (maincounter >= mainlist.size())
                break;
        }
        
    }
    
    public void outputList(Map context, int framenumber, List cursplit) throws Exception
    {
        // default: prefix + framenumber + suffix - override to your heart's content in a subclass if you desire...
        String prefix = (String)context.get("FilePrefix");
        String suffix = (String)context.get("FileSuffix");
        
        String basedir = (String)context.get("BaseDirectory");
        String curpath = basedir+prefix+framenumber+suffix;
        
        File f = new File(curpath);
        XStream xs = new XStream();
        xs.toXML(cursplit, new BufferedWriter(new FileWriter(f)));
    }

}
