/*
 * Created on Apr 4, 2005
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

 Filename       $RCSfile: MrcsMenuBar.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:45 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Menu;
import com.documentum.web.form.control.Panel;
import com.documentum.webcomponent.navigation.homecabinet.HomeCabinetService;
import com.documentum.webtop.app.AppSessionContext;
import com.documentum.webtop.app.ApplicationLocation;
import com.documentum.webtop.webcomponent.menubar.MenuBar;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsMenuBar extends MenuBar {

    /**
     *
     */
    public MrcsMenuBar() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void onInit(ArgumentList argumentlist)
    {
            super.onInit(argumentlist);
            String homeCabinetPath = HomeCabinetService.getHomeCabinetPath();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onInit:  homeCabinetPath : "+ homeCabinetPath, null, null);
            ApplicationLocation applicationlocation = AppSessionContext.get(getPageContext().getSession()).getAppLocation();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar: applicationlocation : "+ applicationlocation, null, null);
            if(applicationlocation != null){
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onInit: applicationlocation.getAppLocation : "+ applicationlocation.getAppLocation(), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onInit: applicationlocation.getComponentPath : "+ applicationlocation.getComponentPath(), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onInit: applicationlocation.getComponentId : "+ applicationlocation.getComponentId(), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onInit: applicationlocation.getFolderPath : "+ applicationlocation.getFolderPath(), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onInit: applicationlocation.getDocbase : "+ applicationlocation.getDocbase(), null, null);
            }

    }

    public void onRender()
    {
        try{
        super.onRender();
        //IDfSession idfsession = getDfSession();
        Menu vdmMenu = (Menu)getControl("doc_vdm", com.documentum.web.form.control.Menu.class);
        Panel vdmPanel = (Panel)getControl("doc_vdm_panel", com.documentum.web.form.control.Panel.class);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onRender: vdmMenu "+vdmMenu, null, null);
        String showvdmMenu = getString("ShowVDMMenu");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onRender: showvdmMenu "+showvdmMenu, null, null);
        boolean display = Boolean.valueOf(showvdmMenu).booleanValue();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs: MrcsMenuBar:onRender: display "+display, null, null);
        if(display){
            vdmMenu.setVisible(true);
            vdmPanel.setVisible(true);
        }else{
            vdmMenu.setVisible(false);
            vdmPanel.setVisible(false);
        }
        //IDfPersistentObject idfpersistentobject = idfsession.getObject(new DfId(s2));
        //String s4 = idfpersistentobject.getType().getName();
        }catch(Exception excep){
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "Mrcs: MrcsMenuBar:onRender:  Exception Occurred "+excep, null, null);
        }

    }

}
