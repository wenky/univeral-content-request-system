/*
 * Created on Dec 16, 2004
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

 Filename       $RCSfile: MrcsNewFolderContainerMKII.java,v $
 Author         $Author: wallam1 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2007/04/02 20:23:33 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.List;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.webcomponent.library.create.NewFolderContainer;
import com.medtronic.documentum.mrcs.config.MrcsFolderConfigFactory;
import com.medtronic.documentum.mrcs.config.dto.MrcsRootFolderMatchDTO;
import com.medtronic.documentum.mrcs.sbo.IMrcsGroupingFolderSBO;
import com.thoughtworks.xstream.XStream;

/**
 * @author muellc4
 *
 * Overrides NewFolderContainer to support MRCS folder extensions to webtop.
 * - detects if this is a valid creation path for MRCS Grouping Folders
 *   - if so, it jumps to MrcsNewGroupingFolderContainer
 * - MRCS subfolder creation is handled by the scoped NewFolderContainer
 * - otherwise, process normally per default Webtop/Documentum UI for folder creation
 */
public class MrcsNewFolderContainerMKII extends NewFolderContainer
{
    public void onInit(ArgumentList argumentlist)
    {
        // get the documentum object id of the current folder
        String currentFolderObjectId = argumentlist.get("objectId");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - Checking what kind of folder this is: "+currentFolderObjectId,null,null);

        // translate the objectid to an actual path
        String currentFolderPath = FolderUtil.getFolderPath(currentFolderObjectId,0);
        String currentDocBase = null;
        String currentMrcsApplication = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - Folderpath: "+currentFolderPath,null,null);

        // get the ConfigBroker service
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - getting configbroker",null,null);
        MrcsFolderConfigFactory folderConfig;
        try { folderConfig = MrcsFolderConfigFactory.getFolderConfig();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderContainer.OnInit - could not load folder config",e);
            setReturnError("MSG_MRCS_CONFIG_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);
            return; // I think we escape out...don't we?
        }

        // check if we are already in an MrcsGroupingFolder or one of its subfolders
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - checking if this is a MrcsGroupingFolder",null,null);
        List groupingFolderTypeList = null; // valid GF types that can be created in this folder
        String groupingFolderType = null; // GF type of the current folder, if it is a grouping folder
        IDfSysObject currentFolder = null;
        String mrcsFolderAction = "NONE";
        boolean bSubfolderProcessingFlag = false; // indicates if MRCS subfolder processing is to occur
        try {
            currentFolder = (IDfSysObject)getDfSession().getObjectByPath(currentFolderPath);
            currentDocBase = DocbaseUtils.getDocbaseNameFromId(currentFolder.getObjectId());
            if (currentFolder.hasAttr("mrcs_application")) // system subfolders will lock out new folder via ACLs, or by not being MRCS folders
            {
                bSubfolderProcessingFlag = true;

                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - MrcsGroupingFolder detected",null,null);
                // look up the MRCS Application this GF belongs to
                currentMrcsApplication = currentFolder.getString("mrcs_application");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - Application: "+currentMrcsApplication,null,null);

                groupingFolderType = currentFolder.getString("mrcs_config");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - GF Type: "+groupingFolderType,null,null);

                // we are in a MRCS grouping folder...look up its grouping folders in the config
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - Getting grouping folders registered for this GF Type",null,null);
                groupingFolderTypeList = folderConfig.getGroupingFolderTypesForGroupingFolder(groupingFolderType, currentMrcsApplication);                

            }
        } catch (Exception dfe) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderContainer.OnInit - error while detecting if current folder is a mrcs folder",dfe);
            setReturnError("MSG_DFC_ERROR", null, dfe);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_FOLDER_IDENTIFICATION_ERROR", dfe);
            return; // I think we escape out...don't we?
        }

        // if it wasn't a grouping folder or a subfolder (gftlist is still null), check for a mrcs root folder match
        try {
            if (!bSubfolderProcessingFlag)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - Not an MRCS Grouping Folder, does it match a root folder?",null,null);
                MrcsRootFolderMatchDTO matchesDTO = folderConfig.getGroupingFolderTypesForRootFolder(currentFolderPath,currentDocBase);
                if (matchesDTO != null)
                {
                    // we got some matches...
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - matches found on MrcsRootFolders",null,null);
                    currentMrcsApplication = matchesDTO.mrcsApplication;
                    groupingFolderTypeList = matchesDTO.groupingFolderTypes;
                }
            }
        } catch (Exception dfe) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderContainer.OnInit - error while detecting a root folder match",dfe);
            setReturnError("MSG_DFC_ERROR", null, dfe);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_ROOT_FOLDER_MATCHING_ERROR", dfe);
            return; // I think we escape out...don't we?
        }

        // allrighty, if we found grouping folders we can create, redirect to the screens that handle them
        // check if we are doing an MRCS Subfolder or choosing b/w MRCS Subfolder and MRCS Grouping Folder
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - check if subfolder creation was detected",null,null);
        // NEW(09/16/05) - check for subfolder creation disallow flag:
        // - we should be okay on the second call of this condition, since bSubfolderProcessingFlag is only true if we are 
        //   currently in a MRCS folder, so no NullPointerExceptions should be thrown...
        if (bSubfolderProcessingFlag && !folderConfig.getGroupingFolderSubfolderDisallowFlag(currentMrcsApplication,groupingFolderType))
        {
            // handle Mrcs Subfolder or nested MrcsGroupingFolder selection screen
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - check if both grouping folders and subfolders are valid options",null,null);
            if (groupingFolderTypeList.size() < 1)
            {
                // ONLY subfolder: jump to MrcsNewFolderComponent with action = CreateSF
                ArgumentList args = new ArgumentList();
                args.add("objectId", currentFolderObjectId);
                args.add("mrcsApplication",currentMrcsApplication);           // pass the MRCS application
                args.add("action","CreateSF");
                args.add("groupingFolderType",groupingFolderType);      // pass the GF type
                setComponentJump("mrcscreatefolder", args, getContext());                
            }
            else
            {
                // user needs to specify if they want to create a nested Mrcs Grouping Folder, or a MrcsSubfolder
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - jumping to (GF or SF) selection screen",null,null);
                XStream xs = new XStream();
                ArgumentList args = new ArgumentList();
                args.add("objectId", currentFolderObjectId);                  // current folder object
                args.add("mrcsApplication",currentMrcsApplication);           // pass the MRCS application
                args.add("action","selectSForGF");
                args.add("groupingFolderType",groupingFolderType);
                args.add("groupingFolderTypeList",xs.toXML(groupingFolderTypeList)); // pass serialized list of grouping folder types
                setComponentJump("mrcscreatefolder",args,getContext());
            }
        }
        else if (bSubfolderProcessingFlag && folderConfig.getGroupingFolderSubfolderDisallowFlag(currentMrcsApplication,groupingFolderType))
        {
            //okay, we are in a MRCS folder, but subfolder creation is disabled. Check if nested grouping folders can be created...
            // this seemingly redundant case prevents the normal NewFolder.super() from executing, which can't in an MRCS folder... 
            if (groupingFolderTypeList.size() > 0)
            {
                // jump to MrcsSelectGroupingFolderType if we find multiple Config matches
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - Multiple possible GF Types, redirect to GF Folder Type selection screen",null,null);
                ArgumentList args = new ArgumentList();
                XStream xs = new XStream();
                args.add("objectId", currentFolderObjectId);
                args.add("mrcsApplication",currentMrcsApplication);           // pass the MRCS application
                args.add("action","CreateGF");
                args.add("groupingFolderTypeList",xs.toXML(groupingFolderTypeList)); // pass serialized list of grouping folder types
                setComponentJump("mrcscreatefolder", args, getContext());
            }
            else
            {
                // we are in a MRCS folder, but no user-created subfolders are allowed, and grouping folders aren't nestable, so there is nothing
                // to do (we may need to disable the new folder menu option...)
            	// Modified by Sujeet on 02/04/2007 to Fix the Issue Found by Michael Wallach
            	// Issue was when we are trying to Create a Grouping Folder inside a Grouping Folder, no Error Messages was Displaying
            	ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_CREATING_GROUPING_FOLDER", null);            	           	
                setComponentReturn();
            }
        }
        else if (groupingFolderTypeList == null)
        {
            // no matches for MRCS...proceed with default webtop folder creation...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - No MRCS-relevant folders were found, proceeding with standard Documentum folder creation",null,null);
            super.onInit(argumentlist);
        }
        else if (groupingFolderTypeList.size() > 0)
        {
            // jump to MrcsSelectGroupingFolderType if we find multiple Config matches
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - Multiple possible GF Types, redirect to GF Folder Type selection screen",null,null);
            ArgumentList args = new ArgumentList();
            XStream xs = new XStream();
            args.add("objectId", currentFolderObjectId);
            args.add("mrcsApplication",currentMrcsApplication);           // pass the MRCS application
            args.add("action","CreateGF");
            args.add("groupingFolderTypeList",xs.toXML(groupingFolderTypeList)); // pass serialized list of grouping folder types
            setComponentJump("mrcscreatefolder", args, getContext());
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderContainer.OnInit - end of OnInit()",null,null);
    }

}
