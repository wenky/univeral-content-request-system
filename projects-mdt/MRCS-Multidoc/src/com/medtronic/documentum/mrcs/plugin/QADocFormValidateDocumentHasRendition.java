package com.medtronic.documentum.mrcs.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

public class QADocFormValidateDocumentHasRendition implements IMrcsWorkflowValidation {

    public boolean validate(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, List errmsgs, Map configdata, Map customdata) throws Exception
    {
        /*-CONFIG-*/String m = "QADocFormValidateDocumentHasRendition.checkRenditions - ";
    	// doc is form package -> get the form's workflow, then get that workflow's attachments
    	IDfSession session = doc.getSession();
        DfQuery dfquery = new DfQuery();
        String dql = "select r_workflow_id from dmi_package where any r_component_id ='" + doc.getObjectId().getId() + "'";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- DQL ["+dql+"]", null, null);
        boolean errtracker = true;
        Map error = new HashMap();
        dfquery.setDQL(dql);      
        IDfCollection idfcollection = dfquery.execute(session, 0);
        try {
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if search returned items, thus a rendition found", null, null);
	        if (idfcollection.next())
	        {
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting workflowid", null, null);
		        IDfId wfid = idfcollection.getId("r_workflow_id");
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting workflow object", null, null);
		        IDfWorkflow wfobj = (IDfWorkflow)session.getObject(wfid);
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting ATTACHMENT collection for workflow: "+wfobj.getObjectName(), null, null);
		    	IDfCollection attachments =  wfobj.getAttachments();
		    	try { 
					while(attachments.next()) 
					{
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--NEXT ATTACHMENT--", null, null);
					    String compid = attachments.getString("r_component_id");
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--ATTACHMENT id: "+compid, null, null);
			    		if (compid != null) {
						    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting ATTACHMENT from docbase", null, null);
			    			IDfDocument attacheddoc = (IDfDocument)session.getObject(new DfId(compid));
						    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--pkgdoc retrieved: "+attacheddoc.getObjectName() + " - "+attacheddoc.getObjectId().getId(), null, null);
						    
						    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting most recent version of attachment", null, null);
			    			IDfDocument pkgdoc = (IDfDocument)session.getObjectByQualification("dm_document where i_chronicle_id = '"+attacheddoc.getChronicleId().getId()+"'");
						    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--most recent retrieved: "+pkgdoc.getObjectName() + " - "+pkgdoc.getObjectId().getId(), null, null);
						    
						    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- checking attachment for rendition", null, null);
					    	boolean flag = !checkRenditions(sMgr,docbase,pkgdoc,mrcsapp,wfname,configdata,customdata);
					        if (!flag)
					        {
					        	errtracker = false;
					            error.put("Error","ERR_RENDITION_VALIDATION_NO_RENDITIONS_FOUND");
					            errmsgs.add(error);            
					        }
			    		}
			    	}
		    	} finally { attachments.close(); }
	        	
	        } else {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"workflow/package not found, throwing exception", null, null);
	            throw new RuntimeException("QADocFormValidateDocumentHasRendition - workflow not found for form "+doc.getObjectId().getId());
	        }
        } finally {
        	idfcollection.close();
        }

        return errtracker;
    }

    // we need to override this method since 
    public boolean checkRenditions(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, Map configdata, Map customdata) throws Exception
    {
        // stolen from ESignPrecondition
        /*-CONFIG-*/String m = "checkRenditions - ";
        boolean flag = false;
        String format = (String)configdata.get("RenditionFormat");
        String objectId = doc.getObjectId().getId();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        if(objectId == null || format == null)
            throw new IllegalArgumentException("Object ID and format must be valid!");
        IDfCollection idfcollection = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing rendition search", null, null);        
        IDfSession session = sMgr.getSession(docbase);
        DfQuery dfquery = new DfQuery();
        String dql = "select r_object_id from dmr_content where any parent_id='" + objectId + "' and full_format='" + format + "'";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- DQL ["+dql+"]", null, null);        
        dfquery.setDQL(dql);
        idfcollection = dfquery.execute(session, 0);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if search returned items, thus a rendition found", null, null);
        flag = idfcollection.next();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"found? "+flag, null, null);
        idfcollection.close();
        // return inverse of flag: we want to return true if no renditions found, false if they were found
        return !flag;
    }

}