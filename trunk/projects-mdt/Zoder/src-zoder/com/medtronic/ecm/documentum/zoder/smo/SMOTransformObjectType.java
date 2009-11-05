package com.medtronic.ecm.documentum.zoder.smo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbase.Lg;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.zoder.processors.conversion.TransformObjectType;
import com.zoder.struct.DocumentError;
import com.zoder.util.CollUtils;
import com.zoder.util.DctmUtils;
import com.zoder.util.ResultSetList;

public class SMOTransformObjectType extends TransformObjectType
{
    ResultSetList mainlist = null;
    Map mainlistindex = new HashMap();
    ResultSetList oldtonew = null;
    Map oldtonewindex = new HashMap();
    
    public void filterItem(IDfSession session, Map document, Map context, IDfSysObject so)throws Exception
    {
        if (mainlist == null) {
            // lazy init - merge lists
            List list = null;
            String[] keylist = { "qualitydocuments", "qualitymanuals", "qualityprocedures", "qualityrecords"};
            mainlist = (ResultSetList)context.get(keylist[0]);
            list = (List)context.get(keylist[1]);
            mainlist.addAll(list);
            list = (List)context.get(keylist[2]);
            mainlist.addAll(list);
            list = (List)context.get(keylist[3]);
            mainlist.addAll(list);
            
            // index for fast lookup: avoid nested linear scan of mainlist 
            for (int zz=0; zz < mainlist.size(); zz++) {
                mainlistindex.put(mainlist.get(zz, 0),zz);
            }
            
            
            oldtonew = (ResultSetList)context.get("NewIdOldIdList");
            for (int zz=0; zz < oldtonew.size(); zz++) {
                oldtonewindex.put(oldtonew.get(zz, 0),oldtonew.get(zz,2));
            }
        }
        
        // exclude if object type isn't mdt_qad_smo_doc (extant quality documents)
        if (!"mdt_qad_smo_doc".equals(so.getTypeName())) {
            throw new DocumentError("SMO-XFORMTYPE - WRONG CURRENT TYPE: Object "+DctmUtils.getObjectSummary(so),document);
        }
        
        return;
    }
    
    public String getFromType(IDfSession session, Map document, Map context, IDfSysObject so)throws Exception
    {
        return "mdt_qad_smo_doc";
    }

    public String getToType(IDfSession session, Map document, Map context, IDfSysObject so)throws Exception
    {
        //String oldid = so.getId("m_old_object_id").getId();
        String oldid = (String)oldtonewindex.get(so.getObjectId().getId());
        int idx = -1;
        try { 
            idx = (Integer)mainlistindex.get(oldid);
        } catch (Exception e) {
            throw new DocumentError("SMO-XFORMTYPE - OLDID DATA NOT FOUND: Object "+DctmUtils.getObjectSummary(so)+" mapped to oldid "+oldid+" which was NOT FOUND in CSV data",e,document);                    
        }
        String oldtype = (String)mainlist.get(idx,"r_object_type");
        String oldqatype = (String)mainlist.get(idx,"qa_type");
        String oldsubtype = (String)mainlist.get(idx,"subtype");        
        
        String totype = null;
        if ("quality_record_trs".equals(oldtype) && "TRS".equals(oldqatype) && "PVSP".equals(oldsubtype)) {
            totype = "mdt_qad_smo_qrec_rev";
        } else {
            totype = "mdt_qad_smo_qrec_lock";
        }
        
        return totype;
    }

    

}
