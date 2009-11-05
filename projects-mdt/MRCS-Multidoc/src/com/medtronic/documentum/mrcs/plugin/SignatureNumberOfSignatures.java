package com.medtronic.documentum.mrcs.plugin;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.documentum.services.workflow.inbox.IWorkflowTask;

public class SignatureNumberOfSignatures {

    public boolean validate(IDfSessionManager sMgr, String docbase, String mrcsapp, String processname, IWorkflowTask task, IDfSysObject packagedoc, List errors, Map configdata, Map customdata) throws Exception
    {
        /*-CONFIG-*/String m = "SignatureNumberOfSignatures.validate-";
    	
    	// check all packages and attachments for renditions of specified format
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get max number of signatures", null, null);
	    int maxSignatures = Integer.parseInt((String)configdata.get("MaxSignatures"));
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" -- format: "+maxSignatures, null, null);
	    

    	
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
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--checking for signature overload", null, null);
				    boolean flag = numSigned(session, doc, maxSignatures);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- validation result: "+flag, null, null);
				    if (!flag) {
				    	errors.add("MSG_SIGNATURE_OVERLOAD_OR_DUPLICATE_REASON");
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
				    
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- checking attachment for signatures overload", null, null);
				    boolean flag = numSigned(session, pkgdoc, maxSignatures);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- validation result: "+flag, null, null);
				    if (!flag) {
				    	errors.add("MSG_SIGNATURE_OVERLOAD_OR_DUPLICATE_REASON");
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

	
    private boolean numSigned(IDfSession session, IDfDocument docObject, int noSigns) throws DfException {
        /*-CONFIG-*/String m = "numSigned - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        
        double numSigned = 0;
        String docId = docObject.getObjectId().getId();

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"numSigned:   "+numSigned, null, null);

        String qualification = "dm_audittrail where " +
		"audited_obj_id='"+docId+"' and " +
		"version_label='"+docObject.getVersionLabel(0)+"'";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing DQL ["+"select count(*) from "+qualification+"]", null, null);

        IDfQuery qry = new DfQuery();
        qry.setDQL("select count(*) from "+qualification);
		IDfCollection myObj1 = (IDfCollection)qry.execute(session,IDfQuery.DF_READ_QUERY);

		try { 
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scanning dql results", null, null);
			while(myObj1.next()) 
			{
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Begin next result - check its attrs", null, null);
			    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--attrnum: "+i, null, null);
					IDfAttr attr = myObj1.getAttr(i);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--attrname: "+attr.getName(), null, null);
					if (attr.getDataType() == attr.DM_DOUBLE) {
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"----double attr found, parsing number of signatures", null, null);
					    numSigned = myObj1.getDouble(attr.getName());
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "MRCS:ESignPrecondition :numSigned: No of times Signed :" +numSigned, null, null);
					}
			    }
			} 
		} finally {
			myObj1.close();
		}
		if(numSigned >= noSigns) return false;
		else return true;

    }

}
