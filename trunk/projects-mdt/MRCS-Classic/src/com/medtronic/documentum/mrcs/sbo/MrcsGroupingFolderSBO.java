/*
 * Created on Feb 10, 2005
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

 Filename       $RCSfile: MrcsGroupingFolderSBO.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:24 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo;


import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.config.MrcsFolderConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsFolderCreationPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsNamingFormatPlugin;

/**
 * @author muellc4
 *
 * Sept. 15th 2005:
 * - changed signatures to include an optional session manager, to provide transactional cleanness.
 * Basically, if the session manager is provided, it is assumed that session manager has an open
 * transaction on it, and the method should act as if it is part of that transaction. If no session
 * manager is provided (null), the method assumes that it is being invoked outside of a transaction,
 * and it should create a new sessionmanager and wrap itself in a new transaction.
 */
public class MrcsGroupingFolderSBO extends MrcsBaseSBO implements IMrcsGroupingFolderSBO
{
    
    public String createGroupingFolder(IDfLoginInfo logininfo, String parentfolderid, String mrcsapplication, String gftype, Map customdata, IDfSessionManager sMgr) throws Exception
    {
        
        // create a new mrcs grouping folder sysobject
        
        // get FolderConfig
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - begin service method",null,null);
        MrcsFolderConfigFactory folderconfig = MrcsFolderConfigFactory.getFolderConfig();
        String docbase = folderconfig.getApplicationDocbase(mrcsapplication);

        //MJH:3-4-2005:Create a session as a system account to create the grouping folder and static subfolders
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - create a session",null,null);
        if (sMgr == null)
        {
            sMgr = createSessionManager(docbase, folderconfig.getSystemUsername(mrcsapplication), folderconfig.getSystemPassword(mrcsapplication));
        }
        IDfSession session = sMgr.getSession(docbase);
         
        boolean bNewtran = false;        
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - start transaction?",null,null);
            if (!sMgr.isTransactionActive())
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - beginning transaction",null,null);
                bNewtran = true;
                sMgr.beginTransaction();
            }
            // re-access a session
            session = sMgr.getSession(docbase);
        
            // get ACL for GF:
            String folderACL = folderconfig.getGroupingFolderACL(mrcsapplication,gftype);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - assigning ACL: "+folderACL,null,null);
            String systemdomain = session.getServerConfig().getString("operator_name");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - system domain: "+systemdomain,null,null);
            IDfACL newACL = session.getACL(systemdomain, folderACL);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - ACL found?: "+(newACL != null),null,null);
            if (newACL == null) 
            {
                Exception e = new Exception("MRCS could not locate ACL for your user. ACL: "+folderACL+" user: "+session.getLoginUserName());
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - unable to look up folder acl: "+folderACL,null,e);
                throw e;
            }
            
            // generate the name of the folder
            String namegenerator = folderconfig.getGroupingFolderNamingFormatPlugin(mrcsapplication,gftype);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - setting folder name - instantiating name generator: "+namegenerator,null,null);
            MrcsNamingFormatPlugin namingplugin = (MrcsNamingFormatPlugin)Class.forName(namegenerator).newInstance();
            Map configdata = folderconfig.getGroupingFolderNamingFormatConfig(mrcsapplication,gftype);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - setting folder name - generating name... ",null,null);
            String foldername = namingplugin.generateName(session,mrcsapplication,gftype,parentfolderid,configdata,customdata);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - setting folder name - generated grouping folder name: "+foldername,null,null);
    
