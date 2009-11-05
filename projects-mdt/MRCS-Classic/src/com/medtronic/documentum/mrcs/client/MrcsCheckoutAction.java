/*
 * Created on Mar 7, 2005
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

 Filename       $RCSfile: MrcsCheckoutAction.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2006/11/14 17:34:20 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.CheckoutAction;
import com.medtronic.documentum.mrcs.common.MrcsPreConditions;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;

/**
 * @author prabhu1
 *
 */
public class MrcsCheckoutAction extends CheckoutAction{

	private String appName = "";

    public MrcsCheckoutAction() {
        super();
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
    	try {
	        IDfDocument docObject = null;
	        MrcsPlugin preCheckClass = null;
	        MrcsPreConditions preCheck = null;
	
	        // CEM: super() unncecessary if sysobject_actions still configured to call the parent as part of the precondition plugin list
	        //boolean flag = super.queryExecute(s, iconfigelement, argumentlist, context, component);
	        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: queryExecute: super.queryExecute "+ flag, null, null);
	        boolean flag = true;
	        // CEM: end of changes (comment out super(), simple default of flag...)
	        
	        IDfSession idfsession = component.getDfSession();
	        if(flag){
	            try
		        {
		            String val[] = argumentlist.getValues("objectId");
		            docObject = (IDfDocument) idfsession.getObject(new DfId(val[0]));
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: queryExecute: docObject "+ docObject, null, null);
		            appName = docObject.getString("mrcs_application");
			        try {
			            //Need to eliminate the usage of this try catch block by
			            //better exception handling mechanizsm at Config broker
			            MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
			            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: queryExecute: config > " + config, null, null);
			            preCheckClass = config.getPreconditionPlugin(appName, "NotInWorkflow");
			            preCheck = (MrcsPreConditions)Class.forName(preCheckClass.PluginClassName).newInstance();
			            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: queryExecute: preCheck "+ preCheck, null, null);
			        } catch (NullPointerException e) {
			            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS: MrcsCheckoutAction: queryExecute: StateTransition precondition not defined for this config " + appName, null, null);
			            return false;
			        }
			        HashMap map = new HashMap();
	                map.put("mrcsapp",appName);
	                map.put("IDfDocument", docObject);
			        flag = preCheck.isTaskEffectual(map,preCheckClass.PluginConfiguration);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: queryExecute: [preCheck.isTaskEffectual]: "+ flag, null, null);
		        }
		        catch(DfException dfexception)
		        {
		            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS: MrcsCheckoutAction: queryExecute: Exception  Occurred "+ dfexception, null, null);
		            //throw new WrapperRuntimeException("Failed to get object state", dfexception);
		        }
	        }
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: queryExecute:  can checkout: "+ flag, null, null);
	        return flag;
    	} catch (Exception e) {
	        /*-ERROR-*/DfLogger.error(this, "MRCS: MrcsCheckoutAction: QUERYEXECUTE Exception: ", null, e);
    		return false;
    	}
    }

    public boolean execute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map)
    {
        boolean flag = super.execute(s, iconfigelement, argumentlist, context, component, map);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: execute:  checkout "+ flag, null, null);
        return flag;
    }
}
