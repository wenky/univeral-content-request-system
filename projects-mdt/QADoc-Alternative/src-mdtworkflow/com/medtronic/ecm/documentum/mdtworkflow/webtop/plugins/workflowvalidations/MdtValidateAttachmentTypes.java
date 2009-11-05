package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowAttachmentValidation;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtValidateAttachmentTypes implements IMdtWorkflowValidation {

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {
        boolean success = true;
        try { 
            Map config = (Map)context;
            
            MdtConfigService cfgsvc = MdtConfigService.getConfigService(mgr,docbase);
            Map appcfg = (Map)cfgsvc.getAppConfig(mdtapp);
            
            Map crdefs = (Map)appcfg.get("ChangeRequests");
            Map wfdef = (Map)crdefs.get(formobj.getTypeName());
            
            String attachattr = (String)wfdef.get("AttachmentsAttribute");
            
            IDfSession session = null;
                   
            try {            
                session = mgr.getSession(docbase);
                
                List attachments = new ArrayList();
            
                // get attachments from docbase
                for (int i=0; i < formobj.getValueCount(attachattr); i++) 
                {
                    String attachmentname = formobj.getRepeatingString(attachattr, i);
                    IDfSysObject attachment = AttachmentUtils.lookupAttachmentByName(formobj, attachmentname);
                    attachments.add(attachment);
                }
                
                // check attachment types
                /*-dbg-*/Lg.dbg("get valid types set");
                Set typeset = (Set)context.get("ValidTypes");
                boolean uniform = context.containsKey("UniformTypes");
                boolean uniformerrortriggered = false;
                boolean badtypeerrortriggered = false;
                String firsttype = null;
                for (int a=0; a < attachments.size(); a++) {
                    IDfSysObject attachment = (IDfSysObject)attachments.get(a);
                    /*-dbg-*/Lg.dbg("check attachment against typelist %s",attachment);
                    try {
                        String curtype = attachment.getTypeName();
                        if (uniform) {
                            if (firsttype == null)
                                firsttype = curtype;
                            else 
                                if (!curtype.equals(firsttype)) {
                                    if (!uniformerrortriggered) {
                                        // uniform validation error
                                        if (context!=null && ((Map)context).containsKey("UniformTypesError")) {
                                            errors.add(MdtErrorService.renderErrorMessage("UniformTypesError",context,mgr,docbase,formobj,attachment,new ErrKey("validtypes",typeset)));
                                        } else {
                                            errors.add("All workflow attachments must be of the same type");
                                        }
                                        uniformerrortriggered = true;
                                    }
                                }
                        }
                        if (!typeset.contains(curtype)) {
                            // valid types validation error
                            if (context!=null && ((Map)context).containsKey("ValidTypesError")) {
                                errors.add(MdtErrorService.renderErrorMessage("ValidTypesError",context,mgr,docbase,formobj,attachment,new ErrKey("validtypes",typeset)));
                            } else {
                                errors.add("Attachment "+attachment.getObjectName()+" is not one of the valid types for this form");
                            }
                            badtypeerrortriggered = true;
                        }
                        
                    } catch (DfException dfe) {
                        /*-ERROR-*/Lg.err("Error checking attachment types on %s",attachment,dfe);
                        throw EEx.create("WFNoRendtion-DFE","Error checking attachment types on %s",attachment,dfe);
                    }
                    
                }
                if (uniformerrortriggered) 
                    return false;
                else if (badtypeerrortriggered)
                    return false;
                else 
                    return true;
            } finally {
                try {mgr.release(session);} catch (Exception e) {}
            }
        } catch (DfException dfe) {
            // create a validation error for the exception?
            /*-ERROR-*/Lg.err("DCTM Error executing attachment validations",dfe);
            throw EEx.create("WFValidateAttachments-DFE","Error executing attachment validations",dfe);
        } catch (Exception e) {
            // create a validation error for the exception?
            /*-ERROR-*/Lg.err("Error executing attachment validations",e);
            throw EEx.create("WFValidateAttachments-EX","Error executing attachment validations",e);
        }
        
    }    
    

}
