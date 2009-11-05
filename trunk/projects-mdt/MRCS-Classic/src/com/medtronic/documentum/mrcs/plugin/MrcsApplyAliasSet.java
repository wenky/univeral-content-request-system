/*
 * Created on Apr 17, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfAliasSet;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsApplyAliasSet implements MrcsDocumentCreationPlugin
{

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsDocumentCreationPlugin#processDocument(com.documentum.fc.client.IDfSession, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     */
    public String processDocument(IDfSession session, String mrcsapp, String gftype, String doctype, IDfSysObject doc, Map configdata, Map customdata) throws Exception 
    {
        /*-CONFIG-*/String m="processDocument - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Top of Attach Alias Set plugin", null, null);

        // get alias set name to locate and apply
        String aliassetname = (String) configdata.get("AliasSet");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Alias set to attach is named: "+aliassetname, null, null);
        // should try to speed the qualification by restricting search to alias sets. How?
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Retrieving alias set", null, null);
        IDfAliasSet obj = (IDfAliasSet)session.getObjectByQualification("dm_alias_set where object_name = '"+aliassetname+"'");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Alias set retrieved? "+(obj!=null), null, null);
        if (obj != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Alias set found, attaching", null, null);
            IDfId objid = obj.getObjectId();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Alias set object id being attached: "+obj.getObjectId().getId(), null, null);
            doc.setId("r_alias_set_id",objid);
            doc.save();
        }
        else 
        {
            // error in alias set lookup
            /*-ERROR-*/DfLogger.error(this, m+"Alias set object id being attached: "+obj.getObjectId().getId(), null, null);
            RuntimeException re = new RuntimeException("Unable to locate AliasSet "+aliassetname);
            throw re;
        }        
        return null;
    }
}
