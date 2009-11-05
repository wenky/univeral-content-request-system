package com.zoder.processors.conversion;

import java.util.Map;

import lbase.Lg;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractDocumentProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;
import com.zoder.util.DctmUtils;

public class TransformObjectType extends AbstractDocumentProcessor 
{

    public void processItem(DctmAccess access, Map script, Context context, Map document, int idx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)document.get("OriginalId")));
                        
            filterItem(session,document,context,so);
            
            String fromType = getFromType(session,document,context,so);
            String toType = getToType(session,document,context,so);
            
            
            IDfQuery query = new DfQuery();
            String dql = "CHANGE "+fromType+"(ALL) OBJECTS TO "+toType+" WHERE i_chronicle_id = '"+so.getChronicleId().getId()+"'";
            /*-INFO-*/Lg.inf("CHANGE OBJECTS DQL: %s",dql);                    
            
            try {
                if (!DctmUtils.verifyPermissionsOnAllVersions(session,so,IDfACL.DF_PERMIT_DELETE)) {
                    throw new DocumentError("TransformObjectType - INSUFFICIENT PRIVLEDGE ON FULL VERSION TREE: "+DctmUtils.getObjectSummary(so),new RuntimeException("TransformObjectType - INSUFFICIENT PRIVLEDGE ON FULL VERSION TREE: "+DctmUtils.getObjectSummary(so)),document);                
                }
                if (!DctmUtils.verifyNotLockedOnAllVersions(session,so,IDfACL.DF_PERMIT_DELETE)) {
                    throw new DocumentError("TransformObjectType - LOCKED VERSION DETECTED on object: "+DctmUtils.getObjectSummary(so),new RuntimeException("TransformObjectType - INSUFFICIENT PRIVLEDGE ON FULL VERSION TREE: "+DctmUtils.getObjectSummary(so)),document);                
                }
            } catch (DfException dfe) { 
                throw new DocumentError("TransformObjectType - DCTM error on VERIFY VERSION TREE PERMIT: "+DctmUtils.getObjectSummary(so) + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); 
            } catch (Exception e) { 
                throw new DocumentError("TransformObjectType - exception on VERIFY VERSION TREE PERMIT: "+DctmUtils.getObjectSummary(so) + " err: " + e.getMessage(),e,document);
            }

            try {
                DctmUtils.makeAllVersionsMutable(session, so);
            } catch (DfException dfe) {
                throw new DocumentError("TransformObjectType - DCTM error de-immuting the version tree of document: "+DctmUtils.getObjectSummary(so) + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document);                 
            }
            
            IDfCollection coll = null;
            try {
                coll = query.execute(session,IDfQuery.EXEC_QUERY);
                String changecount = DctmUtils.getValueAsString(coll.getValue("objects_changed"));
                // success? return code?
                /*-INFO-*/Lg.trc("TRANSFORM TYPE SUCCESSFUL: "+dql);                            
            } catch (DfException dfe) { 
                throw new DocumentError("TransformObjectType - DCTM error on CHANGE OBJECTS DQL: "+DctmUtils.getObjectSummary(so) + " dql: "+dql+" err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); 
            } catch (Exception e) { 
                /*-ERROR-*/Lg.err("TransformObjectType - exception on CHANGE OBJECTS DQL: "+DctmUtils.getObjectSummary(so) + " dql: "+dql+" err: " +e.getMessage(),e);                    
                throw new DocumentError("TransformObjectType - exception on CHANGE OBJECTS DQL: "+DctmUtils.getObjectSummary(so) + " dql: "+dql+" err: " + e.getMessage(),e,document); 
            } finally {
                try{coll.close();}catch(Exception e) {}
            }

            try {
                DctmUtils.makeAllPreviousVersionsImmutable(session, so);
            } catch (DfException dfe) {
                throw new DocumentError("TransformObjectType - DCTM error de-immuting the version tree of document: "+DctmUtils.getObjectSummary(so) + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document);                 
            }
            
            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
    }
    
    public void filterItem(IDfSession session, Map document, Map context, IDfSysObject so)throws Exception
    {
        return;
    }
    
    public String getFromType(IDfSession session, Map document, Map context, IDfSysObject so)throws Exception
    {
        String fromtype = (String)context.get("FromType");
        return fromtype;
    }

    public String getToType(IDfSession session, Map document, Map context, IDfSysObject so)throws Exception
    {
        String totype = (String)context.get("ToType");
        return totype;
    }


}
