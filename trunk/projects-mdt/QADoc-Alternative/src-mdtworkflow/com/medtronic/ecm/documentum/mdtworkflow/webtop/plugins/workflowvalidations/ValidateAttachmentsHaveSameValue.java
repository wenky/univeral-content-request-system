package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfValue;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class ValidateAttachmentsHaveSameValue implements IMdtWorkflowValidation 
{

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
                
                // get configured attachment plugin list
                List attrs = (List)config.get("Attributes");
                for (int i=0; i < attrs.size(); i++) {
                    String attr = (String)attrs.get(i);      
                    IDfValue v = null;
                    for (int a=0; a < attachments.size(); a++) {
                        IDfSysObject attachment = (IDfSysObject)attachments.get(a);
                        if (v == null) {
                            v = attachment.getValue(attr);
                        } else {
                            IDfValue newv = attachment.getValue(attr);
                            if (!newv.equals(v)) {
                                success = false;
                                errors.add("Attribute "+attr+" is not the same for all attachments");            
                            }
                        }                        
                    }
                }
                return success;
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