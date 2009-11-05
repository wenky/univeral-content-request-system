/*
 * Created on Jan 27, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.client;

import java.util.Map;

import com.documentum.fc.common.DfLogger;
import com.documentum.nls.NlsResourceBundle;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.formext.action.ActionExecutionUtil;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.CreatePDFRenditionAction;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsRequestPdfRendition extends CreatePDFRenditionAction
{
    public boolean execute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map)
    {
        // check that pdf rendition isn't already there...
        /*-CONFIG-*/String m = "execute - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - call PdfRenditionAlreadyPresentPrecondition to check doc doesn't have a pdf rendition already",null,null);
        PdfRenditionAlreadyPresentPrecondition prap = new PdfRenditionAlreadyPresentPrecondition();
        boolean check = prap.queryExecute(s,iconfigelement,argumentlist,context,component);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - PdfRenditionAlreadyPresentPrecondition returned: "+check,null,null);
        if (check)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - Pdf Rendition not there, so calling super() to generate PDF",null,null);
            return super.execute(s,iconfigelement,argumentlist,context,component,map);
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - Pdf Rendition was there, so we need to complain - get nls bundle",null,null);
            String nls = iconfigelement.getChildValue("nlsbundle");
            if(nls == null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - no nls bundle found, time to TRULY complain",null,null);
                throw new RuntimeException("Nlsbundle is not found in the configuration");                
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - nls found, compose a complaint for the ErrorMessageService",null,null);
            NlsResourceBundle nlsresourcebundle = new NlsResourceBundle(nls);
            Exception e = new Exception("Document Already Has PDF Rendition");
            ActionExecutionUtil.setCompletionError(map, nlsresourcebundle, "MSG_CREATE_PDF_ERROR", component, null, e);
            ErrorMessageService.getService().setNonFatalError(nlsresourcebundle, "MSG_CREATE_PDF_ERROR", component, null, e);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - returning false",null,null);
            return false;
        }

        
    }
    
}
