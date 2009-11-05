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

 Filename       $RCSfile: MrcsEditFileAction.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.4 $
 Modified on    $Date: 2006/11/08 21:38:18 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.EditAction;
import com.medtronic.documentum.mrcs.common.MrcsPreConditions;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;

/**
 * @author prabhu1
 *
 */
public class MrcsEditFileAction extends EditAction {

	private String appName = "";

    public MrcsEditFileAction() {
        super();
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
        IDfDocument docObject = null;
        MrcsPlugin preCheckClass = null;
        MrcsPreConditions preCheck = null;

        //boolean flag = super.queryExecute(s, iconfigelement, argumentlist, context, component);
        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsCheckoutAction: queryExecute: super.queryExecute "+ flag, null, null);
        try { 
	        boolean flag = false;
	        IDfSession idfsession = component.getDfSession();
	
	        try
	        {
	            String val[] = argumentlist.getValues("objectId");
	            docObject = (IDfDocument) idfsession.getObject(new DfId(val[0]));
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsEditFileAction: queryExecute: docObject "+ docObject, null, null);
	            appName = docObject.getString("mrcs_application");
	            MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsEditFileAction: queryExecute: config > " + config, null, null);
	            try { 
            		preCheckClass = config.getPreconditionPlugin(appName, "NotInWorkflow"); 
        		} catch (NullPointerException npe) { 
    	            /*-ERROR-*/DfLogger.error(this, "MRCS: MrcsEditFileAction: queryExecute: NotInWorkflow precondition not defined for this config "+appName, null, null);
        			return false; 
    			}
	            preCheck = (MrcsPreConditions)Class.forName(preCheckClass.PluginClassName).newInstance();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsEditFileAction: queryExecute: preCheck "+ preCheck, null, null);
		        HashMap map = new HashMap();
	            map.put("mrcsapp",appName);
		        map.put("IDfDocument",docObject);
		        flag = preCheck.isTaskEffectual(map,preCheckClass.PluginConfiguration);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsEditFileAction: queryExecute: [preCheck.isTaskEffectual]: "+ flag, null, null);
	        }
	        catch(DfException dfexception)
	        {
	            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "MRCS: MrcsEditFileAction: queryExecute: Exception  Occurred "+ dfexception, null, null);
	            //throw new WrapperRuntimeException("Failed to get object state", dfexception);
	        }
	
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS: MrcsEditFileAction: queryExecute:  can checkout: "+ flag, null, null);
	        return flag;
        } catch (Exception e) {
	        /*-ERROR-*/DfLogger.error(this, "MRCS: MrcsEditFileAction: queryExecute:  exception ", null, e);
        	return false;
        }
    }

}
