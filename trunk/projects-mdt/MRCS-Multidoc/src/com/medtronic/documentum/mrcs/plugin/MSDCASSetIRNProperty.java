/*
 * Created on Jul 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MSDCASSetIRNProperty implements MrcsDocumentCreationPlugin
{

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsDocumentCreationPlugin#processDocument(com.documentum.fc.client.IDfSession, java.lang.String, java.lang.String, java.lang.String, com.documentum.fc.client.IDfSysObject, java.util.Map, java.util.Map)
     */
    public String processDocument(IDfSession session, String mrcsapp, String gftype, String doctype, IDfSysObject docid, Map configdata, Map customdata) throws Exception 
    {
        /*-CONFIG-*/String m="processDocument - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Top of Set IRN plugin", null, null);
        String objectname = docid.getObjectName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"objectname: "+objectname, null, null);
        int irn = Integer.parseInt(objectname.substring(4));
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"parsed irn: "+irn, null, null);
        docid.setInt("msd_cas_irn",irn);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"IRN set, saving", null, null);
        docid.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done", null, null);
        return null;
    }

}
