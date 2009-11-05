/*
 * Created on Jan 27, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.client;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.medtronic.documentum.mrcs.plugin.PdfRenditionAlreadyPresent;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PdfRenditionAlreadyPresentPrecondition extends PdfRenditionAlreadyPresent implements IActionPrecondition
{

    public String[] getRequiredParams() {
        return (new String[] { "objectId" });
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
        /*-CONFIG-*/String m="queryExecute";
        try { 
            // get from config XML for this action, ie: <precondition class="blah"><role>ARole</role></precondition>
            //String configrole = iconfigelement.getChildValue("role");  
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - checking that doc doesn't have a pdf rendition already",null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - getting session from Webtop's Http Binding",null,null);
            String docbase = SessionManagerHttpBinding.getCurrentDocbase();
            IDfSession session = SessionManagerHttpBinding.getSessionManager().getSession(docbase);
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - retriving doc's objid from args",null,null);
            String objectid = argumentlist.get("objectId");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - objid: "+objectid,null,null);
                    
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - looking up the doc",null,null);
            IDfSysObject doc = (IDfSysObject)session.getObject(new DfId(objectid));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - doc retrieved",null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - checking for renditions",null,null);
            boolean flag = hasRendition("pdf",objectid,session);
            // return inverse of flag: we want to return true if no renditions found, false if they were found
            return !flag;
    
        } catch (DfException dfe) {
            /*-ERROR-*/DfLogger.error(this,m+" - unexpected exception while checking that doc has no renditions",null,dfe);
            throw new RuntimeException(dfe);
        }

    }

}
