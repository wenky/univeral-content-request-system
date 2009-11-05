package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

public class AttachBlankPdfRendition implements IMrcsRenditionPlugin, IMrcsLifecyclePlugin
{
	
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
        /*-CFG-*/String m="execute(new)-";
		try { 
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"preparing to perform rendering", null, null);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
	        IDfSession session = sMgr.getSession(docbase);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"rendering as user "+sMgr.getIdentity(docbase).getUser(), null, null);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"calling rendering", null, null);
	        render(session,mrcsdocument,config);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"render returned - performing document save", null, null);
	        mrcsdocument.save();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"render returned", null, null);
	        sMgr.release(session);
		} catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Exception occurred in AttachBlankPdfRendition attempt", null, e);
            RuntimeException re = new RuntimeException("Exception occurred in AttachBlankPdfRendition attempt "+e,e);
            throw re;			
		}
	}

	
    public void render(IDfSession session, IDfSysObject document, Map config) throws Exception
    {
        /*-CONFIG-*/String m="render-";
        // check if this is the special "acro" format
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" checking if doc already has pdf renditions before we request them" , null, null);
        PdfRenditionAlreadyPresent prap = new PdfRenditionAlreadyPresent();
        if (prap.hasRendition("pdf",document.getObjectId().getId(),session))
        {
            /*-WARN-*/DfLogger.warn(this,m+" bypassing rendition generation (default implementation) - rendition already present" , null, null);
        } else {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" no signing page found, getting base document from config" , null, null);
            String blankpdfpath = (String)config.get("BlankPdfDocument");
            // copy acro source as pdf format rendition
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" looking up blank document" , null, null);
            IDfDocument blankpdf = (IDfDocument)session.getObjectByPath(blankpdfpath);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" manually copy the source as the rendition - step 1 - getFile" , null, null);
            String filename = blankpdf.getFile(null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" add file as rendition: "+filename , null, null);
            document.addRendition(filename,"pdf");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" pdf rendition manually attached to document "+document.getObjectId().getId() , null, null);
        }
    }
}
