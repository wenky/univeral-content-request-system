package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowAttachmentValidation;

public class MdtValidateAttachmentRequiredAttributes implements IMdtWorkflowAttachmentValidation 
{

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, IDfSysObject attachment, List errors, Map context) 
    {
        boolean success = true;
        try { 
            Map cfg = (Map)context;
            /*-dbg-*/Lg.dbg("get attr list from plugin config");                    
            String attrlistcsv = (String)cfg.get("RequiredAttributes");
            /*-dbg-*/Lg.dbg("RequiredAttributes list: %s",attrlistcsv);                    
            String[] attrlist = attrlistcsv.split("\\,");
            /*-dbg-*/Lg.dbg("RequiredAttributes list: %b",attrlist != null);                    
            for (int i=0; i < attrlist.length; i++) {
                /*-dbg-*/Lg.dbg("get next attr");                    
                String attrname = (String)attrlist[i];
                /*-dbg-*/Lg.dbg(" --name: %s",attrname);                    
                if (attachment.isAttrRepeating(attrname)) {
                    /*-dbg-*/Lg.dbg("check that repeating attr has at least one value");                    
                    if (attachment.getValueCount(attrname) < 1) {
                        /*-dbg-*/Lg.dbg("empty required repeating attribute");                    
                        success = false;
                        if (context!=null && ((Map)context).containsKey("AttributeError")) {
                            errors.add(MdtErrorService.renderErrorMessage("AttributeError",(Map)context,mgr,docbase,formobj,attachment,new ErrKey("attrname",attrname)));
                        } else {
                            errors.add("Attachment "+attachment.getObjectName()+" repeating attribute "+MdtErrorService.getAttributeLabel(attachment,attrname)+" has no values, and is required");
                        }
                    }                
                } else {
                    /*-dbg-*/Lg.dbg("check that single value is not empty");                    
                    String attrvalue = attachment.getValue(attrname).asString();
                    if (attrvalue == null || "".equals(attrvalue.trim())) {
                        /*-dbg-*/Lg.dbg("empty required single value attr");                    
                        success = false;
                        if (context!=null && ((Map)context).containsKey("AttributeError")) {
                            errors.add(MdtErrorService.renderErrorMessage("AttributeError",(Map)context,mgr,docbase,formobj,attachment,new ErrKey("attrname",attrname)));
                        } else {
                            errors.add("Attachment "+attachment.getObjectName()+" attribute "+MdtErrorService.getAttributeLabel(attachment,attrname)+" is required");
                        }
                    }
                }
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking attachment required attributes %s",attachment,dfe);
            throw EEx.create("WFValidateAttrs-DFE","Error checking attachment required attributes %s",attachment,dfe);
        }
        
        /*-dbg-*/Lg.dbg("return");                    
        
        return success;
        
    }
    

}
