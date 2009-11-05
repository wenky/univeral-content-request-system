/*
 * Created on Jan 21, 2005
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

 Filename       $RCSfile: GenericConfigFactory.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2008/10/07 15:49:54 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.config;


import java.util.HashMap;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class GenericConfigFactory {

    MrcsConfigBroker configbroker;
    
    public GenericConfigFactory()
    {
        configbroker = MrcsConfigBroker.getConfigBroker();
    }
    
    protected HashMap getApplications()
    {
        return  configbroker.getApplications();        
    }
    
    public MrcsApplication getApplication(String application)
    {
        return (MrcsApplication)this.getApplications().get(application);
    }
 
    public String getSystemUsername(String application)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSystemUsername - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSystemUsername - returning system username "+mrcsapp.SystemUsername,null,null);
        return mrcsapp.SystemUsername;        
    }
    
    public String getSystemPassword(String application)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSystemPassword - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSystemPassword - returning system password",null,null);
        return mrcsapp.SystemPassword;        
    }

    public String getApplicationDocbase(String application)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getApplicationDocbase - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getApplicationDocbase - returning docbase "+mrcsapp.DocBase,null,null);
        return mrcsapp.DocBase;        
    }
    
    public boolean isLegacyLCWF(String mrcsapp)
    {
    	/*-CONFIG-*/String m="isLegacyLCWF-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app name: "+mrcsapp, null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app found: "+(app != null), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"new LC defs? "+(app.MrcsLifecycles != null), null,null);
        return app.MrcsLifecycles == null;    	
    }
    
    public IDfSessionManager cfgSession() throws Exception
    {
    	return configbroker.getConfigSession();
    }
    public String cfgDocbase()
    {
    	return configbroker.getConfigDocbase();
    }


}
