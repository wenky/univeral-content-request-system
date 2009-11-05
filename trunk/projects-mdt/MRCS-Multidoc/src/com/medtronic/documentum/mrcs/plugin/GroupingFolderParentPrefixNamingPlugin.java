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

 Filename       $RCSfile: GroupingFolderParentPrefixNamingPlugin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:54 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GroupingFolderParentPrefixNamingPlugin implements MrcsNamingFormatPlugin, MrcsDocumentNamingFormatPlugin
{
    public String generateName(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.generateName(folder) - top of plugin",null,null);
        delimiter = (String)configdata.get("Delimiter");
        String gfname = getGroupingFolderParentName(session,objectid);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.generateName(folder) - GF: "+gfname,null,null);
        return gfname;
    }
    public String generateName(IDfSession session, String mrcsapp, String gftype, String doctype, String objectid, Map configdata, Map customdata) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.generateName(doc) - top of plugin",null,null);
        delimiter = (String)configdata.get("Delimiter");
        String gfname = getGroupingFolderParentName(session,objectid);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.generateName(doc) - GF: "+gfname,null,null);
        return gfname;
    }

    String getGroupingFolderParentName(IDfSession session,String objectid) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - top, getting parentfolder sysobject",null,null);
        IDfFolder parentfolder = (IDfFolder)session.getObject(new DfId(objectid));
        // is this a mrcs subfolder?
        IDfFolder gfroot = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - is this a mrcs subfolder, or the gf root?",null,null);

        if (parentfolder.hasAttr("mrcs_grouping_folder_root"))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - this is a subfolder, so return the value of the gf root id attr: "+parentfolder.getString("mrcs_grouping_folder_root"),null,null);
            gfroot = (IDfFolder)session.getObject(new DfId(parentfolder.getString("mrcs_grouping_folder_root")));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - getting parent path",null,null);
            String gfparentpath = gfroot.getFolderPath(0);
            gfparentpath = gfparentpath.substring(0,gfparentpath.lastIndexOf('/'));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - parent path: "+gfparentpath,null,null);            
            IDfSysObject gfparent = session.getFolderByPath(gfparentpath);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - gf root attr value: "+gfroot.getObjectName(),null,null);
            return getPrefixName(gfparent.getObjectName());
        }
        // is this the root of the current grouping folder?
        else if (parentfolder.hasAttr("mrcs_config"))
        {
            // this is the root (since it isn't a subfolder, but is an MRCS folder)
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - this is the gf root already: "+parentfolder.getObjectName(),null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - getting parent path",null,null);
            String gfparentpath = parentfolder.getFolderPath(0);
            gfparentpath = gfparentpath.substring(0,gfparentpath.lastIndexOf('/'));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - parent path: "+gfparentpath,null,null);            
            IDfSysObject gfparent = session.getFolderByPath(gfparentpath);
            return getPrefixName(gfparent.getObjectName());
        }
        /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - neither mrcs base grouping folder or mrcs subfolder detected - this is an error!");
        throw new Exception("MRCS:GroupingFolderPrefixNamingPlugin.getGroupingFolderName - folder is not an MRCS folder");
    }

    private String getPrefixName(String objectName)
    {
        int firstSpace = objectName.indexOf(delimiter);
        if (firstSpace == -1)
        {
            return objectName;
        }
        else
        {
            return objectName.substring(0,firstSpace);
        }
    }

    private String delimiter;

}
