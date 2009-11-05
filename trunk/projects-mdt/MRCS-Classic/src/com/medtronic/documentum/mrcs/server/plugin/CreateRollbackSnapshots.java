package com.medtronic.documentum.mrcs.server.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class CreateRollbackSnapshots implements IMrcsWorkflowServerPlugin
{
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{
		// create snapshots for all packages and attachments
       	/*-CONFIG-*/String m="CreateRollbackSnapshots.execute - ";
    	// what is it passing us for arguments?
    	// answer: docbase, ticket, user, packageId, mode

       	IDfSession session = null;
       	
	    IDfCollection packages = null;
	    try {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting session", null, null);
	    	session = sMgr.getSession(docbase);
	    	
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting package collection for workitem", null, null);
	    	packages = task.getPackages(""); // returns visible and invisible. What's an invisible package?
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
	    			IDfDocument pkgdoc = (IDfDocument)session.getObject(new DfId(compid));
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--pkgdoc retrieved: "+pkgdoc.getObjectName(), null, null);
	    			// rollback
	    	        try { 
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"create rollback snapshot", null, null);
    		            createSnapshot(session,pkgdoc);
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"snapshot created successfully", null, null);
	    	        } catch (Exception e) {
	    	    	    /*-ERROR-*/DfLogger.error(this, m+"error while loading or executing promotion service module",null,e);
	    	            throw e;        	
	    	        }
	    		}
	    	}
	    } catch (Exception e) {
    	    /*-ERROR-*/DfLogger.error(this, m+"error in promoting workitem packages...",null,e);
    	    sMgr.release(session);
    	    throw new RuntimeException (e);
	    } finally {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"finally, close the package collection", null, null);
		    try { 
		    	if (packages != null)
		    		packages.close();
		    } catch (DfException dfe) {
	    	    /*-ERROR-*/DfLogger.error(this, m+"error in closing packages collection",null,dfe);
		    }

	    }
	    
	    // okey-dokey: now check for additional attachments to the workflow...
	    IDfCollection attachments = null;
	    try {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting ATTACHMENT collection for workitem", null, null);
	    	attachments =  task.getAttachments();
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
				    
	    			// rollback
	    	        try { 
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"create rollback snapshot", null, null);
    		            createSnapshot(session,pkgdoc);
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"snapshot created successfully", null, null);
	    	        } catch (Exception e) {
	    	    	    /*-ERROR-*/DfLogger.error(this, m+"error while loading or executing promotion service module",null,e);
	    	            throw e;        	
	    	        }
	    		}
	    	}
	    } catch (Exception e) {
    	    /*-ERROR-*/DfLogger.error(this, m+"error in promoting workitem attachments...",null,e);
    	    sMgr.release(session);
    	    throw new RuntimeException(e);
	    } finally {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"finally, close the attachment collection", null, null);
		    try {
		    	if (attachments != null)
		    		attachments.close();
		    } catch (DfException dfe) {
	    	    /*-ERROR-*/DfLogger.error(this, m+"error in closing attachments collection",null,dfe);
		    }
	    }
	    
	    
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing session", null, null);
    	sMgr.release(session);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session released", null, null);
	}

	
	void createSnapshot(IDfSession session, IDfDocument doc) throws DfException
	{
        IDfPersistentObject attachment = session.newObject("mrcs_document_snapshot");
        attachment.setId("doc_chronicle_id", doc.getChronicleId());
        attachment.setString("doc_version_summary", doc.getVersionPolicy().getVersionSummary(","));
        attachment.setId("doc_object_id", doc.getObjectId());
        attachment.setId("doc_acl_id", doc.getACL().getObjectId());
        //attachment.setBoolean("is_rejected_doc_active", true);
        attachment.setTime("doc_creation_date", doc.getCreationDate());
        attachment.setId("doc_policy_id", doc.getPolicyId());
        attachment.setString("doc_state", doc.getCurrentStateName());
        attachment.setString("transaction_type","workflow");
        attachment.save();
		
	}

}
