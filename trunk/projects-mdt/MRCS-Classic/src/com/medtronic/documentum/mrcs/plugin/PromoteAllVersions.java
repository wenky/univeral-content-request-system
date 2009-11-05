package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

// THIS IS A MRCS4.2-only plugin!!!

public class PromoteAllVersions extends SuspendPreviousVersion implements IMrcsLifecyclePlugin {

    // EXTEND ME! if another plugin needs a different lookup DQL, just extend this class and override this method
    public String getDQL(IDfSessionManager sMgr, String docbase, String mrcsapp, IDfSysObject mrcsdocument, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "PromoteAllVersions.getDQL()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get document's id", null, null);
        String docid = mrcsdocument.getObjectId().getId();
        int curstate = mrcsdocument.getCurrentState();
               
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for looking up both newer and older versions of the current doc, excluding the current doc", null, null);
        String qualification = "select r_object_id from dm_sysobject (ALL) "+
 					           "where NOT r_object_id = '"+docid+"' AND i_chronicle_id in " +
						            "(select i_chronicle_id from dm_sysobject where r_object_id = '"+docid + "')";
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"composed DQL: "+qualification, null, null);
        return qualification;
        
    }
    
    // EXTEND ME! override this if you want query results to be processed differently - this does simple promote on all other versions of the doc...
    public void process(IDfCollection match, IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument mrcsdocument, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "PromoteAllVersions.process()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        
        // extract document id from current match (assumes docid is the only string in the match's attribute colleciton), lookup doc in docbase
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get match's object id", null, null);
        String id = getMatchObjectId(match);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up document", null, null);
        IDfDocument doc = (IDfDocument)session.getObject(new DfId(id));
        
        try { 
            // make object immutable, and attach the new lifecycle (policy)
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"begin attach and detach of new lifecycle", null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"make document mutable", null, null);
            // user should be SUPERUSER in order to play with r_immutable_flag
            doc.setString("r_immutable_flag", "FALSE");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving", null, null);
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"refetching", null, null);
            doc.fetch(doc.getTypeName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"forcing simple promote", null, null);
            doc.promote(null,true,false);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"simple promote complete", null, null);
    
            // apply new LC state's label
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scan document's current labels", null, null);
            for (int i = 0; i < doc.getVersionLabelCount(); i++) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--examining label #"+i, null, null);
                String label = doc.getVersionLabel(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--cur label: "+ label, null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--see if we've found 'CURRENT'", null, null);
                if (!label.equalsIgnoreCase("CURRENT"))
                {
                    try {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--'CURRENT' found, unmarking...", null, null);
                        doc.unmark(label);
                    } catch (DfException e1) {
                        /*-ERROR-*/DfLogger.error(this, m+"Error when removing 'CURRENT' label from older document "+doc.getObjectId().getId(), null, e1);
                        throw e1;
                    }
                }
            }
                        
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting document back to immutable", null, null);
            doc.setString("r_immutable_flag", "TRUE");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving", null, null);
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"refetching...", null, null);
            doc.fetch(doc.getTypeName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done!", null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.debug(this, m+"Exception in arbitrary state attachment for doc"+id, null, e);
            throw e;
        }
    }

}
