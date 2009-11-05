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

 Filename       $RCSfile: MrcsSelectGroupingFolderContainer.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:21 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.List;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Option;
import com.documentum.web.formext.component.Component;
import com.medtronic.documentum.mrcs.config.MrcsFolderConfigFactory;
import com.medtronic.documentum.mrcs.sbo.MrcsGroupingFolderSBO;
import com.thoughtworks.xstream.XStream;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsSelectGroupingFolderContainer extends Component
{

    String m_parentfolderid, m_application;
    List m_foldertypes;

    public void onInit(ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.onInit - set form instance vars",null,null);
        m_parentfolderid = argumentlist.get("objectId");         // folder where folder creation request occurred
        m_application = argumentlist.get("mrcsApplication");     // MrcsApp we are dealing with
        // deserialize type list
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.onInit - deserialize gftype list",null,null);
        XStream xs = new XStream();
        m_foldertypes = (List)xs.fromXML(argumentlist.get("groupingFolderTypeList"));
        // configure dropdown list
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.onInit - configuring gftype dropdown",null,null);
        DropDownList typelist = (DropDownList)getControl("groupingfoldertypes",DropDownList.class);
        for (int i=0; i < m_foldertypes.size(); i++)
        {
            Option newopt = new Option();
            newopt.setLabel((String)m_foldertypes.get(i));  // should I do a decode to a description attribute from config?
            newopt.setValue((String)m_foldertypes.get(i));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.onInit - adding option "+newopt.getLabel(),null,null);
            typelist.addOption(newopt);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.onInit - done with init",null,null);
        super.onInit(argumentlist);
    }

    public void createGroupingFolder(Control control, ArgumentList argumentlist)
    {
        // get the ConfigBroker service
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - getting configbroker",null,null);
        MrcsFolderConfigFactory folderConfig;
        try { folderConfig = MrcsFolderConfigFactory.getFolderConfig();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - error getting MRCS folder config",e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);
            setReturnError("MSG_MRCS_CONFIG_ERROR", null, e);
            return; // I think we escape out...don't we?
        }

        // get the selected grouping folder type
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - getting selected gftype",null,null);
        DropDownList typelist = (DropDownList)getControl("groupingfoldertypes",DropDownList.class);
        String gftype = typelist.getValue();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - gftype selected: "+gftype,null,null);

        // jump to MrcsNewGroupingFolderContainer if we find only one match, check if there is a component to jump to...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - determine if we can immediately create the folder, or if extra info needed",null,null);
        String gfcomponent = folderConfig.getGroupingFolderComponent(m_application,(String)gftype);
        if (gfcomponent != null)
        {
            // there is a gfcomponent specified, so we need to collect data before creating the GF instance
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - jumping to component: "+gfcomponent,null,null);
            ArgumentList args = new ArgumentList();
            args.add("objectId", m_parentfolderid);
            args.add("folderType",gftype);
            args.add("mrcsApplication",m_application);           // pass the MRCS application
            setComponentJump(gfcomponent, args, getContext());
        } else {
            // no component is specified, so apparently we can create the new GF without any user input.
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - no component specified, proceeding with creation of folder",null,null);
            try {
                //MrcsGroupingFolderService gfservice = MrcsGroupingFolderService.getService();
                //gfservice.createGroupingFolder(m_parentfolderid,m_application,gftype,null);
                //final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
                //IMrcsGroupingFolderSBO gfservice = (IMrcsGroupingFolderSBO)DfClient.getLocalClient().new-Service(IMrcsGroupingFolderSBO.class.getName(), manager);
                MrcsGroupingFolderSBO gfservice = new MrcsGroupingFolderSBO();
                IDfSession session = getDfSession();
                String newid = gfservice.createGroupingFolder(session.getLoginInfo(),m_parentfolderid,m_application,gftype,null,null);
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - error creating MRCS grouping folder",e);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_FOLDER_CREATION_ERROR", e);
                setReturnError("MSG_MRCS_FOLDER_CREATION_ERROR", null, e);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSelectGroupingFolderContainer.createGroupingFolder - returning...",null,null);
            setComponentReturn();
        }

    }


}
