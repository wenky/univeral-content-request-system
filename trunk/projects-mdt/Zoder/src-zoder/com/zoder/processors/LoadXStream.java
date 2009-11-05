package com.zoder.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;

public class LoadXStream  implements IProcessor
{

    public void process(Map script, Context context) throws Exception 
    {
        XStream xs = new XStream();
        
        Map keys = (Map)context.get("Keys");
        
        Iterator i = keys.keySet().iterator();
        while (i.hasNext()) {
            Object key = i.next();
            String fullfile = (String)keys.get(key);
            BufferedReader rdr = new BufferedReader(new FileReader(new File(fullfile)));
            context.put(key, xs.fromXML(rdr));
        }
        
    }

}
