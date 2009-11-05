/*
 * Created on Apr 11, 2005
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

 Filename       $RCSfile: CreateGroupingFolder.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:24 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.sbo.MrcsGroupingFolderSBO;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CreateGroupingFolder implements MrcsFolderCreationPlugin
{

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsFolderCreationPlugin#processFolder(com.documentum.fc.client.IDfSession, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     * This creates a GF subfolder automagically when a folder is created
     */
    public void processFolder(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception 
    {
        // get the GF type from the plugin config
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:CreateGroupingFolder.processFolder - getting the desired subfolder GF type from the plugin config",null,null);
        String subfoldergftype = (String)configdata.get("subfolder_gf_type");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:CreateGroupingFolder.processFolder - gf type for new subfolder: "+subfoldergftype,null,null);
        
        // create the GF
        try { 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:CreateGroupingFolder.processFolder - locating MRCS folder creation service",null,null);
            MrcsGroupingFolderSBO gfservice = new MrcsGroupingFolderSBO();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:CreateGroupingFolder.processFolder - invoking grouping folder creation",null,null);
            String newid = gfservice.createGroupingFolder(session.getLoginInfo(),objectid,mrcsapp,subfoldergftype,customdata,session.getSessionManager());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:CreateGroupingFolder.processFolder - grouping folder creation complete",null,null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:CreateGroupingFolder.processFolder - error during grouping folder creation process",e);
            throw e;
        }
    }

}
