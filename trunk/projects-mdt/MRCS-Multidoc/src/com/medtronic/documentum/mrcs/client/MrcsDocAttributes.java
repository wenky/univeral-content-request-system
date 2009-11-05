/*
 * Created on Feb 18, 2005
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

 Filename       $RCSfile: MrcsDocAttributes.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:44 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Label;
import com.documentum.webcomponent.library.attributes.Attributes;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsDocAttributes extends MrcsGeneralAttributes {

    public void onInit(ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocAttributes.onInit - custom attributes component initializing");
        super.onInit(argumentlist);
        try {        
            // set the document name on the page
            Label docname = (Label)getControl("labeldocumentname",Label.class);
            // get the document name from the objectid...
            String objectid = argumentlist.get("objectId");
            IDfSession session = getDfSession();
            IDfId newid = new DfId(objectid);
            IDfDocument newdoc = (IDfDocument)session.getObject(newid);
            String objname = newdoc.getObjectName();
            docname.setLabel(objname);
        } catch (Exception dfe) {
            setReturnError("MSG_DFC_ERROR", null, dfe);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", dfe);
            return;
        }

    }
    
    public void processAttributes(Control button, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocAttributes.processAttributes - custom attributes component submit clicked");
        boolean result = onCommitChanges();
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocAttributes.processAttributes - commit success? "+result);
        setComponentReturn();
    }
    
    public void cancelAttributes(Control button, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocAttributes.cancelAttributes - cancelled, returning");
        setComponentReturn();
    }
    
}
