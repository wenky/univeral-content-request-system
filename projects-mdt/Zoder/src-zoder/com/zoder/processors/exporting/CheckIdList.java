package com.zoder.processors.exporting;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.struct.ErrorDetail;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;

public class CheckIdList implements IProcessor {

    public void process(Map script, Context context) throws Exception 
    {
        DctmAccess access = (DctmAccess)context.get("SourceAccess");
        
        // get list of ids
        List exportidlist = (List)context.get("InputDocIdList");
        List success = CollUtils.getList(context,"Success");
        List failure = CollUtils.getList(context, "Failure");
        List errors = CollUtils.getList(context, "ErrorList");        
        
        for (int i=0; i < exportidlist.size(); i++) {
            Object listitem = exportidlist.get(i);
            String id = null;
            if (listitem instanceof IDfId) {
                id = ((IDfId)listitem).getId();
            } else if (listitem.getClass().isArray()) {
                // assume it is the first item (probably this is a csv resultset)                
                id = Array.get(listitem, 0).toString();
            } else if (listitem instanceof List) {
                List valuelist = (List)listitem;
                // assume it is the first item (probably this is a csv resultset)
                id = valuelist.get(0).toString();
            } else if (listitem instanceof Map) {
                Map valuemap = (Map)listitem;
                // assume it is in key r_object_id
                id = valuemap.get("r_object_id").toString();
            } else {
                id = listitem.toString();
            }
            
            // map to store the info specific to the document
            Map exportdocument = new HashMap();
            exportdocument.put("OriginalId",id);
            
            // create success list, fail list, and unique doc tracker
            Map  chronicleids = new HashMap();
            
            // check for version label consistency
            try { 
                getBasicDocinfo(access,id,exportdocument,script,context);
                success.add(exportdocument);
            } catch (DocumentError docerr) {
                failure.add(exportdocument);
                ErrorDetail err = new ErrorDetail();
                err.message = docerr.getMessage();
                err.t = docerr.getCause();
                err.reference = exportdocument;
                errors.add(err);
            } catch (Throwable e) {
                failure.add(exportdocument);
                ErrorDetail err = new ErrorDetail();
                err.message = e.getMessage();
                err.t = e;
                err.reference = exportdocument;
                errors.add(err);
            }
        }
    }
    
    public static void getBasicDocinfo(DctmAccess access, String objectid, Map document, Map script, Context context) throws Exception
    {
        
        IDfSession session = null;
        try {
            session = access.getSession();
            
            IDfId theid = null;
            try { theid = new DfId(objectid); } 
                catch (Exception e) { throw new DocumentError("CheckIdList - getBasicDocInfo - Invalid id: "+objectid,e,document);  }
            
            IDfSysObject thedoc = null;
            try {thedoc = (IDfSysObject)session.getObject(theid); }
                catch (ClassCastException cce) { throw new DocumentError("CheckIdList - getBasicDocInfo - Not a sysobject, perhaps persistent object: "+objectid,cce,document); }
                catch (DfException dfe) { throw new DocumentError("CheckIdList - getBasicDocInfo - DCTM error on lookup: "+objectid + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); }
                catch (Exception e) { throw new DocumentError("CheckIdList - getBasicDocInfo - Other error on lookup: "+objectid + " err: " + e.getMessage(),e,document); }

            try {
                IDfId chronid = thedoc.getChronicleId();
                document.put("ObjectName", thedoc.getObjectName());
                document.put("ChronicleId", chronid.getId());
            } catch (DfException dfe) { throw new DocumentError("CheckIdList - getBasicDocInfo - DCTM error on chronicle lookup: "+objectid + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); }
            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }
        
    }
            

}
