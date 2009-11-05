/*
 * Created on Dec 17, 2004
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

 Filename       $RCSfile: MrcsNewGroupingFolderCustomComponent.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:21 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.BooleanInputControl;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.medtronic.documentum.mrcs.sbo.IMrcsGroupingFolderSBO;
import com.medtronic.documentum.mrcs.sbo.MrcsGroupingFolderSBO;

/**
 * @author muellc4
 *
 * this class provides basic generic functionality for custom MrcsNewGroupingFolder components. Hopefully,
 * new component xmls will be able to reuse this class for the behavior class, as its createMrcsGroupingFolder
 * method, which the submit button should invoke, can collect information from generic HTML widgets, assuming
 * they have matched the naming convention, and those custom fields can be sent to a plug-in class that provides
 * hooks before and after the creation of a new Mrcs Grouping Folder.
 */
public class MrcsNewGroupingFolderCustomComponent extends Component
{
    // several other classes use explicit instance variables for storing data in these components,
    // much like beehive's strategy. I assume that these objects are instantiated per-session, and therefore
    // are thread-safe across requests. Instances of the class are managed by the form processor.
    String m_foldertype, m_parentfolderid, m_application;


    public void onInit(ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.OnInit - getting from arglist",null,null);
        m_foldertype = argumentlist.get("folderType");
        m_parentfolderid = argumentlist.get("objectId");
        m_application = argumentlist.get("mrcsApplication");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.OnInit - gftype: "+m_foldertype,null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.OnInit - parent folder: "+m_parentfolderid,null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.OnInit - mrcsapp: "+m_application,null,null);
        // argumentlist will contain only those arguments listed in the component contract (in the component xml config file),
        // it strips out anything else not in that parameter list
        super.onInit(argumentlist);
    }

    // this is the default custom data scraper. If you have special stuff
    // like datagrids, you should probably override this in a subclass
    public Map getCustomData()
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - scraping custom data into a hashmap - begin",null,null);
        HashMap customdata = new HashMap();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - getting form",null,null);
//        Form newfolderform = getForm();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - getting control iterator",null,null);
//        Iterator formcontrols = newfolderform.getContainedControls();
        Iterator formcontrols = getContainedControls();
        while (formcontrols.hasNext())
        {
            Control currentcontrol = (Control)formcontrols.next();
            // what is the name?
            String controlname = currentcontrol.getName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - current control name: "+controlname,null,null);
            // see if the control name matches the custom control naming convention
            String namingconventionprefix = "MrcsCustom";
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - comparing control name to see if prefixed by 'MrcsCustom'",null,null);
            if (controlname.length() > namingconventionprefix.length() && controlname.substring(0, namingconventionprefix.length()).compareTo(namingconventionprefix) == 0)
            {
                // get the key
                String customkey = controlname.substring(namingconventionprefix.length());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - control is an MrcsCustom field, key is: "+ customkey,null,null);
                // get the value
                // -- is it a text?
                if (currentcontrol instanceof StringInputControl )
                {
                    String customvalue = ((StringInputControl)currentcontrol).getValue();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - control is a String Input Control, value: "+customvalue,null,null);
                    customdata.put(customkey,customvalue);
                }
                else if (currentcontrol instanceof BooleanInputControl )
                {
                    Boolean custombool = new Boolean(((BooleanInputControl)currentcontrol).getValue());
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - control is a Boolean Input Control, value: "+custombool,null,null);
                    customdata.put(customkey,custombool);
                }
                else
                {
                    // log something about the unknown control type - for now, we assume that all custom fields should be StringInputControls
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:CustomDocumentComponent.getCustomData - unknown control type: "+currentcontrol.getClass(),null,null);
                }
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.getCustomData - returning custom data hashmap",null,null);
        return customdata;
    }

    public void createMrcsGroupingFolder(Control control, ArgumentList argumentlist)
    {
        // this is the generic method that MrcsNewGroupingFolder components should invoke from their submit button
        // collect custom widget data
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.createMrcsGroupingFolder - top of create mrcs grouping folder",null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.createMrcsGroupingFolder - searching for custom data",null,null);
        Map customdata = getCustomData();

        try {
            //MrcsGroupingFolderService gfservice = MrcsGroupingFolderService.getService();
            //gfservice.createGroupingFolder(m_parentfolderid,m_application,m_foldertype,customdata);
            //*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.createMrcsGroupingFolder - getting folder SBO",null,null);
            //final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
            //IMrcsGroupingFolderSBO gfservice = (IMrcsGroupingFolderSBO)DfClient.getLocalClient().new-Service(IMrcsGroupingFolderSBO.class.getName(), manager);
            MrcsGroupingFolderSBO gfservice = new MrcsGroupingFolderSBO();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.createMrcsGroupingFolder - creating folder",null,null);
            IDfSession session = getDfSession();
            String newid = gfservice.createGroupingFolder(session.getLoginInfo(),m_parentfolderid,m_application,m_foldertype,customdata,null);

        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewGroupingFolder.createMrcsGroupingFolder - error while creating grouping folder",e);
            setReturnError("MSG_ERROR_CREATING_FOLDER", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_CREATING_FOLDER", e);
        }

        // this magically returns us to the main DCTM screen...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewGroupingFolder.createMrcsGroupingFolder - returning...",null,null);
        setComponentReturn();

    }
}
