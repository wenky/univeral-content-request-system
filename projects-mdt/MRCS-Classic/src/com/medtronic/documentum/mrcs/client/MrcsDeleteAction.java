/*
 * Created on Nov 21, 2005
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

 Filename       $RCSfile: MrcsDeleteAction.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/09/20 18:54:45 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.DeleteDocPrecondition;
import com.medtronic.documentum.mrcs.common.MrcsPreConditions;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsDeleteAction extends DeleteDocPrecondition
{

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
        /*-CONFIG-*/String m = "queryExecute - ";
    	try { 
	        IDfDocument docObject = null;
	        MrcsPlugin preCheckClass = null;
	        MrcsPreConditions preCheck = null;
	
	
	        // CEM: super() unncecessary if sysobject_actions still configured to call the parent as part of the precondition plugin list
	        //boolean flag = super.queryExecute(s, iconfigelement, argumentlist, context, component);
	        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"super.queryExecute "+ flag, null, null);
	        boolean flag = true;
	        // CEM: end of changes (comment out super(), simple default of flag...)
	        
	        IDfSession idfsession = component.getDfSession();
	        if(flag){
	            try
	            {
	                String val[] = argumentlist.getValues("objectId");
	                docObject = (IDfDocument) idfsession.getObject(new DfId(val[0]));
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"docObject "+ docObject, null, null);
	                String appName = docObject.getString("mrcs_application");
	                MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"config > " + config, null, null);
	                preCheckClass = config.getPreconditionPlugin(appName, "NotInWorkflow");
	                preCheck = (MrcsPreConditions)Class.forName(preCheckClass.PluginClassName).newInstance();
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"preCheck "+ preCheck, null, null);
	                HashMap map = new HashMap();
	                map.put("mrcsapp",appName);
	                map.put("IDfDocument",docObject);
	                flag = preCheck.isTaskEffectual(map,preCheckClass.PluginConfiguration);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"[preCheck.isTaskEffectual]: "+ flag, null, null);
	            }
	            catch(Exception dfexception)
	            {
	                /*-ERROR-*/DfLogger.error(this, m+"Exception  Occurred ", null, dfexception);
	                throw new RuntimeException("Error in delete action NotInWorkflow precondition check", dfexception);
	            }
	        }
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" can delete: "+ flag, null, null);
	        return flag;
	        
	    } catch (Exception e) {
	        /*-ERROR-*/DfLogger.error(this, m+"Exception  Occurred ", null, e);
	        return false;
	    }
    }

}
