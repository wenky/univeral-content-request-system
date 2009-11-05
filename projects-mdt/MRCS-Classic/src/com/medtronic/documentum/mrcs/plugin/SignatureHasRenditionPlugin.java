package com.medtronic.documentum.mrcs.plugin;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.services.workflow.inbox.IWorkflowTask;

public class SignatureHasRenditionPlugin implements ISignatureValidation 
{
    public boolean validate(IDfSessionManager sMgr, String docbase, String mrcsapp, String wfname, IWorkflowTask task, IDfSysObject packagedoc, String username, String password, String reason, List errors, Map configdata, Map customdata) throws Exception
    {
        /*-CONFIG-*/String m = "SignatureHasRenditionPlugin.validate-";
    	
    	// check all packages and attachments for renditions of specified format
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get configed format", null, null);
    	String format = (String)configdata.get("Format");    	
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" -- format: "+format, null, null);
    	
    	// get the session
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get session", null, null);
    	IDfSession session = null;
    	session = sMgr.getSession(docbase);

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"lookup workitem", null, null);
		IDfWorkitem workitem = (IDfWorkitem)session.getObject(task.getId("item_id"));

	    IDfCollection packages = null;
	    try {
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting package collection for workitem", null, null);
	    	packages = workitem.getPackages(""); // returns visible and invisible. What's an invisible package?
			while(packages.next()) 
			{
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--NEXT PACKAGE--", null, null);
			    for (int i = 0; i < packages.getAttrCount(); i++) 
			    {
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attr:    "+packages.getAttr(i).getName(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"type:    "+packages.getAttr(i).getDataType(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"repeats: "+packages.getAttr(i).isRepeating(), null, null);		            
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"val:     "+packages.getValueAt(i).asString(), null, null);
			    }
			    String compid = packages.getString("r_component_id");
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--package id: "+compid, null, null);
	    		if (compid != null) {
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting package from docbase", null, null);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--checking for rendition format", null, null);
				    boolean flag = isRenditionExist(session, compid, format);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- validation result: "+flag, null, null);
				    if (!flag) {
				    	errors.add("MSG_RENDITION_NOT_FOUND");
				    	return false;
				    }
	    		}
	    	}
	    } catch (Exception e) {
    	    sMgr.release(session);
    	    /*-ERROR-*/DfLogger.error(this, m+"error in checking workitem packages...",null,e);
    	    throw e;
	    } finally {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"finally, close the package collection", null, null);
	    	if (packages != null)
	    		packages.close();
	    }
	    
	    // okey-dokey: now check for additional attachments to the workflow...
	    IDfCollection attachments = null;
	    try {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting ATTACHMENT collection for workitem", null, null);
	    	attachments =  workitem.getAttachments();
			while(attachments.next()) 
			{
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--NEXT ATTACHMENT--", null, null);
			    for (int i = 0; i < attachments.getAttrCount(); i++) 
			    {
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attr:    "+attachments.getAttr(i).getName(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"type:    "+attachments.getAttr(i).getDataType(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"repeats: "+attachments.getAttr(i).isRepeating(), null, null);		            
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"val:     "+attachments.getValueAt(i).asString(), null, null);
			    }
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
				    boolean flag = isRenditionExist(session, pkgdoc.getObjectId().getId(), format);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- validation result: "+flag, null, null);
				    if (!flag) {
				    	errors.add("MSG_RENDITION_NOT_FOUND");
				    	return false;
				    }
	    		}
	    	}
	    } catch (Exception e) {
    	    /*-ERROR-*/DfLogger.error(this, m+"error in promoting workitem attachments...",null,e);
    	    throw e;
	    } finally {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"finally, close the attachment collection", null, null);
	    	if (attachments != null)
	    		attachments.close();
	    	sMgr.release(session);
	    }
	    return true;
    	
    }
    
    private boolean isRenditionExist(IDfSession session, String objectId, String format) throws DfException
    {
        /*-CONFIG-*/String m = "isRenditionExist - ";
        boolean flag = false;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        if(objectId == null || format == null)
            throw new IllegalArgumentException("Object ID and format must be valid!");
        IDfCollection idfcollection = null;
        try { 
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing rendition search", null, null);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- DQL ["+"select r_object_id from dmr_content where any parent_id='" + objectId + "' and full_format='" + format + "'"+"]", null, null);        
	        DfQuery dfquery = new DfQuery();
	        dfquery.setDQL("select r_object_id from dmr_content where any parent_id='" + objectId + "' and full_format='" + format + "'");
	        idfcollection = dfquery.execute(session, 0);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if search returned items, thus a rendition found", null, null);
	        flag = idfcollection.next();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"found? "+flag, null, null);
        } finally {
        	if (idfcollection != null)
        		idfcollection.close();        	
        }
        return flag;
    }
    

}
