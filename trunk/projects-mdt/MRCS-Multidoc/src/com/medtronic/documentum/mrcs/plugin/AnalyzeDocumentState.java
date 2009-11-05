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

 Filename       $RCSfile: AnalyzeDocumentState.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:53 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * This validates if a document is in the proper state. Valid states are configured by adding keys to the plugin configuration:
 *   <string>ValidState_1</string><string>In-Progress</string>
 *   <string>ValidState_2</string><string>In-Review</string>
 * 
 * typically there will only be one. 
 */
public class AnalyzeDocumentState implements IMrcsWorkflowValidation

{
    public boolean validate(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, List errmsgs, Map configdata, Map customdata) throws Exception
    {
        /*-CONFIG-*/String m = "validate - ";
        // get document's current state
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        String docstate = doc.getCurrentStateName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"current doc state: "+docstate, null, null);
        // check state against configured valid state names
        boolean valid = true;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scanning plugin configuration for allowable states", null, null);
        Iterator keys = configdata.keySet().iterator();
        while (keys.hasNext())
        {
            String configkey = (String)keys.next();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checking current config key to see if it is a ValidState configuration key: "+configkey, null, null);
            if (configkey.matches("ValidState(.*)"))
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"configuration key was a match for a ValidState_#", null, null);
                String validstate = (String)configdata.get(configkey);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checking if docstate ["+docstate+"] equals valid state ["+validstate+"]", null, null);
                if (docstate.equals(validstate))
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"match found", null, null);
                    valid = false;
                }
            }
        }
        // compose analysis message
        if (valid)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"invalid state, need err msg", null, null);
            Map error = new HashMap();
            error.put("Analysis","ERR_VALIDATION_INVALID_DOCUMENT_LIFECYCLE_STATE");
            Object[] params = {docstate};
            error.put("Data",params);
            errmsgs.add(error);
        }
        
        return valid;
    }


}
