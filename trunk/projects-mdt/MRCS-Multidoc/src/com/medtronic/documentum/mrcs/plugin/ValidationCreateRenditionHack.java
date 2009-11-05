/*
 * Created on Jul 16, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ValidationCreateRenditionHack extends MrcsBasePlugin implements IMrcsWorkflowValidation
{
    
    public boolean validate(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, List errors, Map configdata, Map customdata) throws Exception
    {
        /*-CONFIG-*/String m = "validate - ";
        IDfSession session = sMgr.getSession(docbase);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting ST config" , null, null);
        StateTransitionConfigFactory stconfig = StateTransitionConfigFactory.getSTConfig();
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting current state name" , null, null);
        String curstate = doc.getCurrentStateName();
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting state info for state "+curstate, null, null);
        StateInfo newStInfo = stconfig.getStateInfo(mrcsapp,curstate);
        
        // see if we're using the default (original) rendition processor. If a custom one is defined, use that one
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking if doc already has pdf renditions before we request them" , null, null);
        PdfRenditionAlreadyPresent prap = new PdfRenditionAlreadyPresent();
        boolean check = prap.hasRendition("pdf",doc.getObjectId().getId(),session);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"PRAP.hasRendition returned: "+check , null, null);
        if (check)
        {
            /*-WARN-*/DfLogger.warn(this,m+"bypassing rendition generation (default implementation) - rendition already present" , null, null);
        } else {
            if ("acro".equals(doc.getFormat().getName()))
            {
                // copy acro source as pdf format rendition
                // we can't do the pdf rendition manual attachment due to transaction deadlock - so we have to also use this class as a PostTransactionPromotePlugin: see execute() below
                // CEM - patch: need to do as system user
                StartWorkflowConfigFactory config = StartWorkflowConfigFactory.getWorkflowConfig();
                String syspass = config.getSystemPassword(mrcsapp);
                String sysuser = config.getSystemUsername(mrcsapp);
                IDfSessionManager syssessmgr = createSessionManager(docbase,sysuser,syspass);
                IDfSession syssession = syssessmgr.getSession(docbase);
                
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"re-retrieving document with system rights", null, null);
                IDfDocument sysdoc = (IDfDocument)syssession.getObject(doc.getObjectId());

                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" manually copy the source as the rendition - step 1 - getFile" , null, null);
                String filename = sysdoc.getFile(null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" manually copy the source as the rendition - add file as rendition" , null, null);
                sysdoc.addRendition(filename,"pdf");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" manually copy the source as the rendition - done" , null, null);
                sysdoc.save();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done with manual attachment", null, null);
                
                // release session
                syssessmgr.release(syssession);
            } else {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"generating rendition (default implementation)" , null, null);
                doc.queue("dm_autorender_win31", "rendition", 0, false, null, "rendition_req_ps_pdf");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Pdf Rendition of Document created!! " , null, null);
            }
            
        }
        sMgr.release(session);
        return true;
    }


}
