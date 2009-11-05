package com.zoder.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lbase.Lg;

import com.csvreader.CsvReader;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;
import com.zoder.util.ResultSetList;

public class LoadCSV implements IProcessor 
{
    
    public void process(Map script, Context context) throws Exception 
    {    
       
        try {
            Map keys = (Map)context.get("Keys");
            
            Iterator i = keys.keySet().iterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                String filename = (String)keys.get(key);
                /*-trc-*/Lg.trc("csvfile: %s",filename);
                File f = new File(filename);
                BufferedReader rdr = new BufferedReader(new FileReader(f));
                /*-trc-*/Lg.trc("prep csvreader");
                CsvReader csvrdr = new CsvReader(rdr);
                Map columns = new HashMap();

                boolean headerfound = false;
                if (context.containsKey("Headers")) {
                    Map headermap = (Map)context.get("Headers");
                    if (headermap.containsKey(key)) {
                        headerfound = true;
                        List list = (List)headermap.get(key);
                        for (int z=0; z < list.size(); z++) {
                            columns.put(list.get(z), z);
                        }
                    }
                }
                
                if (!headerfound) {
                    // assume first row is header row
                    // get header row            
                    /*-trc-*/Lg.trc("read hdrs");
                    csvrdr.readHeaders();
                    String[] list= csvrdr.getHeaders();
                    for (int z=0; z < list.length; z++) {
                        columns.put(list[z], z);
                    }
                    
                }
                
                //read data rows
                List datarows = new ArrayList();
                String datarow = null;                
                while (csvrdr.readRecord()) 
                { 
                    /*-trc-*/Lg.trc("read data row");
                    String[] data = csvrdr.getValues();
                    datarows.add(data);
                }
                
                ResultSetList rsl = new ResultSetList(datarows,columns);
                context.put(key, rsl);
                /*-trc-*/Lg.trc("done");            
            }
            
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("FATAL",e);
            throw new RuntimeException(e);
        }
        
        
    }

}
