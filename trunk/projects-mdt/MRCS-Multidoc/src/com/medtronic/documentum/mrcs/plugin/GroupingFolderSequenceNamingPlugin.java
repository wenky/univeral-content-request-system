/*
 * Created on Apr 19, 2005
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

 Filename       $RCSfile: GroupingFolderSequenceNamingPlugin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:54 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GroupingFolderSequenceNamingPlugin extends MrcsSequenceNamingPlugin
{
    public String generateName(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception
    {
        String supername = super.generateName(session,mrcsapp,gftype,objectid,configdata,customdata);
        String gfname = getGroupingFolderName(session,objectid);
        return supername+gfname;
    }
    public String generateName(IDfSession session, String mrcsapp, String gftype, String doctype, String objectid, Map configdata, Map customdata) throws Exception
    {
        String supername = super.generateName(session,mrcsapp,gftype,doctype,objectid,configdata,customdata);
        String gfname = getGroupingFolderName(session,objectid);
        return supername+gfname;        
    }
    
    String getGroupingFolderName(IDfSession session,String objectid) throws Exception
    {
        IDfSysObject parentfolder = (IDfSysObject)session.getObject(new DfId(objectid));
        // is this a mrcs subfolder?
        IDfSysObject gfroot = null;
        if (parentfolder.hasAttr("mrcs_grouping_folder_root"))
        {
            gfroot = (IDfSysObject)session.getObject(new DfId(parentfolder.getString("mrcs_grouping_folder_root")));
            return gfroot.getObjectName();
        }
        // is this the root of the current grouping folder?
        else if (parentfolder.hasAttr("mrcs_config"))
        {
            // this is the root (since it isn't a subfolder, but is an MRCS folder)
            return parentfolder.getObjectName();
        }
        throw new Exception("MRCS:GroupingFolderSequenceNamingPlugin.getGroupingFolderName - folder is not an MRCS folder");        
    }

}
