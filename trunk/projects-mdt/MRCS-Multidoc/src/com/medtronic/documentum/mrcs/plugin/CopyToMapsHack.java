/*
 * Created on Nov 10, 2005
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

 Filename       $RCSfile: CopyToMapsHack.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/09/20 19:13:25 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

/**
 * @author muellc4
 * 
 * This is a hack! Since Maps and standard NPP docs use the same lifecycle, but Maps needs source copies but npp docs need renditions,
 * and michael is gone and doing YET ANOTHER lifecycle is prohibitive, we write this plugin to intelligently decide whether we do a 
 * source copy (for Maps) or a rendition copy (for standard NPP docs), by checking the mrcs folder type that the document resides in.
 * Nice.
 */
public class CopyToMapsHack implements IStateTransitionPlugin, IMrcsLifecyclePlugin
{
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
        /*-CFG-*/String m="execute(new)-";        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"4.2 call invoked, calling 4.1.2 plugin",null,null);
		execute(sMgr,docbase,null,mrcsapp,mrcsdocument,config,context);
	}

	

    public void execute(IDfSessionManager smgr, String docbase, StateInfo currentstate, String mrcsapp, IDfDocument preversioned, IDfDocument postversioned, Map configdata, Map customdata) throws Exception
    {        
        /*-CONFIG-*/String m="execute(old) - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top of maps hack plugin - get doc's mrcs folder config", null, null);        
        // get the document's folder's type
        String mrcsfoldertype = postversioned.getString("mrcs_folder_config");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get configured plugin classname for mrcs folder config: "+mrcsfoldertype, null, null);        
        // match:
        String classname = (String)configdata.get("FolderType-"+mrcsfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"instantiating plugin class: "+classname, null, null);        
        // instantiate the plugin:
        IStateTransitionPlugin subplugin = (IStateTransitionPlugin)Class.forName(classname).newInstance();
        // execute
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing plugin class", null, null);        
        subplugin.execute(smgr,docbase,currentstate,mrcsapp,preversioned,postversioned,configdata,customdata);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"plugin class executed", null, null);        
    }
}
