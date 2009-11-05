package com.zoder.processors;

import java.util.List;
import java.util.Map;

import lbase.Lg;

import com.documentum.fc.common.DfException;
import com.zoder.access.DctmAccess;
import com.zoder.struct.DocumentError;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;
import com.zoder.util.DctmUtils;

public abstract class AbstractVersionProcessor extends AbstractDocumentProcessor
{
    public void processItem(DctmAccess access, Map script, Context context,Map document, int idx) throws Exception 
    {
        /*-log-*/String zzzclassname = getClass().getSimpleName();
        /*-dbg-*/Lg.dbg("top of AVP %",zzzclassname);
        List doclist = (List)document.get("Versions");
        /*-trc-*/Lg.trc("doclist found? %b",doclist!=null);
        if (doclist != null) {
            /*-dbg-*/Lg.dbg("%s - iterating on versions",zzzclassname);
            for (int c=0; c < doclist.size(); c++) {
                /*-trc-*/Lg.trc("doc version %d",c);long t=Lg.curtime();
                Map version = (Map)doclist.get(c);
                try { 
                    /*-trc-*/Lg.trc("calling concrete method");
                    processVersion(access,script,context,document,version,c);
                } catch (DocumentError docerr) {
                    String objectid = document != null ? (String)document.get("ChronicleId"):null;
                    String versionid = version != null ? (String)document.get("r_object_id"):null;
                    /*-INFO-*/Lg.inf("%s - version processing Detected Error on [chronid:%s][versionid:%s][ver:%s]",zzzclassname,objectid,versionid,CollUtils.getVersion(version));                        
                    throw docerr;
                } catch (DfException dfe) { 
                    String objectid = document != null ? (String)document.get("ChronicleId"):null;
                    String versionid = version != null ? (String)document.get("r_object_id"):null;
                    throw new DocumentError(zzzclassname+" - DCTM error in version processing on : [chronid:"+objectid+"][versionid:"+versionid+"][ver:"+CollUtils.getVersion(version)+"] err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); 
                } catch (Exception e) {
                    String objectid = document != null ? (String)document.get("ChronicleId"):null;
                    String versionid = version != null ? (String)document.get("r_object_id"):null;                    
                    throw new DocumentError(zzzclassname+" - version processing failed due to exception on [chronid:"+objectid+"][versionid:"+versionid+"][ver:"+CollUtils.getVersion(version)+"]",e,document);
                }
                /*-trc-*/Lg.trc("version iteration done %d time: %d",c,Lg.curtime()-t);                            
            }
        } else {
            // there really should be at least ONE version!!!!
            String chronid = CollUtils.getDocChronid(document);
            throw new DocumentError(zzzclassname+ ".AVP.processItem - No versions for document with chronicle id: "+chronid,document); 
        }
        String chronid = CollUtils.getDocChronid(document);
        /*-INFO-*/Lg.inf("%s - document processing done - success %s",zzzclassname,chronid);                                
    }

    public abstract void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception;

}
