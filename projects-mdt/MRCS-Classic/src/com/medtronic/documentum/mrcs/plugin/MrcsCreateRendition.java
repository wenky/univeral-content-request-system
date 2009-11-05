/*
 * Created on May 25, 2005
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

 Filename       $RCSfile: MrcsCreateRendition.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:56 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsCreateRendition extends MrcsBasePlugin implements IRenditionPlugin {

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.IRenditionPlugin#createRendition(java.util.Map)
     */
    public void createRendition(Map params) throws DfException {
        try{   
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsCreateRendition:createRendition : Creating PDF Rendition for Document... " , null, null);
            IDfDocument doc = (IDfDocument)params.get("DocObject");
            //setSysSession();
            String appName = doc.getString("mrcs_application");    
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsCreateRendition:createRendition : queuing rendition request " , null, null);
            doc.queue("dm_autorender_win31", "rendition", 0, false, null, "rendition_req_ps_pdf");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsCreateRendition:createRendition : rendition request queued" , null, null);
        }catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MrcsCreateRendition:createRendition : - Error encountered while creating rendition ;", null, e);
            throw new RuntimeException("error in post promote rendition creation",e);
        }/*finally{
            releaseSysSession();
        }*/
    }

}
