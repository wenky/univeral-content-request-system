/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ViewAction.java

package com.documentum.webcomponent.library.actions;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.formext.action.ActionService;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.docbase.ObjectCacheUtil;
import com.documentum.web.formext.preferredrenditions.AuthoringApplication;
import com.documentum.web.formext.preferredrenditions.PreferredRenditionsService;
import com.documentum.webcomponent.xforms.XFormsUtil;

public class ViewAction
    implements IActionPrecondition
{

    public ViewAction()
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
        boolean bExecute;
        com.documentum.fc.client.IDfSession dfSession;
        bExecute = false;
        dfSession = component.getDfSession();
        String strObjectId;
        strObjectId = arg.get("objectId");
        if(FolderUtil.isFolderType(strObjectId))
        {
        	// can't view a folder...
        	//break MISSING_BLOCK_LABEL_277;
        	return false;
        }
        IDfSysObject sysobj = null;
        long contentSize = 0L;
        try { 
	        String strContentSize = arg.get("contentSize");
	        if(strContentSize != null && strContentSize.length() != 0)
	        {
	            contentSize = (long)Double.parseDouble(strContentSize);
	        } else
	        {
	            sysobj = (IDfSysObject)ObjectCacheUtil.getObject(dfSession, strObjectId);
	            if(sysobj != null)
	                contentSize = sysobj.getContentSize();
	        }
	        if((double)contentSize <= 0.40000000000000002D)
	        {
	        	// no content???
	            //break MISSING_BLOCK_LABEL_248;
	            bExecute = XFormsUtil.isFormFeatureApplicable(dfSession, strObjectId, arg, context);
	            return bExecute;
	        }
	        
	        String strContentType = arg.get("contentType");
	        if((strContentType == null || strContentType.length() == 0) && sysobj == null)
	        {
	            sysobj = (IDfSysObject)ObjectCacheUtil.getObject(dfSession, strObjectId);
	            strContentType = sysobj.getContentType();
	            arg.add("contentType", strContentType);
	        }
	        if(strContentType == null || strContentType.length() <= 0)
	        {
	            //break MISSING_BLOCK_LABEL_242;
	        	// no content type or size
	        	return true;
	        }
	        String strType = arg.get("type");
	        if(strType == null)
	            strType = "all_types";
	        PreferredRenditionsService preferredRenditionsService = PreferredRenditionsService.getInstance();
	        com.documentum.web.formext.preferredrenditions.PreferredRenditionsService.ViewRenditionSetting viewRenditionSetting = preferredRenditionsService.getViewRenditionSetting(strType, strContentType);
	        AuthoringApplication renditionAuthoringApplication = viewRenditionSetting.getAuthoringApplication();
	        if(renditionAuthoringApplication.getType() != AuthoringApplication.SYSTEM_DEFINED_ACTION)
	        {
	        	// wtf does this do?
	            //break MISSING_BLOCK_LABEL_242;
	            return true;
	        }
	        String action = renditionAuthoringApplication.getName();
	        bExecute = ActionService.queryExecute(action, arg, context, component);
        } catch (DfException dfe) {
            throw new WrapperRuntimeException("Failed to get content size", dfe);        	
        }
        return bExecute;
//		MISSING_BLOCK_LABEL_242:
//		       	bExecute = true;
//		        break MISSING_BLOCK_LABEL_277;
//		MISSING_BLOCK_LABEL_248:
//		        bExecute = XFormsUtil.isFormFeatureApplicable(dfSession, strObjectId, arg, context);
//		        break MISSING_BLOCK_LABEL_277;
//		MISSING_BLOCK_LABEL_277:
//		    	return bExecute;
    }
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/actions/ViewAction.class


	TOTAL TIME: 0 ms


	JAD REPORTED MESSAGES/ERRORS:

Couldn't fully decompile method queryExecute
Couldn't resolve all exception handlers in method queryExecute

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/