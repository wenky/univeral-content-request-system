package com.zoder.processors;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;

public class SuccessListSerialize implements IProcessor
{
    
    public void process(Map script,Context context) throws Exception
    {
        
        String basedir = (String)context.get("BaseDirectory");
        String filename = (String)context.get("SuccessFileName"); if (filename == null) filename = "SuccessList.xml";
        
        List success = (List)context.get("Success");
        List outlist = new ArrayList(success.size());
        for (int i=0; i < success.size(); i++) {
            Map document = (Map)success.get(i);
            outlist.add(document.get("ChronicleId"));
        }
        
        XStream xs = new XStream();
        String fullfile = basedir + filename;
        FileWriter wrt = new FileWriter(new File(fullfile));
        xs.toXML(outlist,wrt);
        wrt.close();
    }

}
