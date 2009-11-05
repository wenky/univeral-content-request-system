package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;
import com.medtronic.ecm.documentum.util.DctmUtils;

public class MdtVersionPromoteRenderForm implements IMdtWorkflowAction
{
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map context)
    {
        IDfSysObject newdoc = null;
        try { 
            newdoc = DctmUtils.versionDocument(formobj, "MAJOR", false);
            /*-dbg-*/Lg.dbg("set in-wf flag on m_rollback_form attr");
            newdoc.setString("m_rollback_form", "in-wf");
            /*-dbg-*/Lg.dbg("saving");
            newdoc.save();
            /*-dbg-*/Lg.dbg("saved");
                        
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while versioning change request form %s",formobj,dfe);
            throw EEx.create("VersionChangeRequest","error while versioning change request form %s",formobj,dfe);            
        }
        
        // promote form
        try { 
            if (newdoc.canPromote())
            {
                /*-dbg-*/Lg.dbg("invoking promote from workflow on changre request form %s",formobj);
                newdoc.promote(null,true,false);
                /*-dbg-*/Lg.dbg("promote returned successfully");
            } else {
                /*-WARN-*/Lg.wrn("attachment document canPromote() returned false %s",formobj);
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("error while promoting change request form %s",newdoc,dfe);
            throw EEx.create("PromoteChangeRequest","error while promoting change request form %s",newdoc,dfe);            
        }
        
        // render form 
        try {
            if (context == null || !context.containsKey("SkipRender"))
            {
                String format = null;
                if (context != null && context instanceof Map) {
                    Map cfgmap = (Map)context;
                    format = (String)cfgmap.get("RenditionFormat");
                }
                if (format == null || "".equals(format))
                {
                    format = "pdf";
                }
        
                /*-dbg-*/Lg.dbg("check if doc already has rendition of format: %s",format);            
                boolean hasrendition = DctmUtils.hasRendition(newdoc.getSession(),newdoc.getObjectId().getId(),format);
               
                if (!hasrendition)
                {
                    /*-dbg-*/Lg.dbg("need to generate a rendition");            
                    DctmUtils.makeRenditionRequest(newdoc,format);
                }
            }
            /*-dbg-*/Lg.dbg("done");
        } catch (DfException e) {
            /*-ERROR-*/Lg.err("Error encountered requesting rendition for change request form %s",newdoc,e);
            throw EEx.create("RenderChangeRequest","Error encountered requesting rendition for change request form %s",newdoc,e);
        }
        
        
    }

}
