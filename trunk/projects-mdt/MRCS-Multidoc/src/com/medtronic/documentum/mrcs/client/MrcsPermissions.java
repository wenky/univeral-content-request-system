/*
 * Created on May 18, 2005
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

 Filename       $RCSfile: MrcsPermissions.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:46 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.ColumnPanel;
import com.documentum.web.form.control.Panel;
import com.documentum.webcomponent.library.permissions.Permissions;
import com.medtronic.documentum.mrcs.config.PermitUIConfigFactory;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsPermissions extends Permissions {

    boolean showPermitUI = false;
    boolean override = true;
    /**
     * 
     */
    public MrcsPermissions() {
        super();
    }
    
    protected boolean canUserChangePermissions()
    {
        /*-CFG-*/String m="canUserChangePermissions-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"calling superclass's canUserChangePermissions", null, null);
        boolean canChange = super.canUserChangePermissions();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"superclass (webtop default impl) says:"+canChange, null, null);
        if (!override) 
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"component is not to override what default webtop says, returning "+canChange, null, null);            
            return canChange;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checking for overrides from PermitUI tag in mrcs config", null, null);
        if(canChange){
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting canChange permissions to PermitUI value: "+showPermitUI, null, null);
            canChange = showPermitUI;  
        }            
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"are permissions editable? returning " + canChange, null, null);
       return canChange;        
    }
    
    
    public void onInit(ArgumentList argumentlist)
    {
        /*-CFG-*/String m="onInit-";
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top..." , null, null);
            String val[] = argumentlist.getValues("objectId");
            IDfSysObject mrcsSysObject = (IDfSysObject) getDfSession().getObject(new DfId(val[0]));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting PermitUI MRCS config", null, null);
            PermitUIConfigFactory config = PermitUIConfigFactory.getPermitUIConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"config > " + config, null, null);
            if (!mrcsSysObject.hasAttr("mrcs_application") || 
                mrcsSysObject.getString("mrcs_application") == null ||
                "".equals(mrcsSysObject.getString("mrcs_application")) )
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"base document not an MRCS document, or mrcs properties are not populated correctly", null, null);
                showPermitUI = false;
                super.onInit(argumentlist);
            }
            else
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"base document is an MRCS document, check if PermitUI in MRCS app disallows permissions modifications", null, null);
                override = true;
                String appName = mrcsSysObject.getString("mrcs_application");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting PermitUI configuration for MRCS app : " + appName, null, null);
                showPermitUI = config.getPermitUIInfo(appName);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"showPermitUI is : " + showPermitUI, null, null);
                super.onInit(argumentlist);
            }
        } catch (DfException dfe) {
            /*-ERROR-*/DfLogger.error(this, m+" DFException Occurred : ", null, dfe);
            throw new RuntimeException("MrcsPermissions encountered an Documentum error in onInit",dfe);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Other Unhandled Exception [Could not load PermitUI configurations] ", null, e);
            throw new RuntimeException("MrcsPermissions encountered an error in onInit",e);
        }   
        
        
    }

}