            // perform DFC operations to create the folder        
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - creating new grouping folder sysobject",null,null);
            IDfSysObject newFolderDctmObj = null;
            try {
                String objecttype = folderconfig.getGroupingFolderDocumentumType(mrcsapplication,gftype);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - -- object type: "+objecttype,null,null);        
                newFolderDctmObj = (IDfSysObject)session.newObject(objecttype);
                String newId = newFolderDctmObj.getObjectId().getId();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - -- object id: "+newId,null,null);        
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - -- setting acl: "+newACL,null,null);        
                newFolderDctmObj.setACL(newACL);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - -- setting name: "+foldername,null,null);        
                newFolderDctmObj.setObjectName(foldername);        
                // link the new grouping folder to the root folder in which we're creating it
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - -- linking to parent folder id: "+parentfolderid,null,null);
                newFolderDctmObj.link(parentfolderid);
                // need to set the mrcs_type attribute
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - -- setting gftype attr to: "+gftype,null,null);
                newFolderDctmObj.setString("mrcs_config", gftype);
                newFolderDctmObj.setString("mrcs_application", mrcsapplication);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - saving...",null,null);
                newFolderDctmObj.save();
                // exec the subfolder topo plugin, if need be
                List plugins = folderconfig.getGroupingFolderCreationPlugins(mrcsapplication,gftype);
                if (plugins != null)
                {
                    for (int i=0; i < plugins.size(); i++)
                    {
                        MrcsPlugin plugin = (MrcsPlugin)plugins.get(i);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - -- running subfolder topology plugin: "+plugin.PluginClassName,null,null);
                        MrcsFolderCreationPlugin topo = (MrcsFolderCreationPlugin)Class.forName(plugin.PluginClassName).newInstance();
                        topo.processFolder(session,mrcsapplication,gftype,newId,plugin.PluginConfiguration,customdata);
                    }
                }            
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - Error encountered while performing Grouping Folder creation operations with DFC",null,e);
                newFolderDctmObj.destroy(); // does this cascade any topo plugin subfolders as well? 
                throw (e);
            }
            if (bNewtran && sMgr.isTransactionActive()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - committing transaction",null,null);
                sMgr.commitTransaction();
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - returning...",null,null);
            return newFolderDctmObj.getObjectId().getId();
        } catch (Exception e) {
            if (sMgr.isTransactionActive()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createGroupingFolder - aborting transaction",null,null);
                sMgr.abortTransaction();
            }
            throw e;
        }
    }

    public String createSubfolder(IDfLoginInfo logininfo, String parentfolderid, String mrcsapplication, String gftype, Map customdata, IDfSessionManager sMgr) throws Exception
    {
        return createSubfolder(logininfo,parentfolderid,mrcsapplication,gftype,customdata,true,sMgr);
    }
    
    public String createSubfolder(IDfLoginInfo logininfo, String parentfolderid, String mrcsapplication, String gftype, Map customdata, boolean execplugins, IDfSessionManager sMgr) throws Exception
    {
        // create a new mrcs subfolder sysobject
        // get FolderConfig
        MrcsFolderConfigFactory folderconfig = MrcsFolderConfigFactory.getFolderConfig();
        String docbase = folderconfig.getApplicationDocbase(mrcsapplication);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - begin service method",null,null);
        if (sMgr == null)
        {
            sMgr = createSessionManager(docbase, folderconfig.getSystemUsername(mrcsapplication), folderconfig.getSystemPassword(mrcsapplication));
        }
        
        boolean bNewtran = false;        
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - start transaction for subfolder creation?",null,null);
            if (!sMgr.isTransactionActive())
            {
                bNewtran = true;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - beginning subfolder transaction",null,null);
                sMgr.beginTransaction();
            }
            // re-access a session
            IDfSession session = sMgr.getSession(docbase);

            // get ACL for GF (we reuse this for subfolders...right?)
            String folderACL = folderconfig.getSubfolderACL(mrcsapplication,gftype);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - assigning ACL: "+folderACL,null,null);
            String systemdomain = session.getServerConfig().getString("operator_name");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - system domain: "+systemdomain,null,null);
            IDfACL newACL = session.getACL(systemdomain, folderACL);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - ACL found?: "+(newACL == null),null,null);
            if (newACL == null) 
            {            
                Exception e = new Exception("MRCS could not locate ACL for your user. ACL: "+folderACL+" user: "+session.getLoginUserName());
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - Error encountered looking up ACL: "+folderACL,null,e);
                throw e;
            }
            
            // generate the name of the new subfolder
            String namegenerator = folderconfig.getSubfolderNamingFormatPlugin(mrcsapplication,gftype);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - setting folder name - instantiating name generator: "+namegenerator,null,null);
            MrcsNamingFormatPlugin namingplugin = (MrcsNamingFormatPlugin)Class.forName(namegenerator).newInstance();
            Map configdata = folderconfig.getSubfolderNamingFormatConfig(mrcsapplication,gftype);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - setting folder name - generating name... ",null,null);
            String foldername = namingplugin.generateName(session,mrcsapplication,gftype,parentfolderid,configdata,customdata);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - setting folder name - generated subfolder name: "+foldername,null,null);
    
            // determine reference to grouping folder root - is the current folder the base grouping folder, or another subfolder?
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - setting folder name - determining grouping folder root",null,null);
            IDfSysObject parentfolder = (IDfSysObject)session.getObject(new DfId(parentfolderid));
            String gfroot = null;
            if (parentfolder.hasAttr("mrcs_grouping_folder_root"))
            {
                // just copy this attr
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - setting folder name - copying current subfolder root attribute",null,null);
                gfroot = parentfolder.getString("mrcs_grouping_folder_root");
            } else {
                // this must be the gf root!
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - setting folder name - we are in the gf root already, so just use parentfolderid",null,null);
                gfroot = parentfolderid;
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - setting folder name - gfroot: "+gfroot,null,null);
            
            // perform DFC operations to create the folder        
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - creating new subfolder sysobject",null,null);
            IDfSysObject newFolderDctmObj = null;
            try {
                newFolderDctmObj = (IDfSysObject)session.newObject(folderconfig.getSubfolderDocumentumType(mrcsapplication,gftype));
                String newId = newFolderDctmObj.getObjectId().getId();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - -- object id: "+newId,null,null);        
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - -- setting acl: "+newACL,null,null);        
                newFolderDctmObj.setACL(newACL);
                //newFolderDctmObj.setOwnerName(logininfo.getUser());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - -- setting name: "+foldername,null,null);        
                newFolderDctmObj.setObjectName(foldername);        
                // link the new grouping folder to the root folder in which we're creating it
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - -- linking to parent folder id: "+parentfolderid,null,null);
                newFolderDctmObj.link(parentfolderid);
                // need to set the mrcs_type attribute
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - -- setting gftype attr to: "+gftype,null,null);
                newFolderDctmObj.setString("mrcs_config", gftype);
                newFolderDctmObj.setString("mrcs_application", mrcsapplication);
                newFolderDctmObj.setString("mrcs_grouping_folder_root",gfroot);
                
                // save this (although an attribute modification did, at least in memory, stay in effect when I did it after the save() call)
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - saving...",null,null);
                newFolderDctmObj.save();
                // exec the subfolder topo plugin, if need be
                List plugins = folderconfig.getSubfolderCreationPlugins(mrcsapplication,gftype);
                if (plugins != null && execplugins)
                {
                    for (int i=0; i < plugins.size(); i++)
                    {
                        MrcsPlugin plugin = (MrcsPlugin)plugins.get(i);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - -- running subfolder topology plugin: "+plugin.PluginClassName,null,null);
                        MrcsFolderCreationPlugin topo = (MrcsFolderCreationPlugin)Class.forName(plugin.PluginClassName).newInstance();
                        topo.processFolder(session,mrcsapplication,gftype,newId,plugin.PluginConfiguration,customdata);
                    }
                }            
                
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - Error encountered while performing subfolder creation operations with DFC",null,e);
                newFolderDctmObj.destroy(); // does this cascade any topo plugin subfolders as well? 
                throw (e);
            }
            if (bNewtran && sMgr.isTransactionActive()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - committing subfolder transaction",null,null);
                sMgr.commitTransaction();
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - returning...",null,null);
            return newFolderDctmObj.getObjectId().getId();
        } catch (Exception e) {
            if (sMgr.isTransactionActive()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - aborting subfolder transaction",null,null);
                sMgr.abortTransaction();
            }
            throw e;
        }
         
    }    

    public String createSystemSubfolder(String foldername, String foldertype, String folderACL, String parentfolderid, String mrcsapplication, String gftype, Map customdata, IDfSessionManager sMgr) throws Exception
    {
        // create a new mrcs system subfolder sysobject
        
        // get FolderConfig
        MrcsFolderConfigFactory folderconfig = MrcsFolderConfigFactory.getFolderConfig();
        String docbase = folderconfig.getApplicationDocbase(mrcsapplication);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - begin service method",null,null);
        // create new session manager if necessary
        if (sMgr == null)
        {
            sMgr = createSessionManager(docbase, folderconfig.getSystemUsername(mrcsapplication), folderconfig.getSystemPassword(mrcsapplication));
        }
        
        boolean bNewtran = false;        
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - start sysfolder transaction?",null,null);
            if (!sMgr.isTransactionActive())
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - beginning sysfolder transaction...",null,null);
                bNewtran = true;
                sMgr.beginTransaction();
            }
            // re-access a session
            IDfSession session = sMgr.getSession(docbase);
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - assigning ACL: "+folderACL,null,null);
            String systemdomain = session.getServerConfig().getString("operator_name");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - system domain: "+systemdomain,null,null);
            IDfACL newACL = session.getACL(systemdomain, folderACL);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - ACL found?: "+(newACL == null),null,null);
            if (newACL == null) 
            {            
                Exception e = new Exception("MRCS could not locate ACL for your user. ACL: "+folderACL+" user: "+session.getLoginUserName());
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsGroupingFolderSBO.createSubfolder - Error encountered looking up ACL: "+folderACL,null,e);
                throw e;
            }
    
            // determine reference to grouping folder root - is the current folder the base grouping folder, or another subfolder?
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - setting folder name - determining grouping folder root",null,null);
            IDfSysObject parentfolder = (IDfSysObject)session.getObject(new DfId(parentfolderid));
            String gfroot = null;
            if (parentfolder.hasAttr("mrcs_grouping_folder_root"))
            {
                // just copy this attr
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - setting folder name - copying current subfolder root attribute",null,null);
                gfroot = parentfolder.getString("mrcs_grouping_folder_root");
            } else {
                // this must be the gf root!
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - setting folder name - we are in the gf root already, so just use parentfolderid",null,null);
                gfroot = parentfolderid;
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - setting folder name - gfroot: "+gfroot,null,null);
            
            // perform DFC operations to create the folder                
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - creating new subfolder sysobject",null,null);
            IDfSysObject newFolderDctmObj = null;
            try {
                newFolderDctmObj = (IDfSysObject)session.newObject(foldertype);
                String newId = newFolderDctmObj.getObjectId().getId();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - -- object id: "+newId,null,null);        
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - -- setting acl: "+newACL,null,null);        
                newFolderDctmObj.setACL(newACL);
                //newFolderDctmObj.setOwnerName(logininfo.getUser());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - -- setting name: "+foldername,null,null);        
                newFolderDctmObj.setObjectName(foldername);        
                // link the new grouping folder to the root folder in which we're creating it
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - -- linking to parent folder id: "+parentfolderid,null,null);
                newFolderDctmObj.link(parentfolderid);
                // need to set the mrcs_type attribute
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - -- setting gftype attr to: "+gftype,null,null);
                newFolderDctmObj.setString("mrcs_config", gftype);
                newFolderDctmObj.setString("mrcs_application", mrcsapplication);
                // need to accomodate mrcs grouping folders...
                if (newFolderDctmObj.hasAttr("mrcs_grouping_folder_root"))newFolderDctmObj.setString("mrcs_grouping_folder_root",gfroot);
                
                // save this (although an attribute modification did, at least in memory, stay in effect when I did it after the save() call)
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - saving...",null,null);
                newFolderDctmObj.save();            
            } catch (Exception e) {
                newFolderDctmObj.destroy(); // does this cascade any topo plugin subfolders as well? 
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - Error encountered while performing subfolder creation operations with DFC",null,e);
                throw (e);
            }
            if (bNewtran && sMgr.isTransactionActive()) {
                // commit if the transaction is local to this method (might not be if this is part of a plugin sequence to, say, new GF)
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - committing sysfolder transaction...",null,null);
                sMgr.commitTransaction();
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - returning...",null,null);
            return newFolderDctmObj.getObjectId().getId();
        } catch (Exception e) {
            if (sMgr.isTransactionActive()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsGroupingFolderSBO.createSystemSubfolder - aborting sysfolder transaction...",null,null);
                sMgr.abortTransaction();
            }
            throw e;
        }
         
    }    

}
