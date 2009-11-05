/*
 * Created on Nov 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: ValidateDocumentHasNoRendition.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:59 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ValidateDocumentHasNoRendition implements IMrcsWorkflowValidation

{
    public boolean validate(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, List errmsgs, Map configdata, Map customdata) throws Exception
    {
        boolean flag = checkRenditions(sMgr,docbase,doc,mrcsapp,wfname,configdata,customdata);
        if (!flag)
        {
            Map error = new HashMap();
            error.put("Error","ERR_RENDITION_VALIDATION_HAS_RENDITIONS");
            errmsgs.add(error);            
        }
        return flag;
    }

    public boolean checkRenditions(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, Map configdata, Map customdata) throws Exception
    {
        // stolen from ESignPrecondition
        /*-CONFIG-*/String m = "checkRenditions - ";
        boolean flag = false;
        String format = (String)configdata.get("RenditionFormat");
        String objectId = doc.getObjectId().getId();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        if(objectId == null || format == null)
            throw new IllegalArgumentException("Object ID and format must be valid!");
        IDfCollection idfcollection = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing rendition search", null, null);        
        IDfSession session = sMgr.getSession(docbase);
        DfQuery dfquery = new DfQuery();
        String dql = "select r_object_id from dmr_content where any parent_id='" + objectId + "' and full_format='" + format + "' and rendition > 0";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- DQL ["+dql+"]", null, null);        
        dfquery.setDQL(dql);
        idfcollection = dfquery.execute(session, 0);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if search returned items, thus a rendition found", null, null);
        flag = idfcollection.next();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"found? "+flag, null, null);
        idfcollection.close();
        // return inverse of flag: we want to return true if no renditions found, false if they were found
        return !flag;
    }


}
