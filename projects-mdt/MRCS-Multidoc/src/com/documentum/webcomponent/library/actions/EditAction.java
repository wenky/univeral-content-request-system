/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   EditAction.java

package com.documentum.webcomponent.library.actions;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.env.*;
import com.documentum.web.formext.action.ActionService;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.preferredrenditions.AuthoringApplication;
import com.documentum.web.formext.preferredrenditions.PreferredRenditionsService;

public class EditAction
    implements IActionPrecondition
{

    public EditAction()
    {
    }

    public String[] getRequiredParams()
    {
        return (new String[] {
            "objectId"
        });
    }

    public boolean queryExecute(String strAction, IConfigElement config, ArgumentList arg, Context context, Component component)
    {
    	try { 
	        boolean bExecute;
	        IDfSession dfSession;
	        bExecute = false;
	        AbstractEnvironment env = EnvironmentService.getEnvironment();
	        IContentTransfer ctContract = env.getContentTransferContract();
	        String strCT = null;
	        if(ctContract != null)
	        {
	            strCT = ctContract.getContentTransferMechanism();
	            if(strCT.equals("http"))
	                return false;
	        }
	        dfSession = component.getDfSession();
	        String strObjectId;
	        IDfSysObject fetchedObj;
	        String strContentType;
	        strObjectId = arg.get("objectId");
	        if(FolderUtil.isFolderType(strObjectId))
	            //break MISSING_BLOCK_LABEL_389;
	        	return bExecute;
	        fetchedObj = null;
	        String strLockOwner = arg.get("lockOwner");
	        if(strLockOwner == null)
	        {
	            if(fetchedObj == null)
	                fetchedObj = (IDfSysObject)dfSession.getObject(new DfId(strObjectId));
	            strLockOwner = fetchedObj.getLockOwner();
	        }
	        if(strLockOwner != null && strLockOwner.length() != 0 && !strLockOwner.equals(dfSession.getLoginUserName()))
	            //break MISSING_BLOCK_LABEL_389;
	        	return bExecute;
	        strContentType = arg.get("contentType");
	        if(strContentType == null)
	        {
	            if(fetchedObj == null)
	                fetchedObj = (IDfSysObject)dfSession.getObject(new DfId(strObjectId));
	            strContentType = fetchedObj.getContentType();
	            arg.add("contentType", strContentType);
	        }
	        String strType = arg.get("type");
	        if(strType == null)
	            strType = "all_types";
	        PreferredRenditionsService preferredRenditionsService = PreferredRenditionsService.getInstance();
	        com.documentum.web.formext.preferredrenditions.PreferredRenditionsService.EditRenditionSetting editRenditionSetting = preferredRenditionsService.getEditRenditionSetting(strType, strContentType);
	        AuthoringApplication renditionAuthoringApplication = editRenditionSetting.getAuthoringApplication();
	        if(renditionAuthoringApplication.getType() != AuthoringApplication.SYSTEM_DEFINED_ACTION)
	        {	
	            //break MISSING_BLOCK_LABEL_276;
	            try
	            {
	                double contentSize = 0.0D;
	                String strContentSize = arg.get("contentSize");
	                if(strContentSize != null && strContentSize.length() != 0)
	                {
	                    contentSize = Double.parseDouble(strContentSize);
	                } else
	                {
	                    if(fetchedObj == null)
	                        fetchedObj = (IDfSysObject)dfSession.getObject(new DfId(strObjectId));
	                    contentSize = fetchedObj.getContentSize();
	                }
	                if(contentSize > 0.40000000000000002D)
	                    bExecute = true;
	                else
	                if(strContentType.length() > 0)
	                    bExecute = true;
	            }
	            catch(DfException e)
	            {
	                throw new WrapperRuntimeException("Failed to get object state", e);
	            }
	            return bExecute;
	        }
	        String action = renditionAuthoringApplication.getName();
	        bExecute = ActionService.queryExecute(action, arg, context, component);
	        return bExecute;
    	} catch (DfException dfe) {
    		throw new WrapperRuntimeException("Error in EditAction rewrite",dfe);
    	}
	}
}
