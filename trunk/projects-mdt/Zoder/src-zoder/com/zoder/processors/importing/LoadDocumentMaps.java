package com.zoder.processors.importing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.thoughtworks.xstream.XStream;
import com.zoder.directoryresolvers.DirectoryResolver;
import com.zoder.main.IProcessor;
import com.zoder.util.Context;
import com.zoder.util.VersionLabelComparator;

public class LoadDocumentMaps implements IProcessor
{
    public void process(Map script, Context context) throws Exception 
    {
        List idlist = context.getList("IdList");
        List success = context.getList("Success");
        List failure = context.getList("Failure");
        
        String basedir = (String)context.get("BaseDirectory");
        XStream xs = new XStream();
        
        DirectoryResolver docdirresolver = DirectoryResolver.getDirectoryResolver(context);
        
        // iterate through the id list and deserialize the metadata map
        for (int i=0; i < idlist.size(); i++) {
            Map document = new HashMap();
            String chronid = (String)idlist.get(i);
            document.put("ChronicleId", chronid);
            try { 
                String docreldir = docdirresolver.relativeDirectory(chronid, context);
                String chronpath = basedir+docreldir;
                document.put("DocumentPath", chronpath);
                
                document.put("ChronicleId", chronid);
                
                // get list of .xml files from the folder
                File docexportdir = new File(chronpath);
                File[] xmlfiles = docexportdir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) { return name == null ? false : name.endsWith(".xml"); }
                });
                
                // sort as we go
                Map versionmap = new TreeMap(new VersionLabelComparator());
                for (int f=0; f < xmlfiles.length; f++) {
                    String filename = xmlfiles[f].getName();
                    Map metadata = (Map)xs.fromXML(new BufferedReader(new FileReader(xmlfiles[f])));
                    String[] filenamedata = filename.split("-");
                    String version = filenamedata[1];
                    String objectid = filenamedata[2].substring(0,filenamedata[2].length()-4);                
                    versionmap.put(version, metadata);
                    metadata.put("ParsedVersion", version);
                    metadata.put("ParsedObjectId", objectid);
                }
                
                List versionlist = new ArrayList();
                Iterator veriter = versionmap.keySet().iterator();
                while (veriter.hasNext()) {
                    versionlist.add(versionmap.get(veriter.next()));
                }
                document.put("Versions", versionlist);            
                
                success.add(document);
            } catch (Exception e) {
                failure.add(document);
            }
        }
        
        
    }
}
