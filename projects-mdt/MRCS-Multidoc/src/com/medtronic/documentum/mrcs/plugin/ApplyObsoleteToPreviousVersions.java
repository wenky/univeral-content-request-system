package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

public class ApplyObsoleteToPreviousVersions extends ThereCanOnlyBeOne
{

    public String getDQL(IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "ApplyObsoleteToPreviousVersions.getDQL()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get postversioned document's new id and doctype", null, null);
        String docid = PostVersionedDoc.getObjectId().getId();        
        String doctype = PostVersionedDoc.getTypeName();
               
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for looking up both newer and older versions of the current doc", null, null);
        String qualification = "select r_object_id from "+doctype+" (ALL) "+
                               "where NOT r_object_id = '"+docid+"' AND i_chronicle_id in " +
                                    "(select i_chronicle_id from dm_sysobject where r_object_id = '"+docid + "')";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"composed DQL: "+qualification, null, null);
        return qualification;
        
    }

    // apply the specified ACL to all matches, flip obsolete and retired flags
    public void process(IDfCollection match, IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "ApplyObsoleteToPreviousVersions.process()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        
        // lookup lifecycle to attach in config, and get LC object from docbase
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"look up ACL name to apply from plugin config", null, null);
        String aclname  = (String)configdata.get("ACL");

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"LOOK up system domain user", null, null);
        String systemdomain = session.getServerConfig().getString("operator_name");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--domain user: "+systemdomain, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get acl", null, null);
        IDfACL newACL = session.getACL(systemdomain, aclname);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- found? "+(newACL != null), null, null);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get match's object id", null, null);
        String id = getMatchObjectId(match);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up document", null, null);
        IDfDocument doc = (IDfDocument)session.getObject(new DfId(id));
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"applying new acl", null, null);
        doc.setACL(newACL);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting obsolete flag", null, null);
        doc.setBoolean("obsolete",true);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"turning off retired flag", null, null);
        doc.setBoolean("retired",false);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving doc", null, null);
        doc.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"RELEASING session", null, null);
        sMgr.release(session);
    	
    }


}
