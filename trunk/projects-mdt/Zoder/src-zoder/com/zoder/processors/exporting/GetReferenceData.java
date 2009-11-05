package com.zoder.processors.exporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbase.Lg;

import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.util.Context;

public class GetReferenceData extends AbstractVersionProcessor
{
    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception
    {
        IDfSession session = null;
        try {
            /*-dbg-*/Lg.dbg("refdata for id %s", version == null?null:version.get("r_object_id"));
            Map refdata = new HashMap();
            session = access.getSession();
            /*-trc-*/Lg.trc("re-retrieve idfobject");
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            
            /*-trc-*/Lg.trc("id refdata");
            refdata.put("ChronicleId", so.getChronicleId().getId());
            refdata.put("ObjectId", so.getObjectId().getId());
            
            /*-trc-*/Lg.trc("lifecycle refdata");
            refdata.put("Lifecycle", so.getPolicyName());
            refdata.put("LifecycleState",so.getCurrentStateName());
            
            /*-trc-*/Lg.trc("ACL refdata");
            refdata.put("ACL",so.getACLName());
            refdata.put("ACLDomain",so.getACLDomain());
            
            /*-trc-*/Lg.trc("Alias refdata");
            refdata.put("AliasSet",so.getAliasSet());

            /*-trc-*/Lg.trc("Format refdata");
            IDfFormat fmt = so.getFormat();
            if (fmt == null) {
                throw new DocumentError("Doc Version has null format verid: "+so.getObjectId().getId() + " chronid: "+ so.getChronicleId().getId(),document);
            }
            refdata.put("Format",fmt.getName());
            refdata.put("Format-DOSEXT",fmt.getDOSExtension());
            
            /*-trc-*/Lg.trc("Type refdata");
            refdata.put("Type", so.getTypeName());

            /*-trc-*/Lg.trc("Version labels refdata");
            List versionlabels = new ArrayList();
            for (int i=0; i < so.getVersionLabelCount(); i++) {
                String label = so.getVersionLabel(i);
                if (!Character.isDigit(label.charAt(0))) {
                    versionlabels.add(label);
                }
            }
            refdata.put("Labels", versionlabels);
            
            //String vdassemblydocid = so.getAssembledFromId().getId();
            // getCompoundArchitecture, getCOntainId, getContainIdCount, getHasFrozenAssembly, getResolutionLabel
            
            version.put("RefData", refdata);
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }
        
        
    }

}
