/*
 * Created on Apr 20, 2005
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

 Filename       $RCSfile: StaticName.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:59 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StaticName implements MrcsDocumentNamingFormatPlugin, MrcsNamingFormatPlugin
{

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsDocumentNamingFormatPlugin#generateName(com.documentum.fc.client.IDfSession, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     */
    public String generateName(IDfSession session, String mrcsapp, String gftype, String doctype, String parentfolderid, Map configdata, Map customdata) throws Exception {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticName.generateFolders(folder) - top",null,null);
        String staticname = (String)configdata.get("Name");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticName.generateFolders(folder) - name: "+staticname,null,null);
        return staticname;
    }

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsNamingFormatPlugin#generateName(com.documentum.fc.client.IDfSession, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     */
    public String generateName(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticName.generateFolders(doc) - top",null,null);
        String staticname = (String)configdata.get("Name");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticName.generateFolders(doc) - name: "+staticname,null,null);
        return staticname;
    }

}
