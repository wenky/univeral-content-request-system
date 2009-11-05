package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.workflowvalidations;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.LocaleService;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.ErrKey;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.common.MdtErrorService;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtValidateFormRequiredAttributes implements IMdtWorkflowValidation {

    public boolean validate(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, List errors, Map context) 
    {        
        boolean success = true;
        try { 
            Map cfg = (Map)context;
            /*-dbg-*/Lg.dbg("get attr list from plugin config");                    
            List attrlist = (List)cfg.get("RequiredAttributes");
            /*-dbg-*/Lg.dbg("RequiredAttributes list: %b",attrlist != null);                    
            for (int i=0; i < attrlist.size(); i++) {
                /*-dbg-*/Lg.dbg("get next attr");                    
                String attrname = (String)attrlist.get(i);
                /*-dbg-*/Lg.dbg(" --name: %s",attrname);                    
                if (formobj.isAttrRepeating(attrname)) {
                    /*-dbg-*/Lg.dbg("check that repeating attr has at least one value");                    
                    if (formobj.getValueCount(attrname) < 1) {
                        /*-dbg-*/Lg.dbg("empty required repeating attribute");                    
                        success = false;
                        if (context!=null && ((Map)context).containsKey("AttributeError")) {
                            errors.add(MdtErrorService.renderErrorMessage("AttributeError",(Map)context,mgr,docbase,formobj,null,new ErrKey("attrname",attrname)));
                        } else {
                            errors.add("Attachment "+formobj.getObjectName()+" repeating attribute "+getAttributeLabel(formobj,attrname)+" has no values, and is required");
                        }
                    }                
                } else {
                    /*-dbg-*/Lg.dbg("check that single value is not empty");                    
                    String attrvalue = formobj.getValue(attrname).asString();
                    if (attrvalue == null || "".equals(attrvalue.trim())) {
                        /*-dbg-*/Lg.dbg("empty required single value attr");                    
                        success = false;
                        if (context!=null && ((Map)context).containsKey("AttributeError")) {
                            errors.add(MdtErrorService.renderErrorMessage("AttributeError",(Map)context,mgr,docbase,formobj,null,new ErrKey("attrname",attrname)));
                        } else {
                            errors.add("Attachment "+formobj.getObjectName()+" attribute "+getAttributeLabel(formobj,attrname)+" is required");
                        }
                    }
                }
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error checking form required attributes %s",formobj,dfe);
            throw EEx.create("WFValidateAttrs-DFE","Error checking form required attributes %s",formobj,dfe);
        }
        
        /*-dbg-*/Lg.dbg("return true");                    
        
        return success;
        
    }
    
    public static String getAttributeLabel(IDfSysObject doc, String attrname) throws DfException
    {
        String labelquery = "SELECT DISTINCT label_text,attr_name,LOWER(label_text) FROM dmi_dd_attr_info WHERE type_name = '"+doc.getTypeName()+"' AND nls_key = '"+LocaleService.getLocale().getLanguage()+"' AND attr_name = '"+attrname+"' ORDER BY 3";
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL(labelquery);
        IDfCollection idfcollection = null;
        try { 
            idfcollection = dfquery.execute(doc.getSession(), 0);
            if (idfcollection.next())             
            {
                String label = idfcollection.getString("label_text");
                return label;           
            }
        } finally {
            try { idfcollection.close(); } catch (Exception e) {}
        }
        
        return attrname;
    }



}
