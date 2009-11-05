package com.zoder.processors.importing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.zoder.directoryresolvers.DirectoryResolver;
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
        }
        String basedir = (String)context.get("BaseDirectory");
        XStream xs = new XStream();
        xs.alias("ErrorDetail",ErrorDetail.class);
        BufferedWriter wrt = new BufferedWriter(new FileWriter(new File(basedir+"Import.ErrorReport.xml")));
        xs.toXML(outlist, wrt);
        wrt.close();
        
        wrt = new BufferedWriter(new FileWriter(new File(basedir+"Import.ErrorList.xml")));
        xs.toXML(errorout,wrt);
        wrt.close();
        
        // do success and failure results
        List success = context.getList("Success");
        List failure = context.getList("Failure");
        List outsuccess = new ArrayList();
        List outfailure = new ArrayList();
        Map successmap = new HashMap();
        Map failuremap = new HashMap();
        {
            for (int i=0; i < success.size(); i++) {
                Map document = (Map)success.get(i);
                String oldid = (String)document.get("ChronicleId");
                String newid = (String)document.get("New.ChronicleId");
                outsuccess.add(oldid);                
                successmap.put(oldid,newid);
            }
        }

        {
            for (int i=0; i < failure.size(); i++) {
                Map document = (Map)failure.get(i);
                String oldid = (String)document.get("ChronicleId");
                String newid = (String)document.get("New.ChronicleId");
                outfailure.add(oldid);                
                failuremap.put(oldid,newid);
            }
        }

        wrt = new BufferedWriter(new FileWriter(new File(basedir+"Import.SuccessList.xml")));
        xs.toXML(outsuccess,wrt);
        wrt = new BufferedWriter(new FileWriter(new File(basedir+"Import.FailureList.xml")));
        xs.toXML(outfailure,wrt);
        wrt = new BufferedWriter(new FileWriter(new File(basedir+"Import.SuccessMap.xml")));
        xs.toXML(successmap,wrt);
        wrt = new BufferedWriter(new FileWriter(new File(basedir+"Import.FailureMap.xml")));
        xs.toXML(failuremap,wrt);
        
    }


}
