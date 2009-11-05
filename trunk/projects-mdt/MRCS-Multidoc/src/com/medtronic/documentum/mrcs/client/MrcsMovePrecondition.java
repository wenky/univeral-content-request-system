/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.2.2
 Description	Determine if an object can be moved within a config
 Created on		11/15/2007

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: MrcsMovePrecondition.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2007/12/12 17:39:32 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;

import java.util.List;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.clipboard.Clipboard;
import com.documentum.web.formext.clipboard.IClipboard;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import com.medtronic.documentum.mrcs.config.MrcsFolderConfigFactory;

public class MrcsMovePrecondition implements IActionPrecondition
{

	List   m_foldertypes;
	MrcsFolderConfigFactory m_folderconfig;

    public String[] getRequiredParams() {
        return null;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
        /*-CONFIG-*/String m="queryExecute";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - checking for matching config values",null,null);

		// get folder config in grouping folder case
		try {
			m_folderconfig = MrcsFolderConfigFactory.getFolderConfig();
		} catch (Exception e) {
			/*-ERROR-*/DfLogger.error(this,m+ " - error getting MRCS folder config",null,e);
		}

		boolean moveValid = false;
        if(component.getClipboard().hasItems())
        {

            //iterate the clipboard
            String clipboardItems[] = component.getClipboard().getItemIds();
            for(int idxObjId = 0; idxObjId < clipboardItems.length; idxObjId++) {
                try
                {

					//get the thing to be moved and where its going
					String clipboardItemId = clipboardItems[idxObjId];
        			/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - object id: " + clipboardItemId,null,null);
					String targetFolderId = component.getClipboardPasteHandler().getDestinationId();
         			/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - folder id: " + targetFolderId,null,null);

         			//turn them into objects
                    IDfSysObject obj = (IDfSysObject)component.getDfSession().getObject(new DfId(clipboardItemId));
                    IDfSysObject folder = (IDfSysObject)component.getDfSession().getObject(new DfId(targetFolderId));

                    //get the associated type objects
					IDfType objType = obj.getType();
					IDfType folderType = folder.getType();

					//don't move to regular folders and cabinets, it will mess with UI
        			if (folderType.getName().equals("dm_folder") || folderType.getName().equals("dm_cabinet")) {
						moveValid = false;

					//let them move it to a mrcs folder
					} else if (folderType.isSubTypeOf("m_mrcs_folder")){

						//are the application values the same?
						boolean appValid = (obj.getString("mrcs_application").equals(folder.getString("mrcs_application")));
						/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - appValid: " + appValid,null,null);

						//set the config value to false, "config" means different things to different objects
						boolean configValid = false;

						//is the thing to be moved an mrcs document?
						if (objType.isSubTypeOf("m_mrcs_document") || objType.getName().equalsIgnoreCase("m_mrcs_document")) {
							configValid = (obj.getString("mrcs_folder_config").equals(folder.getString("mrcs_config")));
							/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - doc configValid: " + configValid,null,null);

						//is it a grouping folder?  look up if it is allowed in the destination folder
						} else if ((objType.getName().equalsIgnoreCase("m_mrcs_grouping_folder")) && appValid){
							List gfList = m_folderconfig.getGroupingFolderTypesForGroupingFolder(folder.getString("mrcs_config"), obj.getString("mrcs_application"));
							configValid = (gfList.contains(obj.getString("mrcs_config")));
							/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - gf configValid: " + configValid,null,null);

						//is it a subfolder?  check to see if its being moved to a grouping folder or another subfolder
						} else if (objType.getName().equalsIgnoreCase("m_mrcs_subfolder")){
							boolean subfolderConfig = (obj.getString("mrcs_config").equals(folder.getString("mrcs_config")));
							boolean folderValid = false;
							if (folderType.getName().equals("m_mrcs_grouping_folder")) {
								folderValid = obj.getString("mrcs_grouping_folder_root").equals(folder.getString("r_object_id"));
							} else {
								folderValid = obj.getString("mrcs_grouping_folder_root").equals(folder.getString("mrcs_grouping_folder_root"));
							}
							configValid = (subfolderConfig && folderValid);
							/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - sf configValid: " + configValid,null,null);
						}

						//aggregate the conditions
						moveValid = appValid && configValid;
					}
                }
                catch(DfException e)
                {
					//log the error, but we don't really care
					/*-ERROR-*/DfLogger.error(this,m+"there was an error",null,e);
                    idxObjId++;
                }
			}
		}
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - moveValid pre-return: " + moveValid,null,null);
		return moveValid;
	}
}
