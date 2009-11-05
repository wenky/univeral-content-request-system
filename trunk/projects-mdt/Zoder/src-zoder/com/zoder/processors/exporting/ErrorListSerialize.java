package com.zoder.processors.exporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvWriter;
import com.thoughtworks.xstream.XStream;
import com.zoder.main.IProcessor;
import com.zoder.struct.ErrorDetail;
import com.zoder.util.Context;

public class ErrorListSerialize implements IProcessor
{
    
    public void process(Map script,Context context) throws Exception
    {
        List errlist = (List)context.get("ErrorList");
        // preprocess for serialization
        List outlist = new ArrayList(errlist.size()+1);
        List errorout = new ArrayList(errlist.size());
        outlist.add("ErrorCount: "+errlist.size());
        
        StringWriter errcsv = new StringWriter();
        CsvWriter csvwrt = new CsvWriter(new BufferedWriter(errcsv), ',');
        String[] header = {"ChronicleId",/*"ObjectId","Ver",*/"Message","Name"};
        csvwrt.writeRecord(header);
        
        for (int i=0; i < errlist.size(); i++) {
            ErrorDetail detail = (ErrorDetail)errlist.get(i);
            ErrorDetail newdtl = new ErrorDetail();
            newdtl.message = ((Map)detail.reference).get("ObjectName")+" - "+ detail.message;
            newdtl.stacktrace = ErrorDetail.stacktrace(detail.t);
            if (detail.t != null  && detail.t.getCause() != detail.t) {
                Throwable t2 = detail.t.getCause();
                newdtl.stacktrace2 = ErrorDetail.stacktrace(t2);
                if (t2 != null && t2.getCause() != t2) {
                    Throwable t3 = t2.getCause();
                    newdtl.stacktrace3 = ErrorDetail.stacktrace(t3);
                }
            }
            errorout.add(((Map)detail.reference).get("ChronicleId"));
            outlist.add(newdtl);

            String chronid = null,/*verid = null,vers = null,*/objname = null,msg = null;
            try { 
                chronid = (String)((Map)detail.reference).get("ChronicleId");
                /*verid = null; vers = null;*/
                msg = detail.message;
                objname = (String)((Map)detail.reference).get("ObjectName");
            } catch (Exception e) {
                // warn
            }
            String[] row = {chronid,/*verid,vers,*/msg,objname};
            csvwrt.writeRecord(row);
        }        
        csvwrt.flush();
        
        String basedir = (String)context.get("BaseDirectory");
        XStream xs = new XStream();
        xs.alias("ErrorDetail",ErrorDetail.class);
        BufferedWriter wrt = new BufferedWriter(new FileWriter(new File(basedir+"Export.ErrorReport.xml")));
        xs.toXML(outlist, wrt);
        wrt.flush();
        wrt.close();

        BufferedWriter csvfilewrt = new BufferedWriter(new FileWriter(new File(basedir+"Export.ErrorReport.csv")));
        csvfilewrt.write(errcsv.toString());
        csvfilewrt.flush();
        csvfilewrt.close();

        wrt = new BufferedWriter(new FileWriter(new File(basedir+"Export.ErrorList.xml")));
        xs.toXML(errorout,wrt);
        wrt.flush();
        wrt.close();
        
    }


}
