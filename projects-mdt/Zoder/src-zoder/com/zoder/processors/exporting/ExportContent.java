package com.zoder.processors.exporting;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.thoughtworks.xstream.XStream;
import com.zoder.access.DctmAccess;
import com.zoder.directoryresolvers.DirectoryResolver;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;

public class ExportContent extends AbstractVersionProcessor 
{
    public void processItem(DctmAccess access, Map script, Context context,Map document, int idx) throws Exception
    {
        String basedir = (String)context.get("BaseDirectory");        
        String chronid = (String)document.get("ChronicleId");
        String reldir = DirectoryResolver.getDirectoryResolver(context).relativeDirectory(chronid,context);
        String docdir = basedir+reldir;
        
        // verify that basedir is there
        File f = new File(docdir);      
        // create document subdir if its not there yet
        if (!f.exists()) {
            f.mkdirs();         
        }
        
        super.processItem(access, script, context, document, idx);
    }
    
    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        String basedir = (String)context.get("BaseDirectory");
        String filename = (String)context.get("ErrorFileName"); if (filename == null) filename = "ErrorFile.xml";
        List renditionformats = (List)context.get("RenditionFormats");
        
        DirectoryResolver docdirresolver = DirectoryResolver.getDirectoryResolver(context);
        

        IDfSession session = null;
        try {
            session = access.getSession();
            
            String objectid = (String)version.get("r_object_id");
            IDfId asid = new DfId(objectid);
            IDfSysObject docobj = (IDfSysObject)session.getObject(asid);
            // check format...
            String format = null;
            IDfFormat formatobj = docobj.getFormat();
            if (formatobj == null) {
                throw new DocumentError("Null format for object "+docobj.getObjectId().getId()+" chronid: "+docobj.getChronicleId().getId(),document);
            }
            
            String docreldir = docdirresolver.relativeDirectory(docobj.getChronicleId().getId(), context);
            String docdir = basedir+docreldir;
            
            XStream xs = new XStream();
            Map docdata = new HashMap();
            docdata.put("MetaData", version.get("MetaData"));
            if (version.containsKey("FolderLinks"))
                docdata.put("FolderLinks", version.get("FolderLinks"));
            else if (version.containsKey("FolderPaths"))
                docdata.put("FolderPaths", version.get("FolderPaths"));
            docdata.put("ReferenceData", version.get("RefData"));
            xs.toXML(docdata, new FileWriter(new File(docdir+"MigrationData-"+version.get("version")+'-'+docobj.getObjectId().getId()+".xml")));
            
            String docname = docdir+"Content-"+version.get("version")+'-'+docobj.getObjectId().getId()+"."+docobj.getFormat().getDOSExtension(); 
            
            try { docobj.getFile(docname);}
                catch (DfException dfe) { throw new DocumentError("ExportContent - DCTM error on content export : "+docobj.getObjectId().getId()+ " chronid: "+docobj.getChronicleId().getId(),dfe,document); }
                catch (Exception e) { throw new DocumentError("ExportContent - Exception on content export : "+docobj.getObjectId().getId()+ " chronid: "+docobj.getChronicleId().getId(),e,document); }
             
            if (renditionformats != null) {
                for (int i=0; i < renditionformats.size(); i++) {
                    String renditionformat = (String)renditionformats.get(i);
                    String renditionfilename = docdir+"Rendition-"+version.get("version")+'-'+docobj.getObjectId().getId()+"."+renditionformat; 
                    try { 
                        docobj.getFileEx(renditionfilename, renditionformat, 0, false);
                    } catch (Exception e) {
                        // required state validations? TODO
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try { access.releaseSession(session); } catch (Exception e) {}
        }
            
    }
    

}