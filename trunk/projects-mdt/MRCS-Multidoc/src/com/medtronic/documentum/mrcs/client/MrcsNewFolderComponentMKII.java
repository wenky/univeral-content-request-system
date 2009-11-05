/*
 * Created on Mar 14, 2005
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

 Filename       $RCSfile: MrcsNewFolderComponentMKII.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:21 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.BooleanInputControl;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.medtronic.documentum.mrcs.config.MrcsFolderConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsCustomPageProcessorPlugin;
import com.medtronic.documentum.mrcs.sbo.IMrcsGroupingFolderSBO;
import com.medtronic.documentum.mrcs.sbo.MrcsGroupingFolderSBO;
import com.thoughtworks.xstream.XStream;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsNewFolderComponentMKII extends Component
{
    String m_selectedfoldertype;
    String m_parentfolderid, m_application;
    List   m_foldertypes;
    MrcsFolderConfigFactory m_folderconfig;
    
    public void onInit(ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - top of onInit, getting folder config",null,null);
        // get folder config
        try { 
            m_folderconfig = MrcsFolderConfigFactory.getFolderConfig();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderComponentMKII.onInit - error getting MRCS folder config",e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);
            setReturnError("MSG_MRCS_CONFIG_ERROR", null, e);
            setComponentReturn();
            return; // I think we escape out...don't we?
        }

        // extract args
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - get required component args",null,null);
        m_parentfolderid = argumentlist.get("objectId");            // folder where folder creation request occurred
        m_application    = argumentlist.get("mrcsApplication");     // MrcsApp we are dealing with
        String action    = argumentlist.get("action");              // what is the first page?

        try {
            // check permission
            IDfFolder parentfolder = (IDfFolder)getDfSession().getObject(new DfId(m_parentfolderid));
            int permission = parentfolder.getACL().getPermit(getDfSession().getLoginUserName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - user's permission level: "+permission,null,null);
            if (IDfACL.DF_PERMIT_WRITE > permission)
            {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderComponentMKII.onInit - Insufficient permit level to create MRCS folders in this folder");
                Exception e = new Exception("Insufficient permit level to create MRCS folders in this folder");
                setReturnError("MSG_MRCS_FOLDER_CREATE_PRIVLEDGE_ERROR", null, e);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_FOLDER_CREATE_PRIVLEDGE_ERROR", e);
                setComponentReturn();
                return;               
            }
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderComponentMKII.onInit - error getting MRCS folder permission level",e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_FOLDER_CREATE_PARENT_FOLDER_LOOKUP_ERROR", e);
            setReturnError("MSG_MRCS_FOLDER_CREATE_PARENT_FOLDER_LOOKUP_ERROR", null, e);
            setComponentReturn();
            return;             
        }
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - branching on requested folder action: "+action,null,null);
        if ("CreateSF".equals(action))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - subfolder creation requested, getting GF type",null,null);
            m_selectedfoldertype = argumentlist.get("groupingFolderType");              // what is the first page?
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - only subfolders, see if there is a subfolder component configured",null,null);
            // does new subfolder require an interactive screen to collect data before creating the subfolder?
            String subfoldercomponent = m_folderconfig.getSubfolderComponent(m_application,m_selectedfoldertype);
            if (subfoldercomponent != null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - custom subfolder screen registered: "+subfoldercomponent,null,null);
                setComponentPage(subfoldercomponent);
            }
            else
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - no custom screen, proceeding with subfolder creation",null,null);
                createMrcsSubfolder(null);                
            }
            
        }
        else if ("selectSForGF".equals(action))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - need to choose between a subfolder or a nested grouping folder",null,null);
            m_selectedfoldertype = argumentlist.get("groupingFolderType");
            XStream xs = new XStream();
            m_foldertypes = m_folderconfig.getGroupingFolderTypesForGroupingFolder(m_selectedfoldertype,m_application);
            setComponentPage("selectSForGF");            
        }
        else if ("CreateGF".equals(action))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - create grouping folder requested",null,null);
            // extract folder list from input args
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - deserializing folder type list",null,null);
            XStream xs = new XStream();
            m_foldertypes = (List)xs.fromXML(argumentlist.get("groupingFolderTypeList"));
            // determine if we need to display type selection page (if #types > 1)
            if (m_foldertypes.size() > 1)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - more than one type in list, need to prompt for type selection",null,null);
                setComponentPage("selecttype");
                setRefreshDataRequired(true,false);            
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - populating types",null,null);
                //populateTypeList();
            }
            else if (m_foldertypes.size() == 1)
            {
                // if there was no custom page, then the folder creation would have already been initiated by now
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - one and only one type specified, checking for custom screen",null,null);
                m_selectedfoldertype = (String)m_foldertypes.get(0);
                String gfcomponent = m_folderconfig.getGroupingFolderComponent(m_application,(String)m_foldertypes.get(0));
                if (gfcomponent != null)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - displaying custom screen: "+gfcomponent,null,null);
                    setComponentPage(gfcomponent);
                }
                else
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - no custom screen, proceeding with creation",null,null);
                    createMrcsGroupingFolder(null);                
                }
            }
            else
            {
                throw new WrapperRuntimeException("New Grouping Folder Component - invalid grouping folder list size: "+m_foldertypes.size());            
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - super() call down the object stack",null,null);
        super.onInit(argumentlist);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.OnInit - done with onInit()",null,null);
    }
    
    public void cancelNewFolder(Control control, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.cancelNewFolder - folder creation cancelled",null,null);
        setComponentReturn();
    }
    
    public void createSF(Control control, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createSF - create subfolder selected",null,null);
        // called by the "select subfolder or grouping folder to create" screen
        String subfoldercomponent = m_folderconfig.getSubfolderComponent(m_application,m_selectedfoldertype);
        if (subfoldercomponent != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createSF - need to display subfolder custom screen: "+subfoldercomponent,null,null);
            setComponentPage(subfoldercomponent);
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createSF - no custom screen, proceeding with creation",null,null);
            createMrcsSubfolder(null);
        }        
    }
    
    public void onRefreshData()
    {
        if ("selecttype".equals(getComponentPage()))
        {
            populateTypeList();            
        }
        super.onRefreshData();
    }

    public void createGF(Control control, ArgumentList argumentlist)
    {
        // called by the "select subfolder or grouping folder to create" screen        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createGF - 'create grouping folder' selected",null,null);
        if (m_foldertypes.size() > 1)
        {
            // go to gf type selection page, and initialize the type list
            setComponentPage("selecttype");
            setRefreshDataRequired(true,false);            
            //populateTypeList(); <-- this is done in onRefreshData(), which is triggered by setRefreshDataRequired()
        }
        else
        {
            // only 1 nested folder type defined, proceed with folder creation 
            String gfcomponent = m_folderconfig.getGroupingFolderComponent(m_application,(String)m_foldertypes.get(0));
            // select the lonely folder type...
            m_selectedfoldertype = (String)m_foldertypes.get(0); 
            if (gfcomponent != null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createGF - custom screen registered: "+gfcomponent,null,null);
                setComponentPage(gfcomponent);
            }
            else
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createGF - no custom screen, proceeding with GF creation",null,null);
                createMrcsGroupingFolder(null);                
            }
        }
    }
        
    // gf type selection page handler
    public void selectGroupingFolderType(Control control, ArgumentList argumentlist)
    {
        // get the selected grouping folder type
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.selectGroupingFolderType - getting selected gftype",null,null);
        DropDownList typelist = (DropDownList)getControl("groupingfoldertypes",DropDownList.class);
        m_selectedfoldertype = typelist.getValue();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.selectGroupingFolderType - gftype selected: "+m_selectedfoldertype,null,null);
        String gfcomponent = m_folderconfig.getGroupingFolderComponent(m_application,(String)m_selectedfoldertype);
        if (gfcomponent != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.selectGroupingFolderType - displaying custom screen: "+gfcomponent,null,null);
            setComponentPage(gfcomponent);
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.selectGroupingFolderType - no custom screens, proceeding with GF creation",null,null);
            createMrcsGroupingFolder(null);                
        }                
    }    
    
    // standard default custom page handler
    public void processCustomGroupingFolderPage(Control control, ArgumentList argumentlist)
    {
        // this is the generic method that MrcsNewGroupingFolder components should invoke from their submit button
        // collect custom widget data
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - top of create mrcs grouping folder",null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - searching for custom data",null,null);
        // TODO custom page processor plugin...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - top of process custom page call",null,null);
        Map customdata = null;
        // check if there is a configured custom processor for the page
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - looking for page processor",null,null);
        MrcsPlugin pageprocessor = m_folderconfig.getGroupingFolderComponentProcessor(m_application, m_selectedfoldertype);
        if (pageprocessor == null) {
            // use the default handler
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - no page processor defined, performing default processing",null,null);
            customdata = getCustomData();
        } else {
            try {
                // instantiate and execute custom handler - do this in an SBO? no...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - custom processor defined, instantiating "+pageprocessor.PluginClassName,null,null);
                MrcsCustomPageProcessorPlugin customprocessor = (MrcsCustomPageProcessorPlugin)Class.forName(pageprocessor.PluginClassName).newInstance();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - executing processor",null,null);
                customdata = customprocessor.process(this,pageprocessor.PluginConfiguration);
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - exception encountered during custom page processing");
                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_FOLDER_CREATE_CUSTOM_DATA_PROCESSING_ERROR", e);
                setReturnError("MSG_MRCS_FOLDER_CREATE_CUSTOM_DATA_PROCESSING_ERROR", null, e);
                throw new WrapperRuntimeException("Exception thrown while executing Mrcs Custom Page Processor "+pageprocessor.PluginClassName,e);
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomGroupingFolderPage - custom page processing complete, returned customdata: "+new XStream().toXML(customdata),null,null);
        createMrcsGroupingFolder(customdata);
    }

    public void processCustomSubfolderPage(Control control, ArgumentList argumentlist)
    {
        // this is the generic method that MrcsNewGroupingFolder components should invoke from their submit button
        // collect custom widget data
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - top of create mrcs subfolder",null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - searching for custom data",null,null);
        // TODO custom page processor plugin...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - top of process custom page call",null,null);
        Map customdata = null;
        // check if there is a configured custom processor for the page
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - looking for page processor",null,null);
        MrcsPlugin pageprocessor = m_folderconfig.getSubfolderComponentProcessor(m_application, m_selectedfoldertype);
        if (pageprocessor == null) {
            // use the default handler
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - no page processor defined, performing default processing",null,null);
            customdata = getCustomData();
        } else {
            try {
                // instantiate and execute custom handler - do this in an SBO? no...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - custom processor defined, instantiating "+pageprocessor.PluginClassName,null,null);
                MrcsCustomPageProcessorPlugin customprocessor = (MrcsCustomPageProcessorPlugin)Class.forName(pageprocessor.PluginClassName).newInstance();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - executing processor",null,null);
                customdata = customprocessor.process(this,pageprocessor.PluginConfiguration);
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - exception encountered during custom page processing");
                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_SUBFOLDER_CREATE_CUSTOM_DATA_PROCESSING_ERROR", e);
                setReturnError("MSG_MRCS_SUBFOLDER_CREATE_CUSTOM_DATA_PROCESSING_ERROR", null, e);
                throw new WrapperRuntimeException("Exception thrown while executing Mrcs Custom Page Processor "+pageprocessor.PluginClassName,e);
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolderComponentMKII.processCustomSubfolderPage - custom page processing complete, returned customdata: "+new XStream().toXML(customdata),null,null);
        createMrcsSubfolder(customdata);
    }

    public void createMrcsGroupingFolder(Map customdata)
    {
        try { 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsGroupingFolder - getting SBO",null,null);
            final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
            //IMrcsGroupingFolderSBO gfservice = (IMrcsGroupingFolderSBO)DfClient.getLocalClient().new-Service(IMrcsGroupingFolderSBO.class.getName(), manager);
            MrcsGroupingFolderSBO gfservice = new MrcsGroupingFolderSBO();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsGroupingFolder - creating folder",null,null);
            IDfSession session = getDfSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsGroupingFolder - calling GF creation method on SBO",null,null);
            // call create GF (null for SessionManager param since we want the "SBO" to create its own session manager for transaction cleanness)
            String newid = gfservice.createGroupingFolder(session.getLoginInfo(),m_parentfolderid,m_application,m_selectedfoldertype,customdata,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsGroupingFolder - id of new GF: "+newid,null,null);

        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderComponentMKII.createMrcsGroupingFolder - error while creating grouping folder",e);
            setReturnError("MSG_MRCS_GROUPING_FOLDER_CREATION_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_GROUPING_FOLDER_CREATION_ERROR", e);
        }

        // this magically returns us to the main DCTM screen...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsGroupingFolder - returning...",null,null);
        setComponentReturn();

    }

    public void createMrcsSubfolder(Map customdata)
    {
        // since no subfolder component specified, we assume we can generate the subfolder w/o any info from the user
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsSubfolder - getting folder service SBO",null,null);
            final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
            //IMrcsGroupingFolderSBO gfservice = (IMrcsGroupingFolderSBO)DfClient.getLocalClient().new-Service(IMrcsGroupingFolderSBO.class.getName(), manager);
            MrcsGroupingFolderSBO gfservice = new MrcsGroupingFolderSBO();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsSubfolder - invoking create subfolder on SBO",null,null);
            IDfSession session = getDfSession();
            // call createsubfolder (null for SessionManager param since we want the "SBO" to create its own session manager for transaction cleanness)
            String newid = gfservice.createSubfolder(session.getLoginInfo(),m_parentfolderid, m_application, m_selectedfoldertype, customdata,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.createMrcsSubfolder - id of new subfolder: "+newid,null,null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewFolderComponentMKII.createMrcsSubfolder - error while creating subfolder",e);
            setReturnError("MSG_MRCS_SUBFOLDER_CREATION_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_SUBFOLDER_CREATION_ERROR", e);
        }
        setComponentReturn();        
    }
    
    private void populateTypeList2()
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.populateTypeList2 - looking up type list control",null,null);
        DropDownList typelist = (DropDownList)getControl("groupingfoldertypes",DropDownList.class);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.populateTypeList2 - clearing options",null,null);
        typelist.clearOptions();
        for (int i=0; i < m_foldertypes.size(); i++)
        {
            Option newopt = new Option();
            newopt.setLabel((String)m_foldertypes.get(i));  // should I do a decode to a description attribute from config?
            newopt.setValue((String)m_foldertypes.get(i));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.populateTypeList - adding option "+newopt.getLabel(),null,null);
            typelist.addOption(newopt);
        }
        
    }
    
    private void populateTypeList()
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.populateTypeList - looking up type list control",null,null);
        DropDownList typelist = (DropDownList)getControl("groupingfoldertypes",DropDownList.class);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.populateTypeList - clearing options",null,null);
        typelist.clearOptions();
        for (int i=0; i < m_foldertypes.size(); i++)
        {
            Option newopt = new Option();
            newopt.setLabel((String)m_foldertypes.get(i));  // should I do a decode to a description attribute from config?
            newopt.setValue((String)m_foldertypes.get(i));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.populateTypeList - adding option "+newopt.getLabel(),null,null);
            typelist.addOption(newopt);
        }
        
    }

    public Map getCustomData()
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - scraping custom data into a hashmap - begin",null,null);
        HashMap customdata = new HashMap();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - getting form",null,null);
//        Form newfolderform = getForm();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - getting control iterator",null,null);
//        Iterator formcontrols = newfolderform.getContainedControls();
        Iterator formcontrols = getContainedControls();
        while (formcontrols.hasNext())
        {
            Control currentcontrol = (Control)formcontrols.next();
            // what is the name?
            String controlname = currentcontrol.getName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - current control name: "+controlname,null,null);
            // see if the control name matches the custom control naming convention
            String namingconventionprefix = "MrcsCustom";
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - comparing control name to see if prefixed by 'MrcsCustom'",null,null);
            if (controlname.length() > namingconventionprefix.length() && controlname.substring(0, namingconventionprefix.length()).compareTo(namingconventionprefix) == 0)
            {
                // get the key
                String customkey = controlname.substring(namingconventionprefix.length());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - control is an MrcsCustom field, key is: "+ customkey,null,null);
                // get the value
                // -- is it a text?
                if (currentcontrol instanceof StringInputControl )
                {
                    String customvalue = ((StringInputControl)currentcontrol).getValue();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - control is a String Input Control, value: "+customvalue,null,null);
                    customdata.put(customkey,customvalue);
                }
                else if (currentcontrol instanceof BooleanInputControl )
                {
                    Boolean custombool = new Boolean(((BooleanInputControl)currentcontrol).getValue());
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - control is a Boolean Input Control, value: "+custombool,null,null);
                    customdata.put(customkey,custombool);
                }
                else
                {
                    // log something about the unknown control type - for now, we assume that all custom fields should be StringInputControls
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - unknown control type: "+currentcontrol.getClass(),null,null);
                }
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewFolderComponentMKII.getCustomData - returning custom data hashmap",null,null);
        return customdata;
    }
    

}
