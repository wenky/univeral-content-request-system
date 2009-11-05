/*
 * Created on May 24, 2005
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

 Filename       $RCSfile: MrcsACLPlugin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:56 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsACLPlugin extends MrcsBasePlugin implements IACLPlugin 
{


    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.IACLPlugin#applyACL(java.util.Map)
     */
    public void applyDocACL(IDfSessionManager sMgr, String mrcsapp, Map params) throws DfException 
    {
        try {
            // get config, retrieve session manager if necessary...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsACLPlugin:applyDocACL : getting ST config", null, null);
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsACLPlugin:applyDocACL : check if we need to create a session manager", null, null);
            String docbase = config.getApplicationDocbase(mrcsapp);
            boolean bSessionManagerCreated = (sMgr == null);
            if (bSessionManagerCreated) {            
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsACLPlugin:applyDocACL : creating session manager", null, null);
                sMgr = createSessionManager(docbase, config.getSystemUsername(mrcsapp), config.getSystemPassword(mrcsapp));            
            }
            IDfSession session = sMgr.getSession(docbase);
            
            String docACL = (String) params.get("ACLName");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsACLPlugin:applyDocACL : docACL " + docACL, null, null);
            IDfDocument docObject = (IDfDocument)params.get("ApplyToObject");
            IDfId _newObjId = docObject.getObjectId();            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsACLPlugin:applyDocACL : docACL " + docACL, null, null);

            String systemdomain = session.getServerConfig().getString("operator_name");
            IDfACL newACL = session.getACL(systemdomain, docACL);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsACLPlugin:applyDocACL : newACL " + newACL, null, null);
            IDfDocument doc = (IDfDocument) session.getObject(_newObjId);
            doc.setACL(newACL);
            doc.save();
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MrcsACLPlugin:applyDocACL : - Error encountered while applying ACL ;" + e, null, null);
        } 
    }

}
