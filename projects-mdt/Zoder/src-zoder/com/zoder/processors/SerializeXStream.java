package com.zoder.processors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;

public class SerializeXStream implements IProcessor
{

    public void process(Map script, Context context) throws Exception 
    {
        XStream xs = new XStream();
        
        Map outputfiles = (Map)context.get("Files");        
        Iterator i = outputfiles.keySet().iterator();
        while (i.hasNext()) {
            String outputfilename = (String)i.next();
            String outputkey = (String)outputfiles.get(outputfilename);
            Object outobject = context.get(outputkey);
            
            BufferedWriter wrt = new BufferedWriter(new FileWriter(new File(outputfilename)));
            xs.toXML(outobject, wrt);
            wrt.flush();
            wrt.close();
        }
    }

}
