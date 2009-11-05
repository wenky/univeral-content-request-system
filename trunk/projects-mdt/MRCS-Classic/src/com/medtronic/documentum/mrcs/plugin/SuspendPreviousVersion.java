package com.medtronic.documentum.mrcs.plugin;

import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

// THIS IS A MRCS4.2-only plugin!!!

public class SuspendPreviousVersion implements IMrcsLifecyclePlugin {

    // EXTEND ME! if another plugin needs a different lookup DQL, just extend this class and override this method
    public String getDQL(IDfSessionManager sMgr, String docbase, String mrcsapp, IDfSysObject mrcsdocument, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "SuspendPreviousVersion.getDQL()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get document's id", null, null);
        String docid = mrcsdocument.getObjectId().getId();
        int curstate = mrcsdocument.getCurrentState();
               
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for looking up both newer and older versions of the current doc with the same state, excluding the current doc", null, null);
        String qualification = "select r_object_id from dm_sysobject (ALL) "+
                               "where NOT r_object_id = '"+docid+"' AND r_current_state = "+curstate+" AND i_chronicle_id in " +
                                    "(select i_chronicle_id from dm_sysobject where r_object_id = '"+docid + "')";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"composed DQL: "+qualification, null, null);
        return qualification;
        
    }
    
    // EXTEND ME! override this if you want query results to be processed differently - this does forced suspend on matches...
    public void process(IDfCollection match, IDfSessionManager sMgr, String docbase, String mrcsapp, IDfSysObject mrcsdocument, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "SuspendPreviousVersion.process()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
                
        // extract document id from current match (assumes docid is the only string in the match's attribute colleciton), lookup doc in docbase
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get match's object id", null, null);
        String id = getMatchObjectId(match);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up document", null, null);
        IDfDocument doc = (IDfDocument)session.getObject(new DfId(id));
        
        try { 
            // make object mutable, and attach the new lifecycle (policy)
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"begin attach and detach of new lifecycle", null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"make document mutable", null, null);
            // user should be SUPERUSER in order to play with r_immutable_flag
            doc.setString("r_immutable_flag", "FALSE");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving", null, null);
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"refetching", null, null);
            doc.fetch(doc.getTypeName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"suspending current document", null, null);
            doc.suspend(null,true,false);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"suspended", null, null);
    
            // make sure it isn't current
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scan document's current labels - no CURRENT", null, null);
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
    
    // utility method to get the objectid from the match's collection of attributes
    // ...only works if the query only selects one single field, the objectid
    public String getMatchObjectId(IDfCollection match) throws Exception
    {
        /*-CFG-*/String m = "SuspendPreviousVersion.getMatchObjectId()-";
        String id = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>processing match's attributes", null, null);
        for (int i = 0; i < match.getAttrCount(); i++) 
        { 
            IDfAttr attr = match.getAttr(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>match attr: "+attr.getName(), null, null);
            if (attr.getDataType() == attr.DM_STRING) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>doc attr is a DM_STRING, must be a docid", null, null);
                id = match.getString(attr.getName());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>object id: "+id, null, null);
            }
        }
        return id;
    }   


    // a simple method that asks for a DQL statement with getDQL(), iterates through the result sets, calling process() for each of them.    
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map configdata, Map contextdata)
    {
        /*-CFG-*/String m = "SuspendPreviousVersion.execute()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"preparing to perform search + process ", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        String docid = null;
        try {

            IDfSession session = sMgr.getSession(docbase);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get postversioned document's new id", null, null);
            docid = mrcsdocument.getObjectId().getId();
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for search", null, null);
            String qualification = getDQL(sMgr,docbase,mrcsapp,mrcsdocument,configdata,contextdata);

            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Prepare to exec DQL query", null, null);
            qry.setDQL(qualification);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing query", null, null);
            Date start = new Date();
            IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
            Date finish = new Date();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"query exec time: "+(finish.getTime()-start.getTime()), null, null);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterating through results", null, null);
            while (myObj1.next()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--process match: "+myObj1.getObjectId(), null, null);
                process(myObj1,sMgr,docbase,mrcsapp,mrcsdocument,configdata,contextdata);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--match processed, next!", null, null);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done with search, closing result set", null, null);            
            myObj1.close();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Exception occurred while iterating through search results "+docid, null, e);
            RuntimeException re = new RuntimeException("Exception occurred while looking up all versions of document "+docid,e);
            throw re;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done!", null, null);            
        
    }

}
