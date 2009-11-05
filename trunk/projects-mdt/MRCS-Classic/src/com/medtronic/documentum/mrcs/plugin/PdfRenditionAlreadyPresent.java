/*
 * Created on Jan 27, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PdfRenditionAlreadyPresent extends ValidateDocumentHasNoRendition 
{

    public boolean hasRendition(String format, String objectid, IDfSession session) throws DfException    
    {
        /*-CONFIG-*/String m="hasRendition";
        // stolen from ESignPrecondition
        boolean flag = false;
        IDfCollection idfcollection = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing rendition search", null, null);        
        String dql = "select r_object_id from dmr_content where any parent_id='" + objectid + "' and full_format='" + format + "' and rendition > 0";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- DQL ["+dql+"]", null, null);        
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL(dql);
        idfcollection = dfquery.execute(session, 0);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if search returned items, thus a rendition found", null, null);
        flag = idfcollection.next();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"found? "+flag, null, null);
        idfcollection.close();
        return flag;
        
    }
}
