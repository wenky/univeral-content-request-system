/*
 * Created on Apr 18, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import java.util.Iterator;
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
public class MrcsSetAttributes implements MrcsDocumentCreationPlugin
{

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsDocumentCreationPlugin#processDocument(com.documentum.fc.client.IDfSession, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     */
    public String processDocument(IDfSession session, String mrcsapp, String gftype, String doctype, IDfSysObject doc, Map configdata, Map customdata) throws Exception 
    {
        /*-CONFIG-*/String m="processDocument - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Top of Set Attributes plugin", null, null);
        // simple: iterate through configdata keys as attr names and set the value (assumes string for now, add autodetect for type?)
        Iterator attrs = configdata.keySet().iterator();
        while (attrs.hasNext())
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"process next key", null, null);
            String attr = (String)attrs.next();
            String value = (String)configdata.get(attr);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"set "+attr+" to "+value, null, null);
            doc.setString(attr,value);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving changes", null, null);
        doc.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done", null, null);
        return null;       
        
    }

}
