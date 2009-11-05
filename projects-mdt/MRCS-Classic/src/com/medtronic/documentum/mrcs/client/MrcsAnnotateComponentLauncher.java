/*
 * Created on Mar 9, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.client;

import java.util.Map;

import javax.servlet.http.HttpSession;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.LaunchComponent;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.messages.MessageService;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsAnnotateComponentLauncher extends LaunchComponent
{
    public boolean execute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map)
    {
        /*-CONFIG-*/String m="execute";
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Top of MrcsAnnotateComponentLauncher.execute - getting session from component", null, null);
            IDfSession dctmsession = component.getDfSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get objectid of doc we will be annotating", null, null);
            String objectid = argumentlist.get("objectId");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting object for objid "+objectid, null, null);
            IDfSysObject theobject = (IDfSysObject)dctmsession.getObject(new DfId(objectid));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up value of mrcs_application attribute", null, null);
            String mrcsapp = theobject.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting conventional servlet HTTP session", null, null);
            HttpSession httpsession = component.getPageContext().getSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting servlet session attribute annotate_mrcs_app to "+mrcsapp, null, null);
            httpsession.setAttribute("annotate_mrcs_app",mrcsapp);
            // must have RELATE access to annotate...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checking that user has at least RELATE level access rights", null, null);
            int objpermit = theobject.getPermit();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"current user's permit level on doc: "+objpermit, null, null);
            if (IDfACL.DF_PERMIT_RELATE > objpermit)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"user does not have at least RELATE access, returning false", null, null);
                MessageService.addMessage(component, "MUST_HAVE_RELATE_TO_ANNOTATE", null);
                return false;
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing annotation...", null, null);
            return super.execute(s,iconfigelement,argumentlist,context,component,map);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Error in MrcsAnnotateComponentLauncher (make sure document is actually a MRCS document)", null, e);
            return false;
        }
            
    }
}
