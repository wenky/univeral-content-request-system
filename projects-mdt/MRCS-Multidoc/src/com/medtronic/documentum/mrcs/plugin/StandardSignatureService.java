package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.services.workflow.inbox.IWorkflowTask;

// apparently we have a race condition when signing docs. If only automated tasks follow the signature process, 
// it is a race between lookup of the attachments and whether the server completes/erases the workflow information...
// perhaps it would be best to get all attachment and package information before completing the workitem
// - I think the super.onCommitChanges() call in the Forward/FinishWFT classes begins the process of finishing the workflow...


public class StandardSignatureService implements ISignatureService
{

	public void sign(IDfSessionManager sMgr, String docbase, String mrcsapp, String processname, IWorkflowTask task, IDfDocument primarypackage,String username,String password,String reason, Map plugin, Map context) throws DfException
	{
        /*-CONFIG-*/String m = "StandardSignatureService.sign-";
		
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
				    IDfDocument doc = (IDfDocument)session.getObject(new DfId(compid));
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- performing TCS pdf-based signature process", null, null);
				    boolean flag = signDocument(session,doc,username,password,reason);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- signing result: "+flag, null, null);
	    		}
	    	}
	    } catch (DfException e) {
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
				    
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- performing TCS pdf-based signature process", null, null);
				    boolean flag = signDocument(session,pkgdoc,username,password,reason);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- signing result: "+flag, null, null);
	    		}
	    	}
	    } catch (DfException e) {
    	    /*-ERROR-*/DfLogger.error(this, m+"error in promoting workitem attachments...",null,e);
    	    throw e;
	    } finally {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"finally, close the attachment collection", null, null);
	    	if (attachments != null)
	    		attachments.close();
	    	sMgr.release(session);
	    }
		
	}
	
	public boolean signDocument(IDfSession session, IDfDocument docObject, String username, String password, String reason) throws DfException
	{
	    
		//public boolean signDocument(IDfDocument docObject, ESignDTO sign ) throws DfException
    	boolean canCommit = false;
    
    	try {
        
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument : usrName "+username, null, null);
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument : reasonTxt "+reason, null, null);
    		
    		
    
    		//com.documentum.fc.client.IDfDocument docObject = ESignHelper.getSignableDocument(itask,taskInfo);
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : calling add eSign for docObject "+docObject, null, null);
    		com.documentum.fc.common.IDfId auditRec = docObject.addESignature(username,password,reason,"pdf","","","","","","");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : eSign audit record id: "+(auditRec == null ? null : auditRec.getId()), null, null);
            
            try {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : verifying esignature",null,null);                
                docObject.verifyESignature();
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this, "MRCS:ESignServiceSBO.signDocument  : verifyESignature on docObject "+docObject.getObjectId().getId()+" detected error",null,e);
                throw new RuntimeException("MRCSError in verification of signature (ESignServiceSBO)");
            }
    		//signoff(java.lang.String user,java.lang.String os_password, java.lang.String reason)
    		canCommit = true;
    
        }catch(DfException e1){
        	canCommit = false;
        	/*-ERROR-*/DfLogger.error(this, "MRCS:ESignServiceSBO.signDocument : Exception Occurred in eSign service", null, e1);
        	throw e1;
    	}
        return canCommit;
	}


}
