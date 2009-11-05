/*
 * Created on Feb 7, 2005
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

 Filename       $RCSfile: MrcsSelectSubfolderOrGroupingFolderContainer.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:21 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.List;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.medtronic.documentum.mrcs.config.MrcsFolderConfigFactory;
import com.medtronic.documentum.mrcs.sbo.IMrcsGroupingFolderSBO;
import com.medtronic.documentum.mrcs.sbo.MrcsGroupingFolderSBO;
import com.thoughtworks.xstream.XStream;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsSelectSubfolderOrGroupingFolderContainer extends Component
{
    String m_foldertype, m_parentfolderid, m_application;


    public void onInit(ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.onInit - set form instance vars",null,null);
        m_foldertype = argumentlist.get("folderType");           // parent GF type
        m_parentfolderid = argumentlist.get("objectId");         // folder where folder creation request occurred
        m_application = argumentlist.get("mrcsApplication");     // MrcsApp we are dealing with
        super.onInit(argumentlist);
    }

    public void createGF(Control control, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - determine if single GF type or multiple GF types available",null,null);
        // get the ConfigBroker service
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - getting configbroker",null,null);
        MrcsFolderConfigFactory folderConfig;
        try { folderConfig = MrcsFolderConfigFactory.getFolderConfig();
        } catch (Exception e) {
            setReturnError("MSG_MRCS_CONFIG_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);
            return;
        }
        // determine if there are multiple GF types registered
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - getting list of GF types for this gf",null,null);
        List groupingFolderTypeList = folderConfig.getGroupingFolderTypesForGroupingFolder(m_foldertype,m_application);
        if (groupingFolderTypeList.size() == 1)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - only one GF found...",null,null);
            // jump to MrcsNewGroupingFolderContainer if we find only one match, check if there is a component to jump to...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - Only one grouping folder was found, jump to the Mrcs New Folder screens for that GF Type",null,null);
            String gfcomponent = folderConfig.getGroupingFolderComponent(m_application,(String)groupingFolderTypeList.get(0));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - configured gf component: "+gfcomponent,null,null);
            if (gfcomponent != null)
            {
                // there is a gfcomponent specified, so we need to collect data before creating the GF instance
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - redirecting to component",null,null);
                ArgumentList args = new ArgumentList();
                args.add("objectId", m_parentfolderid);
                args.add("folderType",(String)groupingFolderTypeList.get(0));
                args.add("mrcsApplication",m_application);           // pass the MRCS application
                setComponentJump(gfcomponent, args, getContext());
            } else {
                // no component is specified, so apparently we can create the new GF without any user input.
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - no component configured for gf creation, proceeding with GF creation",null,null);
                try {
                    //MrcsGroupingFolderService gfservice = MrcsGroupingFolderService.getService();
                    //gfservice.createGroupingFolder(m_parentfolderid,m_application,(String)groupingFolderTypeList.get(0),null);
                    //final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
                    //IMrcsGroupingFolderSBO gfservice = (IMrcsGroupingFolderSBO)DfClient.getLocalClient().new-Service(IMrcsGroupingFolderSBO.class.getName(), manager);
                    MrcsGroupingFolderSBO gfservice = new MrcsGroupingFolderSBO();
                    IDfSession session = getDfSession();
                    String newid = gfservice.createGroupingFolder(session.getLoginInfo(),m_parentfolderid,m_application,(String)groupingFolderTypeList.get(0),null,null);
                } catch (Exception e) {
                    /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSelectSFOrGF.createGF - error creating MRCS grouping folder",e);
                    ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_FOLDER_CREATION_ERROR", e);
                    setReturnError("MSG_MRCS_FOLDER_CREATION_ERROR", null, e);
                }
                setComponentReturn();
            }
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createGF - multiple GFs found, redirect to GF selection screen",null,null);
            // jump to MrcsSelectGroupingFolderType if we find multiple gf type matches
            XStream xs = new XStream();
            ArgumentList args = new ArgumentList();
            args.add("objectId", m_parentfolderid);
            args.add("groupingFolderTypeList",xs.toXML(groupingFolderTypeList)); // pass serialized list of grouping folder types
            args.add("mrcsApplication",m_application);           // pass the MRCS application
            setComponentJump("mrcsselectgroupingfolder", args, getContext()); // parameterize this component reference?
        }

    }

    public void createSF(Control control, ArgumentList argumentlist)
    {
        // determine if we need to collect info prior to SF creation...
        // does new subfolder require an interactive screen to collect data before creating the subfolder?
        // get the ConfigBroker service
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createSF - Creating SUBfolder",null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createSF - getting configbroker",null,null);
        MrcsFolderConfigFactory folderConfig;
        try { folderConfig = MrcsFolderConfigFactory.getFolderConfig();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSelectSFOrGF.createSF - error loading MRCS folder config",e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);
            setReturnError("MSG_MRCS_CONFIG_ERROR", null, e);
            return;
        }
        String subfoldercomponent = folderConfig.getSubfolderComponent(m_application,m_foldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createSF - is there a configured subfolder component? "+subfoldercomponent,null,null);
        if (subfoldercomponent != null)
        {
            // redirect to the specified component to collect subfolder information
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createSF - redirecting to subfolder component",null,null);
            ArgumentList args = new ArgumentList();
            args.add("objectId", m_parentfolderid);                  // current folder object
            args.add("mrcsApplication",m_application);           // pass the MRCS application
            args.add("folderType",m_foldertype);      // pass the GF type
            setComponentJump(subfoldercomponent,args,getContext());
        }
        else
        {
            // since no subfolder component specified, we assume we can generate the subfolder w/o any info from the user
            try {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createSF - creating subfolder...",null,null);
                //MrcsGroupingFolderService gfservice = MrcsGroupingFolderService.getService();
                //gfservice.createSubfolder(m_parentfolderid, m_application, m_foldertype, null);
                //final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
                //IMrcsGroupingFolderSBO gfservice = (IMrcsGroupingFolderSBO)DfClient.getLocalClient().new-Service(IMrcsGroupingFolderSBO.class.getName(), manager);
                MrcsGroupingFolderSBO gfservice = new MrcsGroupingFolderSBO();
                IDfSession session = getDfSession();
                String newid = gfservice.createSubfolder(session.getLoginInfo(),m_parentfolderid, m_application, m_foldertype, null,null);
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSelectSFOrGF.createSF - error creating MRCS subfolder",e);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_FOLDER_CREATION_ERROR", e);
                setReturnError("MSG_MRCS_FOLDER_CREATION_ERROR", null, e);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectSFOrGF.createSF - returning",null,null);
            setComponentReturn();
        }


    }

}
